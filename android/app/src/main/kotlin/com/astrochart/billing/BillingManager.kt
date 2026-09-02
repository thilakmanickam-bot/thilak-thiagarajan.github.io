package com.astrochart.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.astrochart.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.coroutines.resume

/** The two Halo Premium subscription products — must match the IDs created in
 *  Play Console → Monetize → Subscriptions exactly. */
const val PRODUCT_ID_MONTHLY = "halo_premium_monthly"
const val PRODUCT_ID_YEARLY = "halo_premium_yearly"

/** One purchasable subscription option, with its real Play-localized price. */
data class SubscriptionOption(
    val productId: String,
    val productDetails: ProductDetails,
    val offerToken: String,
    val formattedPrice: String,
    val billingPeriodIso: String
)

/**
 * Wraps [BillingClient] for Halo Premium's two subscriptions. Connects lazily,
 * verifies every purchase server-side via the `verifyPurchase` Cloud Function
 * (see `functions/src/billing.ts`) before trusting it — the client never
 * decides its own entitlement, it only reports what Play returned and caches
 * whatever the server verifies (see [PremiumStore]).
 *
 * Depends on the plain `billing` artifact, not `billing-ktx`: the ktx
 * suspend extensions are a thin wrapper over the same callback methods used
 * below, and skipping them avoids a Kotlin-metadata version mismatch between
 * a recent billing-ktx release and this project's older Kotlin Gradle plugin
 * (surfaced as a `kaptDebugKotlin` failure — see git history for the exact
 * error before this was reverted to plain callbacks).
 */
class BillingManager(private val context: Context) {

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                pendingHandler?.invoke(purchase)
            }
        }
    }

    // Set per-call by callers that need to react to a fresh purchase (e.g. the
    // Subscription screen); purchases arriving with nothing listening (e.g. a
    // renewal detected on next launch) are picked up by refreshEntitlement()
    // via queryPurchasesAsync() instead, so nothing is silently dropped.
    private var pendingHandler: ((Purchase) -> Unit)? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        // Required by the Billing Library even though Halo only sells
        // subscriptions, not one-time products — this flag governs pending
        // *one-time* purchases specifically and has no effect on subscriptions.
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private suspend fun ensureConnected(): Boolean {
        if (billingClient.isReady) return true
        return suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (cont.isActive) cont.resume(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                }
                override fun onBillingServiceDisconnected() {
                    // Not resumed here — startConnection's own callback above
                    // already resolves this coroutine; a later call re-connects.
                }
            })
        }
    }

    /** The two subscriptions' real, Play-localized product details and prices. */
    suspend fun querySubscriptionOptions(): List<SubscriptionOption> {
        if (!ensureConnected()) return emptyList()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(PRODUCT_ID_MONTHLY, PRODUCT_ID_YEARLY).map {
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(it)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }
            )
            .build()
        val productDetailsList = suspendCancellableCoroutine<List<ProductDetails>?> { cont ->
            billingClient.queryProductDetailsAsync(params) { _, result ->
                if (cont.isActive) cont.resume(result.productDetailsList)
            }
        }
        return productDetailsList.orEmpty().mapNotNull { details ->
            val offer = details.subscriptionOfferDetails?.firstOrNull() ?: return@mapNotNull null
            val price = offer.pricingPhases.pricingPhaseList.firstOrNull() ?: return@mapNotNull null
            SubscriptionOption(
                productId = details.productId,
                productDetails = details,
                offerToken = offer.offerToken,
                formattedPrice = price.formattedPrice,
                billingPeriodIso = price.billingPeriod
            )
        }
    }

    /** Launches Play's purchase UI for [option]. Result arrives via the
     *  purchases-updated listener, handled by [onPurchaseResult]. */
    suspend fun launchPurchase(activity: Activity, option: SubscriptionOption, onPurchaseResult: (Purchase) -> Unit) {
        if (!ensureConnected()) return
        pendingHandler = onPurchaseResult
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(option.productDetails)
                        .setOfferToken(option.offerToken)
                        .build()
                )
            )
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    /**
     * Acknowledges [purchase] (required within 3 days or Play auto-refunds),
     * then asks the server to verify it and returns the verified entitlement.
     * Returns null on any failure — callers should treat that as "not yet
     * confirmed premium," not as an error to alarm the user with; the next
     * [refreshEntitlement] retries.
     */
    suspend fun acknowledgeAndVerify(purchase: Purchase): VerifiedEntitlement? {
        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            val ackResult = suspendCancellableCoroutine<BillingResult> { cont ->
                billingClient.acknowledgePurchase(ackParams) { billingResult ->
                    if (cont.isActive) cont.resume(billingResult)
                }
            }
            if (ackResult.responseCode != BillingClient.BillingResponseCode.OK) return null
        }
        val productId = purchase.products.firstOrNull() ?: return null
        val verified = verifyWithServer(productId, purchase.purchaseToken) ?: return null
        PremiumStore.save(context, verified.active, verified.expiresAtMillis)
        return verified
    }

    /**
     * Re-checks current purchases with Play (cheap, local) and re-verifies
     * whichever subscription is found with the server — called on app launch
     * so entitlement never drifts far from reality without needing push
     * notifications from Play (see the plan's explicit no-RTDN decision).
     */
    suspend fun refreshEntitlement(): VerifiedEntitlement? {
        if (!ensureConnected()) return null
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val purchasesList = suspendCancellableCoroutine<List<Purchase>?> { cont ->
            billingClient.queryPurchasesAsync(params) { _, list ->
                if (cont.isActive) cont.resume(list)
            }
        }
        val active = purchasesList.orEmpty().firstOrNull {
            it.purchaseState == Purchase.PurchaseState.PURCHASED
        } ?: run {
            PremiumStore.save(context, active = false, expiresAtMillis = 0L)
            return null
        }
        return acknowledgeAndVerify(active)
    }

    private suspend fun verifyWithServer(productId: String, purchaseToken: String): VerifiedEntitlement? {
        val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token ?: return null
        return runCatching {
            val body = JSONObject()
                .put("productId", productId)
                .put("purchaseToken", purchaseToken)
                .toString()
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(BuildConfig.BILLING_VERIFY_BASE_URL)
                .header("Authorization", "Bearer $idToken")
                .post(body)
                .build()
            VERIFY_CLIENT.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching null
                val json = JSONObject(response.body?.string().orEmpty())
                VerifiedEntitlement(
                    active = json.optBoolean("active", false),
                    expiresAtMillis = json.optLong("expiresAtMillis", 0L)
                )
            }
        }.getOrNull()
    }

    private companion object {
        val VERIFY_CLIENT = OkHttpClient()
    }
}

data class VerifiedEntitlement(val active: Boolean, val expiresAtMillis: Long)

import { onRequest } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import * as logger from "firebase-functions/logger";
import express, { Request, Response } from "express";
import { GoogleAuth } from "google-auth-library";
import { admin, userDoc, verifyCaller } from "./common";

const playServiceAccountJson = defineSecret("PLAY_SERVICE_ACCOUNT_JSON");

const PACKAGE_NAME = "com.techbyt.halo";
const PLAY_SCOPE = "https://www.googleapis.com/auth/androidpublisher";

const KNOWN_PRODUCT_IDS = new Set(["halo_premium_monthly", "halo_premium_yearly"]);

// Subscription states the Play Developer API v2 considers "the user
// currently has access" — see SubscriptionState in the subscriptionsv2
// reference. Anything else (expired, on hold, paused, canceled-and-lapsed)
// is treated as not active.
const ACTIVE_STATES = new Set([
  "SUBSCRIPTION_STATE_ACTIVE",
  "SUBSCRIPTION_STATE_IN_GRACE_PERIOD",
]);

let cachedAuth: GoogleAuth | null = null;

function playAuth(): GoogleAuth {
  if (!cachedAuth) {
    const credentials = JSON.parse(playServiceAccountJson.value());
    cachedAuth = new GoogleAuth({ credentials, scopes: [PLAY_SCOPE] });
  }
  return cachedAuth;
}

interface VerifiedEntitlement {
  active: boolean;
  expiresAtMillis: number;
}

async function fetchSubscriptionState(purchaseToken: string): Promise<VerifiedEntitlement> {
  const client = await playAuth().getClient();
  const url =
    `https://androidpublisher.googleapis.com/androidpublisher/v3/applications/` +
    `${PACKAGE_NAME}/purchases/subscriptionsv2/tokens/${encodeURIComponent(purchaseToken)}`;

  const response = await client.request<{
    subscriptionState?: string;
    lineItems?: { expiryTime?: string }[];
  }>({ url });

  const data = response.data;
  const active = ACTIVE_STATES.has(data.subscriptionState ?? "");
  const expiryTime = data.lineItems?.[0]?.expiryTime;
  const expiresAtMillis = expiryTime ? Date.parse(expiryTime) : 0;
  return { active, expiresAtMillis };
}

const app = express();
app.use(express.json());

// Mounted at the function root — `BillingManager.kt` posts directly to
// `BILLING_VERIFY_BASE_URL` with no extra path segment.
app.post("/", async (req: Request, res: Response) => {
  const uid = await verifyCaller(req.header("Authorization"));
  if (!uid) {
    res.status(401).json({ error: "unauthenticated" });
    return;
  }

  const { productId, purchaseToken } = req.body ?? {};
  if (typeof productId !== "string" || typeof purchaseToken !== "string" || !purchaseToken) {
    res.status(400).json({ error: "invalid_request" });
    return;
  }
  if (!KNOWN_PRODUCT_IDS.has(productId)) {
    res.status(400).json({ error: "unknown_product" });
    return;
  }

  try {
    const entitlement = await fetchSubscriptionState(purchaseToken);

    await userDoc(uid).set(
      {
        premiumActive: entitlement.active,
        premiumExpiresAt: entitlement.expiresAtMillis
          ? admin.firestore.Timestamp.fromMillis(entitlement.expiresAtMillis)
          : null,
        premiumProductId: entitlement.active ? productId : null,
      },
      { merge: true }
    );

    res.status(200).json({
      active: entitlement.active,
      expiresAtMillis: entitlement.expiresAtMillis,
    });
  } catch (err) {
    logger.error("Play purchase verification failed", err);
    res.status(502).json({ error: "verification_failed" });
  }
});

/**
 * Verifies a Play Billing purchase token server-side against the Play
 * Developer API, then writes the result to `users/{uid}` (the same doc
 * `ProfileSync.kt` writes) so `chat.ts` and any future client can trust
 * `premiumActive` without re-verifying. Called by `BillingManager.kt` right
 * after a purchase, and again on each app launch to refresh entitlement.
 */
export const verifyPurchase = onRequest(
  { region: "us-central1", secrets: [playServiceAccountJson], cors: false, timeoutSeconds: 30 },
  app
);

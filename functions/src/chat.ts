import { onRequest } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import * as logger from "firebase-functions/logger";
import express, { Request, Response } from "express";
import { admin, userDoc, verifyCaller } from "./common";

const anthropicApiKey = defineSecret("ANTHROPIC_API_KEY");

const ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";
const ANTHROPIC_VERSION = "2023-06-01";

/**
 * Daily message caps. Premium subscribers (verified via `verifyPurchase`,
 * see billing.ts, and cached on `users/{uid}.premiumActive`) get a much
 * higher ceiling — still a sane abuse guard, not truly unlimited. Everyone
 * else keeps the original interim cap from before billing existed.
 */
const DAILY_MESSAGE_LIMIT_BASIC = 20;
const DAILY_MESSAGE_LIMIT_PREMIUM = 200;

/** UTC calendar date as `YYYY-MM-DD`, used to key the per-day usage doc. */
function todayUtc(): string {
  return new Date().toISOString().slice(0, 10);
}

/**
 * Atomically reads-and-increments today's message count for `uid`. Returns
 * false (without incrementing) once the caller is already at their cap —
 * premium subscribers get the higher [DAILY_MESSAGE_LIMIT_PREMIUM] ceiling.
 */
async function tryConsumeQuota(uid: string): Promise<boolean> {
  const db = admin.firestore();
  const docRef = db.collection("chatUsage").doc(`${uid}_${todayUtc()}`);
  const profileSnap = await userDoc(uid).get();
  const isPremium = profileSnap.exists && profileSnap.data()?.premiumActive === true;
  const limit = isPremium ? DAILY_MESSAGE_LIMIT_PREMIUM : DAILY_MESSAGE_LIMIT_BASIC;

  return db.runTransaction(async (tx) => {
    const snap = await tx.get(docRef);
    const count = snap.exists ? (snap.data()?.count as number) ?? 0 : 0;
    if (count >= limit) {
      return false;
    }
    tx.set(docRef, { count: count + 1, updatedAt: admin.firestore.FieldValue.serverTimestamp() }, { merge: true });
    return true;
  });
}

// Express, not a bare onRequest handler, so a request to the deployed
// function's URL with `v1/messages` appended (exactly what `AnthropicApi.kt`'s
// `@POST("v1/messages")` sends, relative to `CHAT_PROXY_BASE_URL`) routes
// correctly instead of 404ing at the function's bare root.
const app = express();
app.use(express.json());

app.post("/v1/messages", async (req: Request, res: Response) => {
  const uid = await verifyCaller(req.header("Authorization"));
  if (!uid) {
    res.status(401).json({ error: "unauthenticated" });
    return;
  }

  const allowed = await tryConsumeQuota(uid);
  if (!allowed) {
    res.status(429).json({ error: "rate_limited" });
    return;
  }

  try {
    const upstream = await fetch(ANTHROPIC_URL, {
      method: "POST",
      headers: {
        "x-api-key": anthropicApiKey.value(),
        "anthropic-version": ANTHROPIC_VERSION,
        "content-type": "application/json",
      },
      body: JSON.stringify(req.body),
    });

    const bodyText = await upstream.text();
    res.status(upstream.status);
    res.setHeader("content-type", upstream.headers.get("content-type") ?? "application/json");
    res.send(bodyText);
  } catch (err) {
    logger.error("Anthropic upstream request failed", err);
    res.status(502).json({ error: "upstream_failed" });
  }
});

/**
 * Chat proxy for the Android app's "Ask the Universe" feature. The request
 * body at `v1/messages` is already shaped exactly like Anthropic's
 * `/v1/messages` payload (see `AnthropicApi.kt`'s `ChatRequest`), so it's
 * forwarded verbatim — this function only adds auth, rate limiting, and the
 * real Anthropic API key (which never ships in the app).
 */
export const chatProxy = onRequest(
  { region: "us-central1", secrets: [anthropicApiKey], cors: false, timeoutSeconds: 60 },
  app
);

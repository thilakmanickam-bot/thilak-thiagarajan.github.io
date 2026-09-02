import { onRequest } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import * as logger from "firebase-functions/logger";
import * as admin from "firebase-admin";
import express, { Request, Response } from "express";

admin.initializeApp();

const anthropicApiKey = defineSecret("ANTHROPIC_API_KEY");

const ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";
const ANTHROPIC_VERSION = "2023-06-01";

/**
 * Interim access rule while there's no real subscription to check yet (see
 * the plan doc): any signed-in user may chat, capped at this many messages
 * per UTC calendar day. Once billing exists, replace/extend this with a
 * `premiumActive` check on the same Firestore user doc `ProfileSync.kt`
 * already writes to.
 */
const DAILY_MESSAGE_LIMIT = 20;

/** UTC calendar date as `YYYY-MM-DD`, used to key the per-day usage doc. */
function todayUtc(): string {
  return new Date().toISOString().slice(0, 10);
}

/**
 * Atomically reads-and-increments today's message count for `uid`. Returns
 * false (without incrementing) once the caller is already at the cap.
 */
async function tryConsumeQuota(uid: string): Promise<boolean> {
  const db = admin.firestore();
  const docRef = db.collection("chatUsage").doc(`${uid}_${todayUtc()}`);

  return db.runTransaction(async (tx) => {
    const snap = await tx.get(docRef);
    const count = snap.exists ? (snap.data()?.count as number) ?? 0 : 0;
    if (count >= DAILY_MESSAGE_LIMIT) {
      return false;
    }
    tx.set(docRef, { count: count + 1, updatedAt: admin.firestore.FieldValue.serverTimestamp() }, { merge: true });
    return true;
  });
}

/**
 * Verifies the `Authorization: Bearer <idToken>` header against Firebase
 * Auth for this same project (the app signs users in via `AuthManager.kt`).
 * Returns the caller's uid, or null if missing/invalid.
 */
async function verifyCaller(authHeader: string | undefined): Promise<string | null> {
  if (!authHeader?.startsWith("Bearer ")) return null;
  const idToken = authHeader.slice("Bearer ".length).trim();
  if (!idToken) return null;
  try {
    const decoded = await admin.auth().verifyIdToken(idToken);
    return decoded.uid;
  } catch (err) {
    logger.warn("ID token verification failed", err);
    return null;
  }
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

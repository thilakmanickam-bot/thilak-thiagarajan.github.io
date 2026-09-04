import { onRequest } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import * as logger from "firebase-functions/logger";
import express, { Request, Response } from "express";
import { admin, userDoc, verifyCaller } from "./common";

/**
 * The tester code, held in Secret Manager rather than in the repo or the APK.
 *
 * This is the whole reason redemption is server-side. A code compiled into the
 * app can be read out of any published build with `strings classes.dex`, and
 * once one tester posts it, every install has free Premium with no way to
 * revoke it short of shipping a new version. Here it is rotated with
 * `firebase functions:secrets:set TESTER_CODE` and a redeploy.
 */
const testerCode = defineSecret("TESTER_CODE");

/** How long a redeemed code grants Premium for. */
const GRANT_DAYS = 90;

/**
 * Written to `premiumProductId` so a tester is distinguishable from a paying
 * subscriber in Firestore — the two otherwise write identical fields, which is
 * the point (every existing Premium gate works unchanged) but would make the
 * books unreadable without this.
 */
const TESTER_PRODUCT_ID = "tester_code";

/** Wrong-code attempts allowed per user per UTC day, before 429. */
const MAX_ATTEMPTS_PER_DAY = 10;

/** UTC calendar date as `YYYY-MM-DD`, matching chat.ts's usage keys. */
function todayUtc(): string {
  return new Date().toISOString().slice(0, 10);
}

/**
 * Atomically counts one redemption attempt, returning false once the caller is
 * over [MAX_ATTEMPTS_PER_DAY]. A short fixed code is guessable at machine
 * speed, and an authenticated endpoint that grants Premium is worth guessing
 * at — so the attempt is counted before the comparison, not after a failure.
 */
async function tryConsumeAttempt(uid: string): Promise<boolean> {
  const db = admin.firestore();
  const docRef = db.collection("testerAttempts").doc(`${uid}_${todayUtc()}`);
  return db.runTransaction(async (tx) => {
    const snap = await tx.get(docRef);
    const count = snap.exists ? (snap.data()?.count as number) ?? 0 : 0;
    if (count >= MAX_ATTEMPTS_PER_DAY) {
      return false;
    }
    tx.set(
      docRef,
      { count: count + 1, updatedAt: admin.firestore.FieldValue.serverTimestamp() },
      { merge: true }
    );
    return true;
  });
}

// Express rather than a bare handler so the two routes below can share one
// deployed function. The app posts to `TESTER_REDEEM_BASE_URL` + "/redeem" or
// + "/request"; the base URL carries no trailing slash.
const app = express();
app.use(express.json());

app.post("/redeem", async (req: Request, res: Response) => {
  const uid = await verifyCaller(req.header("Authorization"));
  if (!uid) {
    res.status(401).json({ error: "unauthenticated" });
    return;
  }

  const submitted = req.body?.code;
  if (typeof submitted !== "string" || !submitted.trim()) {
    res.status(400).json({ error: "invalid_request" });
    return;
  }

  if (!(await tryConsumeAttempt(uid))) {
    res.status(429).json({ error: "too_many_attempts" });
    return;
  }

  // Case- and whitespace-insensitive: the code is typed by hand, often from a
  // phone keyboard that capitalises, and rejecting "test2026halo" would be a
  // support ticket rather than a security measure.
  if (submitted.trim().toUpperCase() !== testerCode.value().trim().toUpperCase()) {
    res.status(403).json({ error: "invalid_code" });
    return;
  }

  const expiresAtMillis = Date.now() + GRANT_DAYS * 24 * 60 * 60 * 1000;

  try {
    // The same three fields verifyPurchase writes, so every Premium gate in
    // the app reads a tester exactly as it reads a subscriber.
    await userDoc(uid).set(
      {
        premiumActive: true,
        premiumExpiresAt: admin.firestore.Timestamp.fromMillis(expiresAtMillis),
        premiumProductId: TESTER_PRODUCT_ID,
        testerRedeemedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    // Mirrors verifyPurchase's response shape exactly, so the client parses
    // both with the same code.
    res.status(200).json({ active: true, expiresAtMillis });
  } catch (err) {
    logger.error("Tester code redemption failed to write entitlement", err);
    res.status(502).json({ error: "grant_failed" });
  }
});

app.post("/request", async (req: Request, res: Response) => {
  const uid = await verifyCaller(req.header("Authorization"));
  if (!uid) {
    res.status(401).json({ error: "unauthenticated" });
    return;
  }

  const email = req.body?.email;
  if (typeof email !== "string" || !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email.trim())) {
    res.status(400).json({ error: "invalid_email" });
    return;
  }

  try {
    // Keyed by uid, not auto-id: someone tapping the button three times should
    // update one row rather than leave three for the reviewer to reconcile.
    await admin
      .firestore()
      .collection("testerRequests")
      .doc(uid)
      .set(
        {
          uid,
          email: email.trim(),
          appVersion: typeof req.body?.appVersion === "string" ? req.body.appVersion : null,
          status: "pending",
          requestedAt: admin.firestore.FieldValue.serverTimestamp(),
        },
        { merge: true }
      );
    res.status(200).json({ received: true });
  } catch (err) {
    logger.error("Tester access request failed to write", err);
    res.status(502).json({ error: "request_failed" });
  }
});

/**
 * Tester access for Halo Premium: redeems a code into the same entitlement
 * fields `verifyPurchase` writes (see `functions/src/billing.ts`), and takes
 * access requests from people who do not have a code yet.
 *
 * Approval is deliberately manual — requests land in `testerRequests` for the
 * owner to read in the Firebase console and mail the code back. Automating
 * that would mean a mail provider, another secret and a deliverability setup
 * for a step a human decides anyway.
 */
export const redeemTesterCode = onRequest(
  { region: "us-central1", secrets: [testerCode], cors: false, timeoutSeconds: 30 },
  app
);

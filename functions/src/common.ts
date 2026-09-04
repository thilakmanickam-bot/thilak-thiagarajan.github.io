import * as logger from "firebase-functions/logger";
import * as admin from "firebase-admin";

// Node module caching means this runs once even though both chat.ts and
// billing.ts import this file.
admin.initializeApp();

export { admin };

/**
 * Verifies the `Authorization: Bearer <idToken>` header against Firebase
 * Auth for this project (the app signs users in via `AuthManager.kt`).
 * Returns the caller's uid, or null if missing/invalid.
 */
export async function verifyCaller(authHeader: string | undefined): Promise<string | null> {
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

/** The Firestore doc `ProfileSync.kt` already writes the primary profile to. */
export function userDoc(uid: string) {
  return admin.firestore().collection("users").doc(uid);
}

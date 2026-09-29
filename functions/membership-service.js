"use strict";

const { MembershipError, validUid, fetchProfile, membershipView } = require("./membership");
const { randomUUID } = require("node:crypto");

// New server-only state. The live rules allow users/{uid} and notifications only;
// private subcollections are denied. Do not deploy a recursive owner-write rule.
function createMembershipService({ db, auth, secret, fetchImpl, clock = Date.now }) {
  function refs(uid) {
    const user = db.collection("users").doc(uid);
    return { user, state: user.collection("private").doc("membershipState") };
  }

  function requireUser(doc) {
    if (!doc.exists || doc.get("membershipDeletionPending") === true) {
      throw new MembershipError("account-unavailable");
    }
  }

  async function requireAuthAccount(uid) {
    let account;
    try {
      account = await auth.getUser(uid);
    } catch (error) {
      if (error?.code === "auth/user-not-found") throw new MembershipError("account-unavailable");
      throw new MembershipError("membership-unavailable");
    }
    if (account.disabled) throw new MembershipError("account-unavailable");
  }

  async function refresh(uid, { webhook = false, eventHash = null } = {}) {
    if (!validUid(uid)) throw new MembershipError("invalid-user");
    await requireAuthAccount(uid);
    const { user, state } = refs(uid);
    const startedAt = clock();
    const reservation = await db.runTransaction(async (tx) => {
      const [userDoc, stateDoc] = await Promise.all([tx.get(user), tx.get(state)]);
      requireUser(userDoc);
      const current = stateDoc.data() ?? {};
      if (eventHash && eventHash === current.lastEventHash && current.snapshot) {
        return { cached: current.snapshot };
      }
      // Caller cannot force-refresh arbitrary UIDs or bypass provider rate limiting.
      if (!webhook && Number.isFinite(current.attemptedAt) && startedAt - current.attemptedAt < 30000) {
        if (current.snapshot && current.completedRevision === current.revision) return { cached: current.snapshot };
        throw new MembershipError("refresh-in-progress");
      }
      const revision = (Number.isSafeInteger(current.revision) ? current.revision : 0) + 1;
      const refreshToken = randomUUID();
      tx.set(state, { revision, refreshToken, attemptedAt: startedAt }, { merge: true });
      return { revision, refreshToken };
    });
    if (reservation.cached) return membershipView(reservation.cached, clock());

    // Webhook is an invalidation signal, never authority for granting access itself.
    // Fetch current state: event_datetime can be older than previously delivered events.
    const snapshot = await fetchProfile(uid, secret(), fetchImpl, clock());
    await requireAuthAccount(uid);
    const committed = await db.runTransaction(async (tx) => {
      const [userDoc, stateDoc] = await Promise.all([tx.get(user), tx.get(state)]);
      requireUser(userDoc);
      const current = stateDoc.data() ?? {};
      // Token also prevents revision reuse (ABA) if a document was removed and recreated.
      if (current.refreshToken !== reservation.refreshToken || current.revision !== reservation.revision) {
        if (!current.snapshot || current.completedRevision !== current.revision) {
          throw new MembershipError("refresh-in-progress");
        }
        return current.snapshot;
      }
      tx.set(state, { snapshot, completedRevision: reservation.revision, lastEventHash: eventHash }, { merge: true });
      return snapshot;
    });
    return membershipView(committed, clock());
  }

  // Set before recursive deletion. Transactions that read the parent conflict with
  // this write and must stop; ordinary delayed callbacks cannot recreate subdocuments.
  async function beginDeletion(uid) {
    const { user } = refs(uid);
    await db.runTransaction(async (tx) => {
      const userDoc = await tx.get(user);
      if (userDoc.exists) tx.update(user, { membershipDeletionPending: true });
    });
  }

  return { refresh, beginDeletion };
}

module.exports = { createMembershipService };

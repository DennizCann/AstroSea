package com.denizcan.astrosea.billing

import com.adapty.Adapty
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class MembershipLevel(
    val source: String,
    val environment: String,
    val productId: String?,
    val startsAt: String?,
    val expiresAt: String?,
    val renewalCancelledAt: String?,
    val isActive: Boolean
)

data class MembershipState(
    val uid: String? = null,
    val verified: Boolean = false,
    val levels: List<MembershipLevel> = emptyList()
) {
    val activeLevels: List<MembershipLevel> get() = levels.filter {
        it.isActive && (it.expiresAt == null || (membershipTime(it.expiresAt) ?: 0) > System.currentTimeMillis())
    }
    val hasAccess: Boolean get() = verified && activeLevels.isNotEmpty()
}

fun membershipTime(value: String): Long? = runCatching {
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
        isLenient = false
    }.parse(value)?.time
}.getOrNull()

/** Only the authenticated server response grants access; never public profile fields. */
object MembershipRepository {
    private val auth = FirebaseAuth.getInstance()
    private val functions = FirebaseFunctions.getInstance("europe-west1")
    private val refreshLock = Mutex()
    private val identityLock = Mutex()
    private var billingUid: String? = null
    private val mutableState = MutableStateFlow(MembershipState())
    val state = mutableState.asStateFlow()

    init {
        auth.addAuthStateListener { mutableState.value = MembershipState(uid = it.currentUser?.uid) }
    }

    suspend fun refresh(): MembershipState = refreshLock.withLock {
        val uid = auth.currentUser?.uid ?: error("No session")
        try {
            val data = functions.getHttpsCallable("getMembership").call().await().getData() as? Map<*, *>
                ?: error("Invalid membership")
            check(data["schemaVersion"] == 1 || (data["schemaVersion"] as? Number)?.toInt() == 1)
            val levels = (data["accessLevels"] as? List<*>)?.map { raw ->
                val level = raw as? Map<*, *> ?: error("Invalid membership level")
                MembershipLevel(
                    source = level["source"] as? String ?: "manual",
                    environment = level["environment"] as? String ?: "unknown",
                    productId = level["productId"] as? String,
                    startsAt = level["startsAt"] as? String,
                    expiresAt = level["expiresAt"] as? String,
                    renewalCancelledAt = level["renewalCancelledAt"] as? String,
                    isActive = level["isActive"] == true
                )
            } ?: error("Invalid membership levels")
            check(auth.currentUser?.uid == uid) { "Account changed" }
            MembershipState(uid, true, levels).also { mutableState.value = it }
        } catch (error: Exception) {
            if (auth.currentUser?.uid == uid) mutableState.value = MembershipState(uid)
            throw error
        }
    }

    /** Serialize identity changes with the entire purchase/restore operation. */
    suspend fun <T> withBillingIdentity(action: suspend (String) -> T): T = identityLock.withLock {
        val uid = auth.currentUser?.uid ?: error("No session")
        if (billingUid != uid) {
            suspendCancellableCoroutine<Unit> { continuation ->
                Adapty.logout { error ->
                    if (continuation.isActive) {
                        if (error == null) continuation.resume(Unit) else continuation.resumeWithException(error)
                    }
                }
            }
            billingUid = null
            suspendCancellableCoroutine<Unit> { continuation ->
                Adapty.identify(uid) { error ->
                    if (continuation.isActive) {
                        if (error == null) continuation.resume(Unit) else continuation.resumeWithException(error)
                    }
                }
            }
            billingUid = uid
        }
        check(auth.currentUser?.uid == uid) { "Account changed" }
        val result = action(uid)
        check(auth.currentUser?.uid == uid) { "Account changed" }
        result
    }
}

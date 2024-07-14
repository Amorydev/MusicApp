package com.amory.musicapp.config

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.AnyThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.RegistrationResponse
import net.openid.appauth.TokenResponse
import org.json.JSONException
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock

class AuthStateManager private constructor(context: Context) {

    companion object {
        private val INSTANCE_REF =
            AtomicReference<WeakReference<AuthStateManager?>>(WeakReference(null))
        private const val TAG = "AuthStateManager"
        private const val STORE_NAME = "AuthState"
        private const val KEY_STATE = "state"

        @AnyThread
        fun getInstance(context: Context): AuthStateManager {
            var manager = INSTANCE_REF.get().get()
            if (manager == null) {
                manager = AuthStateManager(context.applicationContext)
                INSTANCE_REF.set(WeakReference(manager))
            }
            return manager
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
    private val prefsLock = ReentrantLock()
    private val currentAuthState = AtomicReference<AuthState?>()

    @AnyThread
    fun getCurrent(): AuthState {
        currentAuthState.get()?.let { return it }
        val state = readState()
        return currentAuthState.compareAndSet(null, state)
            .takeIf { true }
            ?.let { state } ?: currentAuthState.get()!!
    }

    @AnyThread
    fun replace(state: AuthState): AuthState {
        writeState(state)
        currentAuthState.set(state)
        return state
    }

    @AnyThread

    fun updateAfterAuthorization(
        response: AuthorizationResponse?,
        ex: AuthorizationException?
    ): AuthState {
        val current = getCurrent()
        current.update(response, ex)
        return replace(current)
    }

    @AnyThread
    fun updateAfterTokenResponse(
        response: TokenResponse?,
        ex: AuthorizationException?
    ): AuthState {
        val current = getCurrent()
        current.update(response, ex)
        return replace(current)
    }

    @AnyThread
    fun updateAfterRegistration(
        response: RegistrationResponse,
        ex: AuthorizationException?
    ): AuthState {
        val current = getCurrent()
        if (ex != null) {
            return current
        }
        current.update(response)
        return replace(current)
    }

    @AnyThread
    @NonNull
    private fun readState(): AuthState {
        prefsLock.lock()
        return try {
            val currentState = prefs.getString(KEY_STATE, null)
            if (currentState == null) {
                AuthState()
            } else {
                try {
                    AuthState.jsonDeserialize(currentState)
                } catch (ex: JSONException) {
                    Log.w(TAG, "Failed to deserialize stored auth state - discarding")
                    AuthState()
                }
            }
        } finally {
            prefsLock.unlock()
        }
    }

    @AnyThread
    private fun writeState(state: AuthState?) {
        prefsLock.lock()
        try {
            with(prefs.edit()) {
                if (state == null) {
                    remove(KEY_STATE)
                } else {
                    putString(KEY_STATE, state.jsonSerializeString())
                }
                commit()
            }
        } finally {
            prefsLock.unlock()
        }
    }
}
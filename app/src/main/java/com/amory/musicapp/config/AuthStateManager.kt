package com.amory.musicapp.config

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.AnyThread
import androidx.annotation.NonNull
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

    private val mPrefs: SharedPreferences
    private val mPrefsLock = ReentrantLock()
    private val mCurrentAuthState = AtomicReference<AuthState>()

    init {
        mPrefs = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
    }

    @AnyThread
    fun getCurrent(): AuthState {
        if (mCurrentAuthState.get() != null) {
            return mCurrentAuthState.get()!!
        }
        val state = readState()
        return if (mCurrentAuthState.compareAndSet(null, state)) {
            state
        } else {
            mCurrentAuthState.get()!!
        }
    }

    @AnyThread
    fun replace(state: AuthState): AuthState {
        writeState(state)
        mCurrentAuthState.set(state)
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
        response: TokenResponse,
        ex: AuthorizationException?
    ): AuthState {
        val current = getCurrent()
        current.update(response, ex)
        return replace(current)
    }

    @AnyThread
    fun updateAfterRegistration(
        response: RegistrationResponse?,
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
        mPrefsLock.lock()
        return try {
            val currentState = mPrefs.getString(KEY_STATE, null)
            if (currentState == null) {
                AuthState()
            } else {
                try {
                    AuthState.jsonDeserialize(currentState)
                } catch (ex: JSONException) {
                    AuthState()
                }
            }
        } finally {
            mPrefsLock.unlock()
        }
    }

    @AnyThread
    private fun writeState(state: AuthState?) {
        mPrefsLock.lock()
        try {
            val editor = mPrefs.edit()
            if (state == null) {
                editor.remove(KEY_STATE)
            } else {
                editor.putString(KEY_STATE, state.jsonSerializeString())
            }
        } finally {
            mPrefsLock.unlock()
        }
    }

    companion object  {

        private val INSTANCE_REF: AtomicReference<WeakReference<AuthStateManager>> =
            AtomicReference(WeakReference(null))

        private const val TAG = "AuthStateManager"
        private const val STORE_NAME = "AuthState"
        private const val KEY_STATE = "state"

        @AnyThread
        fun getInstance(context: Context): AuthStateManager {
            var manager: AuthStateManager? = INSTANCE_REF.get().get()
            if (manager == null) {
                manager = AuthStateManager(context.applicationContext)
                INSTANCE_REF.set(WeakReference(manager))
            }
            return manager
        }
    }
}

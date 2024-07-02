package com.amory.musicapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.amory.musicapp.activities.PlayMusicActivity
import com.amory.musicapp.config.AuthStateManager
import com.amory.musicapp.config.Configuration
import com.amory.musicapp.databinding.ActivityMainBinding
import com.amory.musicapp.fragment.HomeFragment
import com.amory.musicapp.fragment.LibraryFragment
import com.amory.musicapp.fragment.ProfileFragment
import com.amory.musicapp.model.TokenClientResponse
import com.amory.musicapp.retrofit.APICallToken
import com.amory.musicapp.retrofit.RetrofitClient
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mAuthService: AuthorizationService
    private lateinit var mStateManager: AuthStateManager

    private lateinit var mExecutor: ExecutorService
    private lateinit var mConfiguration: Configuration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        init()
        setContentView(binding.root)
        sharedPreferences = this.getSharedPreferences("SAVE_TOKEN", Context.MODE_PRIVATE)
        RetrofitClient.init(this)

        getTokenClient()
        initViews()

        if (mStateManager.getCurrent().isAuthorized) {
            getTokenAuth()
        }
        if (mExecutor.isShutdown) {
            mExecutor = Executors.newSingleThreadExecutor()
        }

        if (mStateManager.getCurrent().isAuthorized) {
            getTokenAuth()
        }

        val response = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)

        if (response != null || ex != null) {
            mStateManager.updateAfterAuthorization(response, ex)
        }

        if (response?.authorizationCode != null) {
            mStateManager.updateAfterAuthorization(response, ex)
            exchangeAuthorizationCode(response)
        }
    }

    private fun init() {
        mStateManager = AuthStateManager.getInstance(this)
        mExecutor = Executors.newSingleThreadExecutor()
        mConfiguration = Configuration.getInstance(this)
        val config = Configuration.getInstance(this)
        mAuthService = AuthorizationService(
            this, AppAuthConfiguration.Builder()
                .setConnectionBuilder(config.getConnectionBuilder())
                .build()
        )
    }

    private fun initViews() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    replaceFragment(HomeFragment())
                    true
                }

                R.id.library -> {
                    replaceFragment(LibraryFragment())
                    true
                }

                R.id.profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }

                else -> {
                    replaceFragment(HomeFragment())
                    true
                }

            }

        }
        //Fragment default
        replaceFragment(HomeFragment())
    }


    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun getTokenClient() {
        val service = RetrofitClient.retrofitInstance.create(APICallToken::class.java)
        val callToken = service.getToken()
        callToken.enqueue(object : Callback<TokenClientResponse> {
            @SuppressLint("CommitPrefEdits")
            override fun onResponse(
                call: Call<TokenClientResponse>,
                response: Response<TokenClientResponse>
            ) {
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    /*Log.d("token", token.toString())*/
                    val editor = sharedPreferences.edit()
                    editor.putString("tokenClient", token)
                    editor.apply()
                }
            }

            override fun onFailure(call: Call<TokenClientResponse>, t: Throwable) {
                Log.d("token", t.message.toString())
            }
        })
    }

    override fun onStart() {
        super.onStart()
        getTokenClient()
        getTokenAuth()
    }

    private fun getTokenAuth() {
        val state: AuthState = mStateManager.getCurrent()

        val expiresAt: Long? = state.accessTokenExpirationTime
        if (expiresAt == null) {
            Log.d("TokenAuth", "Không có thời hạn token")
        } else if (expiresAt < System.currentTimeMillis()) {
            refreshAccessToken()
        }
        val token = state.accessToken
        val editor = sharedPreferences.edit()
        editor.putString("tokenAuth", token)
        editor.apply()
    }

    private fun refreshAccessToken() {
        performTokenRequest(
            mStateManager.getCurrent().createTokenRefreshRequest(),
            ::handleAccessTokenResponse
        )
    }

    private fun performTokenRequest(
        request: TokenRequest,
        callback: AuthorizationService.TokenResponseCallback
    ) {
        val clientAuthentication: ClientAuthentication =
            mStateManager.getCurrent().clientAuthentication
        mAuthService.performTokenRequest(request, clientAuthentication, callback)
    }

    private fun handleAccessTokenResponse(
        tokenResponse: TokenResponse?,
        authException: AuthorizationException?
    ) {
        if (tokenResponse != null) {
            mStateManager.updateAfterTokenResponse(tokenResponse, authException)
            runOnUiThread { getTokenAuth() }
        } else {
            Log.e("AccessToken", "Token response is null", authException)
            runOnUiThread {
                Toast.makeText(this, "Access token refresh failed", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun exchangeAuthorizationCode(authenticationResponse: AuthorizationResponse) {
        performTokenRequest(
            authenticationResponse.createTokenExchangeRequest(),
            ::handleCodeExchangeResponse
        )
    }

    private fun handleCodeExchangeResponse(
        tokenResponse: TokenResponse?,
        authException: AuthorizationException?
    ) {
        if (tokenResponse != null) {
            mStateManager.updateAfterTokenResponse(tokenResponse, authException)
            if (mStateManager.getCurrent().isAuthorized) {
                runOnUiThread { getTokenAuth() }
            }
        } else {
            Log.e("TokenExchange", "Token response is null", authException)
            runOnUiThread {
                Toast.makeText(this, "Token exchange failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        RetrofitClient.init(this)
        getTokenClient()
        getTokenClient()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!PlayMusicActivity.isPlayingMusic && PlayMusicActivity.musicService != null){
            PlayMusicActivity.musicService!!.stopForeground(true)
            PlayMusicActivity.musicService!!.mediaPlayer!!.release()
            PlayMusicActivity.musicService!!.mediaPlayer = null
            exitProcess(1)
        }
    }

}
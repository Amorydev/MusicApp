package com.amory.musicapp.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.amory.musicapp.MainActivity
import com.amory.musicapp.R
import com.amory.musicapp.databinding.ActivityLoginBinding
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientSecretBasic
import net.openid.appauth.ResponseTypeValues

class LoginActivity : AppCompatActivity() {
    private lateinit var service: AuthorizationService
    private lateinit var binding : ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        service = AuthorizationService(this)
        binding.btnlogin.setOnClickListener {
            loginAuth()
        }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK) {
            val ex = AuthorizationException.fromIntent(it.data!!)
            val result = AuthorizationResponse.fromIntent(it.data!!)

            if (ex != null){
            } else {
                val tokenRequest = result?.createTokenExchangeRequest()

                service.performTokenRequest(tokenRequest!!) {res, exception ->
                    if (exception != null){
                        Log.e("Github Auth", "launcher: ${exception.error}" )
                    } else {
                        val token = res?.accessToken

                        // Move to Github screen
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun loginAuth() {
        val redirectUri = Uri.parse("beatbuddy://oauth-callback")
        val authorizeUri = Uri.parse("https://auth.beatbuddy.io.vn/realms/beatbuddy/protocol/openid-connect/auth")
        val tokenUri = Uri.parse("https://auth.beatbuddy.io.vn/realms/beatbuddy/protocol/openid-connect/token")

        val config = AuthorizationServiceConfiguration(authorizeUri, tokenUri)
        val request = AuthorizationRequest
            .Builder(config, "web", ResponseTypeValues.CODE, redirectUri)
            .build()

        val intent = service.getAuthorizationRequestIntent(request)
        launcher.launch(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        service.dispose()
    }
}
package com.amory.musicapp.activities

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.ColorRes
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.amory.musicapp.MainActivity
import com.amory.musicapp.R
import com.amory.musicapp.config.AuthStateManager
import com.amory.musicapp.config.Configuration
import com.amory.musicapp.databinding.ActivityLoginBinding
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientSecretBasic
import net.openid.appauth.RegistrationRequest
import net.openid.appauth.RegistrationResponse
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.browser.BrowserMatcher
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val EXTRA_FAILED = "failed"
    private val RC_AUTH = 100

    private lateinit var mAuthService: AuthorizationService
    private lateinit var mAuthStateManager: AuthStateManager
    private lateinit var mConfiguration: Configuration

    private val mClientId = AtomicReference<String>()
    private val mAuthRequest = AtomicReference<AuthorizationRequest>()
    private val mAuthIntent = AtomicReference<CustomTabsIntent>()
    private var mAuthIntentLatch = CountDownLatch(1)
    private lateinit var mExecutor: ExecutorService

    private var mUsePendingIntents = false

    private var mBrowserMatcher: BrowserMatcher = AnyBrowserMatcher.INSTANCE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        //Tạo và quản lý các tác vụ không đồng bộ (ExecutorsService)
        mExecutor = Executors.newSingleThreadExecutor()
        mAuthStateManager = AuthStateManager.getInstance(this)
        mConfiguration = Configuration.getInstance(this)
        mAuthService = AuthorizationService(this)

        if (mAuthStateManager.getCurrent().isAuthorized) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(binding.root)

        binding.btnlogin.setOnClickListener {
            startAuth()
        }

        mExecutor.submit { this.initializeAppAuth() }
    }

    override fun onStart() {
        super.onStart()
        if (mExecutor.isShutdown) {
            mExecutor = Executors.newSingleThreadExecutor()
        }
    }

    override fun onStop() {
        super.onStop()
        mExecutor.shutdownNow()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mAuthStateManager != null){
            mAuthService.dispose()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtras(data?.extras ?: return)
            startActivity(intent)
        }
    }


    @MainThread
    fun startAuth() {
        //Thực hiện tác vụ bất đồng bộ
        mExecutor.submit { doAuth() }
    }

    private fun initializeAppAuth() {
        //Hủy bỏ auth cũ
        recreateAuthorizationService()

        if (mAuthStateManager.getCurrent().authorizationServiceConfiguration != null) {
            initializeClient()
            return
        }

        //Tạo auth từ file config (auth_config.json)
        if (mConfiguration.getDiscoveryUri() == null) {
            mAuthStateManager.replace(
                AuthState(
                    AuthorizationServiceConfiguration(
                        mConfiguration.getAuthEndpointUri()!!,
                        mConfiguration.getTokenEndpointUri()!!
                    )
                )
            )
            initializeClient()
            return
        }

        if (mConfiguration.getDiscoveryUri() == null) {
            assert(mConfiguration.getAuthEndpointUri() != null)
            assert(mConfiguration.getTokenEndpointUri() != null)

            val config = AuthorizationServiceConfiguration(
                mConfiguration.getAuthEndpointUri()!!,
                mConfiguration.getTokenEndpointUri()!!
            )
            mAuthStateManager.replace(AuthState(config))
            initializeClient()
            return
        }

        //Truy xuất url openid-configuration
        AuthorizationServiceConfiguration.fetchFromUrl(
            mConfiguration.getDiscoveryUri()!!,
            { config, _ ->
                //Nếu có lỗi -> thử truy xuất lại
                handleConfigurationRetrievalResult(config)
            },
            mConfiguration.getConnectionBuilder()
        )
    }

    @MainThread
    private fun handleConfigurationRetrievalResult(
        config: AuthorizationServiceConfiguration?
    ) {
        // Gọi lại file config để lấy lại dữ liệu
        mAuthStateManager.replace(AuthState(config!!))
        mExecutor.submit { initializeClient() }
    }

    private fun initializeClient() {
        //Nếu clientId được thiết lập tồn tại
        if (mConfiguration.getClientId() != null) {
            mClientId.set(mConfiguration.getClientId())
            runOnUiThread { initializeAuthRequest() }
            return
        }

        //Nếu clientId không tồn tại -> lấy clientId lần trước đó
        val lastResponse = mAuthStateManager.getCurrent().lastRegistrationResponse
        if (lastResponse != null) {
            mClientId.set(lastResponse.clientId)
            runOnUiThread { initializeAuthRequest() }
            return
        }

        //Thực hiện khởi tạo 1 client mới
        val registrationRequest = RegistrationRequest.Builder(
            mAuthStateManager.getCurrent().authorizationServiceConfiguration!!,
            listOf(mConfiguration.getRedirectUri())
        )
            .setTokenEndpointAuthenticationMethod(ClientSecretBasic.NAME)
            .build()

        mAuthService.performRegistrationRequest(registrationRequest) { response, ex ->
            handleRegistrationResponse(response, ex)
        }
    }

    //Cập nhật lại clientId
    @MainThread
    private fun handleRegistrationResponse(
        response: RegistrationResponse?,
        ex: AuthorizationException?
    ) {
        mAuthStateManager.updateAfterRegistration(response!!,ex)
        mClientId.set(response.clientId)
        initializeAuthRequest()
    }

    private fun doAuth() {
        mAuthIntentLatch.await()

        //Hủy bỏ xác thực
        if (mUsePendingIntents) {
            val completionIntent = Intent(this, MainActivity::class.java)
            val cancelIntent = Intent(this, LoginActivity::class.java)
            cancelIntent.putExtra(EXTRA_FAILED, true)
            cancelIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

            var flags = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags = flags or PendingIntent.FLAG_MUTABLE
            }

            mAuthService.performAuthorizationRequest(
                mAuthRequest.get(),
                PendingIntent.getActivity(this, 0, completionIntent, flags),
                PendingIntent.getActivity(this, 0, cancelIntent, flags),
                mAuthIntent.get()
            )
        } else {
            //Thực hiện xác thực
            val intent = mAuthService.getAuthorizationRequestIntent(
                mAuthRequest.get(),
                mAuthIntent.get()
            )
            startActivityForResult(intent, RC_AUTH)
        }
    }

    private fun recreateAuthorizationService() {
        //Hủy authService cũ
        if (::mAuthService.isInitialized) {
            mAuthService.dispose()
        }
        //Tạo authService mới
        mAuthService = createAuthorizationService()
        mAuthRequest.set(null)
        mAuthIntent.set(null)
    }

    //Tạo mới 1 authService
    private fun createAuthorizationService(): AuthorizationService {
        val builder = AppAuthConfiguration.Builder()
        builder.setBrowserMatcher(mBrowserMatcher)
        builder.setConnectionBuilder(mConfiguration.getConnectionBuilder())
        return AuthorizationService(this, builder.build())
    }

    @MainThread
    private fun initializeAuthRequest() {
        createAuthRequest()
        warmUpBrowser()
    }

    //Khởi chạy trình duyệt
    private fun warmUpBrowser() {
        mAuthIntentLatch = CountDownLatch(1)
        mExecutor.execute {
            val intentBuilder =
                mAuthService.createCustomTabsIntentBuilder(mAuthRequest.get().toUri())
            intentBuilder.setToolbarColor(getColorCompat(R.color.primary))
            mAuthIntent.set(intentBuilder.build())
            mAuthIntentLatch.countDown()
        }
    }

    //Tạo 1 authRequest
    private fun createAuthRequest() {
        val authRequestBuilder = AuthorizationRequest.Builder(
            mAuthStateManager.getCurrent().authorizationServiceConfiguration!!,
            mClientId.get(),
            ResponseTypeValues.CODE,
            mConfiguration.getRedirectUri()
        )

        mAuthRequest.set(authRequestBuilder.build())
    }

    @SuppressWarnings("deprecation")
    private fun getColorCompat(@ColorRes color: Int): Int {
        return getColor(color)
    }

}

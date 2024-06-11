package com.amory.musicapp.config

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.text.TextUtils
import androidx.annotation.NonNull
import com.amory.musicapp.R
import net.openid.appauth.connectivity.ConnectionBuilder
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import okio.Buffer
import okio.BufferedSource
import okio.buffer
import okio.source
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.nio.charset.Charset

class Configuration(private val context: Context) {

    private val resources: Resources = context.resources

    private var configJson: JSONObject? = null
    private var configHash: String? = null

    private var clientId: String? = null
    private var redirectUri: Uri? = null
    private var endSessionRedirectUri: Uri? = null
    private var discoveryUri: Uri? = null
    private var authEndpointUri: Uri? = null
    private var tokenEndpointUri: Uri? = null
    private var endSessionEndpoint: Uri? = null
    private var registrationEndpointUri: Uri? = null
    private var userInfoEndpointUri: Uri? = null
    private var httpsRequired: Boolean = false

    init {
        readConfiguration()
    }

    fun getClientId(): String? {
        return clientId
    }


    fun getRedirectUri(): Uri {
        return redirectUri!!
    }

    fun getDiscoveryUri(): Uri? {
        return discoveryUri
    }

    fun getEndSessionRedirectUri(): Uri? {
        return endSessionRedirectUri
    }

    fun getAuthEndpointUri(): Uri? {
        return authEndpointUri
    }

    fun getTokenEndpointUri(): Uri? {
        return tokenEndpointUri
    }

    fun getEndSessionEndpoint(): Uri? {
        return endSessionEndpoint
    }

    fun getRegistrationEndpointUri(): Uri? {
        return registrationEndpointUri
    }

    fun getUserInfoEndpointUri(): Uri? {
        return userInfoEndpointUri
    }


    fun getConnectionBuilder(): ConnectionBuilder {
        return DefaultConnectionBuilder.INSTANCE
    }

    //Đọc dữ liệu từ file json
    private fun readConfiguration() {
        val configSource: BufferedSource =
            resources.openRawResource(R.raw.auth_config).source().buffer()
        val configData = Buffer()
        configSource.readAll(configData)
        configJson = JSONObject(configData.readString(Charset.forName("UTF-8")))
        configHash = configData.sha256().base64()
        clientId = getConfigString("client_id")
        redirectUri = getRequiredConfigUri("redirect_uri")
        endSessionRedirectUri = getRequiredConfigUri("end_session_redirect_uri")


        if (getConfigString("discovery_uri") == null) {
            authEndpointUri = getRequiredConfigWebUri("authorization_endpoint_uri")
            tokenEndpointUri = getRequiredConfigWebUri("token_endpoint_uri")
            userInfoEndpointUri = getRequiredConfigWebUri("user_info_endpoint_uri")
            endSessionEndpoint = getRequiredConfigUri("end_session_endpoint")
            if (clientId == null) {
                registrationEndpointUri = getRequiredConfigWebUri("registration_endpoint_uri")
            }
        } else {
            discoveryUri = getRequiredConfigWebUri("discovery_uri")
        }

        httpsRequired = configJson?.optBoolean("https_required", true) ?: true
    }

    private fun getConfigString(propName: String): String? {
        var value = configJson?.optString(propName) ?: return null
        value = value.trim()
        return if (TextUtils.isEmpty(value)) {
            null
        } else value
    }

    private fun getRequiredConfigString(propName: String): String? {
        return getConfigString(propName)
    }

    private fun getRequiredConfigUri(propName: String): Uri {
        val uriStr = getRequiredConfigString(propName)
        return Uri.parse(uriStr)
    }


    private fun getRequiredConfigWebUri(propName: String): Uri {
        return getRequiredConfigUri(propName)
    }


    companion object {
        private var sInstance: WeakReference<Configuration> = WeakReference(null)

        @JvmStatic
        fun getInstance(context: Context): Configuration {
            var config = sInstance.get()
            if (config == null) {
                config = Configuration(context)
                sInstance = WeakReference(config)
            }
            return config
        }
    }
}
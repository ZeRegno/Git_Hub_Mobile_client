package com.example.githubmobileclient2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthActivity : AppCompatActivity() {

    private lateinit var authService: AuthorizationService
    private lateinit var authState: AuthState
    private lateinit var userManager: UserManager
    private val clientId = "Ov23lirnWKr8997rxaNk"
    private val clientSecret = "87a551e2fc4de1b317a77c82247e3860db84a424"
    private val redirectUri = Uri.parse("mygithubsuka://callback")
    private val startForResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                Log.d("AuthActivity", "Raw Response URI: $uri")
                if (uri != null) {
                    if (uri.toString().startsWith(redirectUri.toString())) {
                        val code = uri.getQueryParameter("code")
                        if (code != null) {
                            Log.d("AuthActivity", "Authorization code: $code")
                            if (authState.authorizationServiceConfiguration != null && authState.accessToken == null) {
                                handleAuthorizationCode(code)
                            } else {
                                navigateToRepositoryList()
                            }
                        } else {
                            Log.e("AuthActivity", "Authorization code is null")
                        }
                    } else {
                        Log.e("AuthActivity", "Redirect URI mismatch: $uri")
                    }
                } else {
                    Log.e("AuthActivity", "Received URI is null")
                }
            } else {
                Log.e("AuthActivity", "Invalid result code: ${result.resultCode}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        authService = AuthorizationService(this)
        userManager = UserManager(this)
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("https://github.com/login/oauth/authorize"),
            Uri.parse("https://github.com/login/oauth/access_token")
        )

        authState = AuthState(serviceConfig)

        val dataUri = intent?.data
        if (dataUri != null && dataUri.toString().startsWith(redirectUri.toString())) {
            val code = dataUri.getQueryParameter("code")
            if (code != null) {
                handleAuthorizationCode(code)
                return
            }
        }

        if (authState.accessToken != null) {
            navigateToRepositoryList()
        } else {
            val authRequest = AuthorizationRequest.Builder(
                serviceConfig,
                clientId,
                ResponseTypeValues.CODE,
                redirectUri
            ).setScope("repo")
                .build()

            val customTabsIntent = CustomTabsIntent.Builder().apply {}.build()
            val authIntent = authService.getAuthorizationRequestIntent(authRequest, customTabsIntent)
            startForResult.launch(authIntent)
        }
    }

    private fun handleAuthorizationCode(code: String) {
        lifecycleScope.launch {
            try {
                val config = authState.authorizationServiceConfiguration
                if (config != null) {
                    val tokenRequest = TokenRequest.Builder(
                        config,
                        clientId
                    )
                        .setAuthorizationCode(code)
                        .setRedirectUri(redirectUri)
                        .build()

                    exchangeCodeForToken(tokenRequest)
                }
            } catch (e: Exception) {
                Log.e("AuthActivity", "Token request preparation failed", e)
            }
        }
    }

    private suspend fun exchangeCodeForToken(tokenRequest: TokenRequest) = withContext(Dispatchers.IO) {
        val clientAuth = ClientSecretBasic(clientSecret)

        try {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val httpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val tokenResponse = suspendCoroutine<TokenResponse> { continuation ->
                authService.performTokenRequest(tokenRequest, clientAuth) { response, exception ->
                    if (exception != null) {
                        continuation.resumeWithException(exception)
                    } else if (response != null) {
                        continuation.resume(response)
                    } else {
                        continuation.resumeWithException(IllegalStateException("Token exchange failed"))
                    }
                }
            }

            authState.update(tokenResponse, null)
            val accessToken = tokenResponse.accessToken
            Log.d("AuthActivity", "Access token received: $accessToken")

            val userInfo = fetchGitHubUserInfo(accessToken!!)
            userManager.saveUserData(userInfo.login, accessToken)

            navigateToRepositoryList(accessToken)

        } catch (e: Exception) {
            Log.e("AuthActivity", "Error exchanging code for token", e)
        }
    }

    private val apiService: ApiService = ApiService.create()


    private suspend fun fetchGitHubUserInfo(accessToken: String): User = withContext(Dispatchers.IO) {
        try {
            val userInfoResponse = apiService.getUserInfo("Bearer $accessToken")
            return@withContext userInfoResponse
        } catch (e: Exception) {
            Log.e("AuthActivity", "Error fetching user info", e)
            throw e
        }
    }

    private fun navigateToRepositoryList(accessToken: String? = null) {
        val intent = Intent(this@AuthActivity, RepositoryListActivity::class.java)
        intent.putExtra("auth_token", accessToken)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        authService.dispose()
        super.onDestroy()
    }
}

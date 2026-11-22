package com.example.data.remote

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val TAG = "AppCheckInit"

private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

@Volatile
private var appCheckConfigured = false

fun configureAppCheck(context: Context, intent: Intent?) {
    val appContext = context.applicationContext ?: context
    try {
        if (FirebaseApp.getApps(appContext).isEmpty()) {
            FirebaseApp.initializeApp(appContext)
        }

        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)

            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = FirebaseApp.getInstance().persistenceKey
                val prefs = appContext.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                // Synchronous commit() ensures the secret is written before DebugAppCheckProviderFactory reads it.
                prefs.edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
                Log.d(TAG, "App Check debug secret configured successfully.")
            } else {
                Log.d(TAG, "DebugAppCheckProviderFactory initializing without static intent token.")
            }

            if (!appCheckConfigured || !debugToken.isNullOrEmpty()) {
                FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                appCheckConfigured = true
                Log.d(TAG, "DebugAppCheckProviderFactory installed.")
            }
        } else {
            if (appCheckConfigured) return
            // Direct R.string reference guarantees R8 resource shrinker retains recaptcha_site_key.
            val directSiteKey = runCatching { appContext.getString(R.string.recaptcha_site_key) }
                .getOrDefault("")
            val fallbackResId = if (directSiteKey.isBlank()) {
                appContext.resources.getIdentifier(
                    RECAPTCHA_SITE_KEY_RES,
                    "string",
                    appContext.packageName
                )
            } else {
                0
            }
            val siteKey = when {
                directSiteKey.isNotBlank() -> directSiteKey
                fallbackResId != 0 -> appContext.getString(fallbackResId)
                else -> ""
            }

            if (siteKey.isNotBlank()) {
                FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                    RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                )
                appCheckConfigured = true
                Log.i(TAG, "RecaptchaAppCheckProviderFactory installed cleanly.")
            } else {
                Log.e(TAG, "recaptcha_site_key resource string is missing or blank.")
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
    }
}

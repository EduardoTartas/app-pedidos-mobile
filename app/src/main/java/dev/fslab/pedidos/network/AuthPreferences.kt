package dev.fslab.pedidos.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object AuthPreferences {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_DATA = "user_data"

    private fun getPrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveRefreshToken(context: Context, token: String) {
        getPrefs(context).edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun getRefreshToken(context: Context): String? {
        return getPrefs(context).getString(KEY_REFRESH_TOKEN, null)
    }

    fun saveUser(context: Context, userJson: String) {
        getPrefs(context).edit().putString(KEY_USER_DATA, userJson).apply()
    }

    fun getUser(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_DATA, null)
    }

    fun clear(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_DATA)
            .apply()
    }
}

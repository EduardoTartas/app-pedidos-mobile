package dev.fslab.pedidos.network

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object AuthPreferences {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_DATA = "user_data"

    private fun getPrefs(context: Context): android.content.SharedPreferences {
        return try {
            createPrefs(context)
        } catch (e: Exception) {
            // Se falhar (corrupção de KeyStore), tentamos limpar e recriar
            Log.e("AuthPreferences", "Erro ao inicializar EncryptedSharedPreferences, limpando...", e)
            try {
                // Tenta apagar o arquivo de preferências
                val sharedPrefsFile = java.io.File(context.filesDir.parent, "shared_prefs/${PREFS_NAME}.xml")
                if (sharedPrefsFile.exists()) {
                    sharedPrefsFile.delete()
                }
                createPrefs(context)
            } catch (e2: Exception) {
                // Fallback final: usar SharedPreferences comum se o criptografado falhar de vez
                context.getSharedPreferences(PREFS_NAME + "_fallback", Context.MODE_PRIVATE)
            }
        }
    }

    private fun createPrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAccessToken(context: Context, token: String) {
        getPrefs(context).edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(context: Context): String? {
        return getPrefs(context).getString(KEY_ACCESS_TOKEN, null)
    }

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
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_DATA)
            .apply()
    }
}

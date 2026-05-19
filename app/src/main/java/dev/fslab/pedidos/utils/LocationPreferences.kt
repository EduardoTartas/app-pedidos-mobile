package dev.fslab.pedidos.utils

import android.content.Context

/**
 * LocationPreferences - Persiste o ID do endereço selecionado pelo usuário localmente.
 */
object LocationPreferences {
    private const val PREFS_NAME = "location_prefs"
    private const val KEY_SELECTED_ID = "selected_endereco_id"

    private fun getPrefs(context: Context) = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSelectedId(context: Context, id: String?) {
        getPrefs(context).edit().putString(KEY_SELECTED_ID, id).apply()
    }

    fun getSelectedId(context: Context): String? {
        return getPrefs(context).getString(KEY_SELECTED_ID, null)
    }

    fun clear(context: Context) {
        getPrefs(context).edit().remove(KEY_SELECTED_ID).apply()
    }
}

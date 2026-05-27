package com.cletaeats.app.domain.datamode

import android.content.Context

class DataModeManager(
    context: Context
) {
    private val prefs = context.getSharedPreferences(
        "cletaeats_data_mode",
        Context.MODE_PRIVATE
    )

    fun saveMode(mode: DataMode) {
        prefs.edit()
            .putString("mode", mode.code)
            .apply()
    }

    fun getMode(): DataMode {
        return DataMode.fromCode(
            prefs.getString("mode", DataMode.API.code)
        )
    }

    fun clearMode() {
        prefs.edit()
            .remove("mode")
            .apply()
    }
}
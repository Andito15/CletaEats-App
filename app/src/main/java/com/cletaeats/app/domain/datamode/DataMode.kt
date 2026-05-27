package com.cletaeats.app.domain.datamode

enum class DataMode(
    val code: String,
    val title: String,
    val description: String
) {
    API(
        code = "API",
        title = "API remota",
        description = "Consume datos desde el backend."
    ),

    LOCAL(
        code = "LOCAL",
        title = "SQLite local",
        description = "Trabaja con datos guardados en el dispositivo."
    ),

    CLOUD(
        code = "CLOUD",
        title = "Cloud",
        description = "Trabaja con datos sincronizados en la nube."
    );

    companion object {
        fun fromCode(code: String?): DataMode {
            return entries.firstOrNull { it.code == code } ?: API
        }
    }
}
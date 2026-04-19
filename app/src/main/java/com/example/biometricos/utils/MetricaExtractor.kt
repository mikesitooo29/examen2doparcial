package com.example.biometricos.utils

data class MetricasExtraidas(
    val km: Double,
    val minutos: Int
)

object MetricaExtractor {

    fun extraer(texto: String): MetricasExtraidas {
        val km = extraerKm(texto)
        val minutos = extraerMinutos(texto)
        return MetricasExtraidas(km, minutos)
    }

    private fun extraerKm(texto: String): Double {
        // Patrones: "5 km", "5.2km", "5 kilómetros", "cinco kilómetros"
        val regexNum = Regex(
            """(\d+(?:[.,]\d+)?)\s*(?:km|kilómetros?|kilometros?)""",
            RegexOption.IGNORE_CASE
        )
        regexNum.find(texto)?.groupValues?.get(1)?.let { v ->
            return v.replace(',', '.').toDoubleOrNull() ?: 0.0
        }
        return 0.0
    }

    private fun extraerMinutos(texto: String): Int {
        // Patrones: "45 min", "45 minutos", "1 hora", "1:30", "una hora"
        val regexMin = Regex(
            """(\d+)\s*(?:min(?:utos?)?)""",
            RegexOption.IGNORE_CASE
        )
        regexMin.find(texto)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }

        val regexHora = Regex(
            """(\d+)\s*hora?s?""",
            RegexOption.IGNORE_CASE
        )
        regexHora.find(texto)?.groupValues?.get(1)?.toIntOrNull()?.let { return it * 60 }

        return 0
    }
}

package com.example.biometricos.dominios

data class EntradaBitacora(
    val id: String = "",
    val texto: String = "",
    val km: Double = 0.0,
    val minutos: Int = 0,
    val fecha: String = "",
    val hora: String = "",
    // Campos de display derivados
    val titulo: String = "",
    val resumen: String = ""
)

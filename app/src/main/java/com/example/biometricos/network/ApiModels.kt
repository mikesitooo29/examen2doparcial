package com.example.biometricos.network

import com.google.gson.annotations.SerializedName

data class EntrenamientoRequest(
    val texto: String,
    val km: Double,
    val minutos: Int
)

data class EntrenamientoResponse(
    @SerializedName("_id")    val id: String,
    val texto: String,
    val km: Double,
    val minutos: Int,
    val fecha: String
)

package com.example.biometricos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("training")
    suspend fun guardarEntrenamiento(@Body body: EntrenamientoRequest): Response<EntrenamientoResponse>

    @GET("trainings")
    suspend fun obtenerEntrenamientos(): Response<List<EntrenamientoResponse>>
}

package com.example.biometricos.activities

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.biometricos.components.LogEntryCard
import com.example.biometricos.components.WelcomeCard
import com.example.biometricos.dominios.EntradaBitacora
import com.example.biometricos.network.RetrofitClient
import com.example.biometricos.ui.theme.SpotifyGreen
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp

@Composable
fun BitacoraListActivity(recargar: Int = 0) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var entradas by remember { mutableStateOf<List<EntradaBitacora>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var busquedaActiva by remember { mutableStateOf(false) }
    var textoBusqueda by remember { mutableStateOf("") }

    val entradasFiltradas = remember(entradas, textoBusqueda) {
        if (textoBusqueda.isBlank()) entradas
        else entradas.filter {
            it.titulo.contains(textoBusqueda, ignoreCase = true) ||
                    it.resumen.contains(textoBusqueda, ignoreCase = true) ||
                    it.fecha.contains(textoBusqueda, ignoreCase = true)
        }
    }

    fun hayConexion(): Boolean {
        val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun cargar() {
        if (!hayConexion()) {
            Toast.makeText(context, "Sin conexión a internet", Toast.LENGTH_SHORT).show()
            return
        }
        cargando = true
        scope.launch {
            try {
                val resp = RetrofitClient.api.obtenerEntrenamientos()
                if (resp.isSuccessful) {
                    entradas = (resp.body() ?: emptyList()).map { e ->
                        val fechaCorta = e.fecha.take(10)
                        EntradaBitacora(
                            id = e.id,
                            texto = e.texto,
                            km = e.km,
                            minutos = e.minutos,
                            fecha = fechaCorta,
                            hora = "",
                            titulo = "🏃 ${e.km} km · ${e.minutos} min",
                            resumen = e.texto.take(80)
                        )
                    }.reversed()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(recargar) { cargar() }

    if (cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SpotifyGreen)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            val totalKm = entradas.sumOf { it.km }
            val totalMin = entradas.sumOf { it.minutos }
            val sesiones = entradas.size

            WelcomeCard(
                nombreUsuario = "Mike",
                activityGraphContent = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%.1f", totalKm),
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = SpotifyGreen
                            )
                            Text(
                                text = "km totales",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalMin",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = SpotifyGreen
                            )
                            Text(
                                text = "minutos",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$sesiones",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = SpotifyGreen
                            )
                            Text(
                                text = "sesiones",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            )
        }

        // Barra de búsqueda
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mis entrenamientos",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = {
                    busquedaActiva = !busquedaActiva
                    if (!busquedaActiva) textoBusqueda = ""
                }) {
                    Icon(
                        imageVector = if (busquedaActiva) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = SpotifyGreen
                    )
                }
            }
        }

        item {
            AnimatedVisibility(visible = busquedaActiva) {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    placeholder = { Text("Buscar por fecha, km, texto...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = SpotifyGreen)
                    },
                    trailingIcon = {
                        if (textoBusqueda.isNotBlank()) {
                            IconButton(onClick = { textoBusqueda = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpotifyGreen,
                        cursorColor = SpotifyGreen
                    )
                )
            }
        }

        if (entradasFiltradas.isEmpty()) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (textoBusqueda.isBlank())
                            "No hay entrenamientos aún.\nUsa la pestaña Registrar."
                        else
                            "No se encontraron resultados para \"$textoBusqueda\"",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(entradasFiltradas) { entrada ->
                LogEntryCard(entrada = entrada)
            }
        }
    }
}
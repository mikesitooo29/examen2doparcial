package com.example.biometricos.activities

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.biometricos.network.EntrenamientoResponse
import com.example.biometricos.network.RetrofitClient
import com.example.biometricos.ui.theme.SpotifyGreen
import com.example.biometricos.ui.theme.SpotifyGreenDark
import com.example.biometricos.ui.theme.NegritoFondo
import com.example.biometricos.ui.theme.GrisCard
import kotlinx.coroutines.launch

@Composable
fun PerfilActivity() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var entrenamientos by remember { mutableStateOf<List<EntrenamientoResponse>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }

    fun hayConexion(): Boolean {
        val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    LaunchedEffect(Unit) {
        if (!hayConexion()) return@LaunchedEffect
        cargando = true
        scope.launch {
            try {
                val resp = RetrofitClient.api.obtenerEntrenamientos()
                if (resp.isSuccessful) entrenamientos = resp.body() ?: emptyList()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar perfil", Toast.LENGTH_SHORT).show()
            } finally {
                cargando = false
            }
        }
    }

    val totalKm = entrenamientos.sumOf { it.km }
    val totalMin = entrenamientos.sumOf { it.minutos }
    val sesiones = entrenamientos.size
    val mejorSesion = entrenamientos.maxByOrNull { it.km }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NegritoFondo),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar y nombre
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(SpotifyGreenDark.copy(alpha = 0.3f), NegritoFondo)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(SpotifyGreen)
                            .border(3.dp, SpotifyGreenDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Mike",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Atleta de alto rendimiento",
                        fontSize = 14.sp,
                        color = SpotifyGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Stats
        item {
            Text(
                text = "Estadísticas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        item {
            if (cargando) {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SpotifyGreen)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatItem(
                        valor = String.format("%.1f", totalKm),
                        label = "km totales",
                        icono = Icons.Default.DirectionsRun,
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        valor = "$totalMin",
                        label = "minutos",
                        icono = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        valor = "$sesiones",
                        label = "sesiones",
                        icono = Icons.Default.FitnessCenter,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Mejor sesión
        if (mejorSesion != null) {
            item {
                Text(
                    text = "Mejor sesión",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GrisCard)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SpotifyGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏆", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "${mejorSesion.km} km · ${mejorSesion.minutos} min",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = mejorSesion.fecha.take(10),
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                            Text(
                                text = mejorSesion.texto.take(60),
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Logros
        item {
            Text(
                text = "Logros",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LogroItem(
                    emoji = "🎯",
                    titulo = "Primera sesión",
                    descripcion = "Registraste tu primer entrenamiento",
                    desbloqueado = sesiones >= 1
                )
                LogroItem(
                    emoji = "🔥",
                    titulo = "En racha",
                    descripcion = "3 sesiones registradas",
                    desbloqueado = sesiones >= 3
                )
                LogroItem(
                    emoji = "💪",
                    titulo = "Resistencia",
                    descripcion = "Más de 50 km totales",
                    desbloqueado = totalKm >= 50
                )
                LogroItem(
                    emoji = "🏅",
                    titulo = "Centurión",
                    descripcion = "100 km totales",
                    desbloqueado = totalKm >= 100
                )
                LogroItem(
                    emoji = "⚡",
                    titulo = "Maratonista",
                    descripcion = "Una sesión de más de 42 km",
                    desbloqueado = (mejorSesion?.km ?: 0.0) >= 42
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    valor: String,
    label: String,
    icono: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GrisCard)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = SpotifyGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = valor,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LogroItem(
    emoji: String,
    titulo: String,
    descripcion: String,
    desbloqueado: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (desbloqueado) SpotifyGreen.copy(alpha = 0.15f) else GrisCard
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (desbloqueado) emoji else "🔒",
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = titulo,
                    fontWeight = FontWeight.Bold,
                    color = if (desbloqueado) Color.White else Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = descripcion,
                    color = if (desbloqueado) SpotifyGreen else Color.Gray,
                    fontSize = 12.sp
                )
            }
            if (desbloqueado) {
                Spacer(modifier = Modifier.weight(1f))
                Text("✓", color = SpotifyGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}
package com.example.biometricos.activities

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.biometricos.network.EntrenamientoResponse
import com.example.biometricos.network.RetrofitClient
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

@Composable
fun GraficaActivity() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var entrenamientos by remember { mutableStateOf<List<EntrenamientoResponse>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var modoGrafica by remember { mutableStateOf("km") } // "km" o "minutos"

    val colorKm    = MaterialTheme.colorScheme.primary.toArgb()
    val colorMin   = MaterialTheme.colorScheme.tertiary.toArgb()
    val colorTexto = MaterialTheme.colorScheme.onBackground.toArgb()

    fun hayConexion(): Boolean {
        val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun cargarDatos() {
        if (!hayConexion()) {
            Toast.makeText(context, "Sin conexión a internet", Toast.LENGTH_LONG).show()
            return
        }
        cargando = true
        scope.launch {
            try {
                val resp = RetrofitClient.api.obtenerEntrenamientos()
                if (resp.isSuccessful) {
                    entrenamientos = resp.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Error al cargar datos: ${resp.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Progreso semanal",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        // Selector KM / Minutos
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = modoGrafica == "km",
                onClick = { modoGrafica = "km" },
                label = { Text("Kilómetros") }
            )
            FilterChip(
                selected = modoGrafica == "minutos",
                onClick = { modoGrafica = "minutos" },
                label = { Text("Minutos") }
            )
        }

        if (cargando) {
            Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (entrenamientos.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Aún no hay entrenamientos registrados.\nDicta tu primer sesión.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            // Gráfica MPAndroidChart real
            Card(
                modifier = Modifier.fillMaxWidth().height(320.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    factory = { ctx ->
                        LineChart(ctx).apply {
                            description.isEnabled = false
                            legend.textColor = colorTexto
                            setTouchEnabled(true)
                            isDragEnabled = true
                            setScaleEnabled(true)
                            xAxis.apply {
                                position = XAxis.XAxisPosition.BOTTOM
                                textColor = colorTexto
                                granularity = 1f
                                setDrawGridLines(false)
                            }
                            axisLeft.textColor = colorTexto
                            axisRight.isEnabled = false
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            setNoDataText("Cargando datos…")
                        }
                    },
                    update = { chart ->
                        val etiquetas = entrenamientos.mapIndexed { i, e ->
                            e.fecha.take(10).removePrefix("2024-").removePrefix("2025-").removePrefix("2026-")
                        }

                        val entries = entrenamientos.mapIndexed { i, e ->
                            val valor = if (modoGrafica == "km") e.km.toFloat() else e.minutos.toFloat()
                            Entry(i.toFloat(), valor)
                        }

                        val color = if (modoGrafica == "km") colorKm else colorMin
                        val label = if (modoGrafica == "km") "Kilómetros" else "Minutos"

                        val dataset = LineDataSet(entries, label).apply {
                            this.color = color
                            valueTextColor = colorTexto
                            lineWidth = 2.5f
                            circleRadius = 5f
                            setCircleColor(color)
                            setDrawCircleHole(true)
                            circleHoleRadius = 2.5f
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                            setDrawValues(true)
                            valueTextSize = 10f
                        }

                        chart.xAxis.valueFormatter = IndexAxisValueFormatter(etiquetas)
                        chart.data = LineData(dataset)
                        chart.animateX(800)
                        chart.invalidate()
                    }
                )
            }
        }

        // Resumen stats
        if (entrenamientos.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    label = "Total km",
                    valor = String.format("%.1f", entrenamientos.sumOf { it.km }),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Total min",
                    valor = "${entrenamientos.sumOf { it.minutos }}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Sesiones",
                    valor = "${entrenamientos.size}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Button(onClick = { cargarDatos() }, modifier = Modifier.fillMaxWidth()) {
            Text("Actualizar datos")
        }
    }
}

@Composable
private fun StatCard(label: String, valor: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(valor, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        }
    }
}

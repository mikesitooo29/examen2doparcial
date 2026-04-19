package com.example.biometricos.activitys

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.biometricos.network.EntrenamientoRequest
import com.example.biometricos.network.RetrofitClient
import com.example.biometricos.utils.MetricaExtractor
import kotlinx.coroutines.launch

@Composable
fun VoiceEntryActivity(onEntradaGuardada: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var grabando by remember { mutableStateOf(false) }
    var textoTranscrito by remember { mutableStateOf("") }
    var textoParciable by remember { mutableStateOf("") }
    var kmTexto by remember { mutableStateOf("") }
    var minutosTexto by remember { mutableStateOf("") }
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    val pulsoScale by animateFloatAsState(
        targetValue = if (grabando) 1.15f else 1f,
        animationSpec = tween(600),
        label = "pulso"
    )

    DisposableEffect(Unit) {
        onDispose { speechRecognizer.destroy() }
    }

    fun iniciarGrabacion() {
        textoTranscrito = ""
        textoParciable = ""
        grabando = true

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() { grabando = false }

            override fun onPartialResults(r: Bundle?) {
                val parcial = r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                textoParciable = parcial?.firstOrNull() ?: ""
            }

            override fun onResults(r: Bundle?) {
                val resultados = r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                textoTranscrito = resultados?.firstOrNull() ?: ""
                grabando = false
                if (textoTranscrito.isNotBlank()) {
                    val metricas = MetricaExtractor.extraer(textoTranscrito)
                    kmTexto = if (metricas.km > 0) metricas.km.toString() else ""
                    minutosTexto = if (metricas.minutos > 0) metricas.minutos.toString() else ""
                    mostrarConfirmacion = true
                }
            }

            override fun onError(error: Int) {
                grabando = false
                val msg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No se detectó voz, intenta de nuevo"
                    SpeechRecognizer.ERROR_NETWORK   -> "Error de red en reconocimiento de voz"
                    else -> "Error al grabar ($error)"
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }

            override fun onEvent(t: Int, p: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer.startListening(intent)
    }

    fun detenerGrabacion() {
        speechRecognizer.stopListening()
        grabando = false
    }

    fun hayConexion(): Boolean {
        val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun guardarEntrenamiento() {
        if (!hayConexion()) {
            Toast.makeText(context, "Sin conexión a internet. Conéctate e intenta de nuevo.", Toast.LENGTH_LONG).show()
            return
        }
        val km = kmTexto.toDoubleOrNull() ?: 0.0
        val min = minutosTexto.toIntOrNull() ?: 0
        if (textoTranscrito.isBlank() || (km == 0.0 && min == 0)) {
            Toast.makeText(context, "Completa km o minutos antes de guardar", Toast.LENGTH_SHORT).show()
            return
        }
        guardando = true
        scope.launch {
            try {
                val resp = RetrofitClient.api.guardarEntrenamiento(
                    EntrenamientoRequest(texto = textoTranscrito, km = km, minutos = min)
                )
                if (resp.isSuccessful) {
                    Toast.makeText(context, "✅ Entrenamiento guardado", Toast.LENGTH_SHORT).show()
                    mostrarConfirmacion = false
                    textoTranscrito = ""
                    onEntradaGuardada()
                } else {
                    Toast.makeText(context, "Error del servidor: ${resp.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                guardando = false
            }
        }
    }

    // ── UI ──────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            Text(
                "Registrar entrenamiento",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            // Botón de micrófono grande (RNF02 — manos libres)
            FilledTonalButton(
                onClick = { if (grabando) detenerGrabacion() else iniciarGrabacion() },
                shape = CircleShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (grabando)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulsoScale)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (grabando) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Micrófono",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(if (grabando) "Detener" else "Dictar", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Texto parcial en tiempo real
            if (grabando || textoParciable.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (grabando && textoParciable.isNotBlank()) textoParciable else "Escuchando…",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Diálogo de confirmación y edición (RF03)
            AnimatedVisibility(visible = mostrarConfirmacion) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Confirma tu entrenamiento",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            textoTranscrito,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        OutlinedTextField(
                            value = kmTexto,
                            onValueChange = { kmTexto = it },
                            label = { Text("Kilómetros") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = minutosTexto,
                            onValueChange = { minutosTexto = it },
                            label = { Text("Minutos") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { mostrarConfirmacion = false },
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancelar") }

                            Button(
                                onClick = { guardarEntrenamiento() },
                                enabled = !guardando,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (guardando) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Guardar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

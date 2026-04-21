package com.example.biometricos.activities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.biometricos.components.HomeHeaders

@Composable
fun HomeActivity() {
    val context = LocalContext.current
    var tabSeleccionado by remember { mutableStateOf(0) }
    var recargarBitacora by remember { mutableStateOf(0) }

    Scaffold(
        topBar = { HomeHeaders() },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tabSeleccionado == 0,
                    onClick = { tabSeleccionado = 0 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Bitácora") },
                    label = { Text("Bitácora") }
                )
                NavigationBarItem(
                    selected = tabSeleccionado == 1,
                    onClick = { tabSeleccionado = 1 },
                    icon = { Icon(Icons.Default.Mic, contentDescription = "Registrar") },
                    label = { Text("Registrar") }
                )
                NavigationBarItem(
                    selected = tabSeleccionado == 2,
                    onClick = { tabSeleccionado = 2 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Progreso") },
                    label = { Text("Progreso") }
                )
                NavigationBarItem(
                    selected = tabSeleccionado == 3,
                    onClick = { tabSeleccionado = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (tabSeleccionado) {
                0 -> BitacoraListActivity(recargar = recargarBitacora)
                1 -> VoiceEntryActivity(onEntradaGuardada = {
                    recargarBitacora++
                    tabSeleccionado = 0
                })
                2 -> GraficaActivity()
                3 -> PerfilActivity()
            }
        }
    }
}
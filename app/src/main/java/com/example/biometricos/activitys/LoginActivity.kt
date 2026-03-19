package com.example.biometricos.activitys

import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun LoginActivity(onAutenticacionExitosa: ()-> Unit) {

    val context = LocalContext.current
    val activity =  context as? FragmentActivity

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bitacora",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (activity != null){
                    LanzarBiometrica(activity, onAutenticacionExitosa)
                }else{
                    Toast.makeText(context, "No se pudo iniciar la autenticación", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text(text = "Iniciar sesión con biometría")
        }

    }
}

fun LanzarBiometrica(activity: FragmentActivity, onAutenticacionExitosa: () -> Unit){

    val executor = ContextCompat.getMainExecutor(activity)

    val biometricPrompt = BiometricPrompt(activity, executor,
        object: BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(activity, "Error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(activity, "Desbloqueo exitoso", Toast.LENGTH_SHORT).show()
                onAutenticacionExitosa()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(activity, "Huella no reconocida, intenta de nuevo", Toast.LENGTH_SHORT).show()
            }
        })

    val promptInfo= BiometricPrompt.PromptInfo.Builder()
        .setTitle("Acceso a la bitacora")
        .setSubtitle("Autenticacion reqeurida")
        .setDescription("Usa tu huella para acceder a la bitacora")
        .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        .build()

    val biometricManager = BiometricManager.from(activity)
    if (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)== BiometricManager.BIOMETRIC_SUCCESS){
        biometricPrompt.authenticate(promptInfo)
    }else{
        Toast.makeText(activity, "Huella no configurada en el dispositivo", Toast.LENGTH_SHORT).show()
    }
}
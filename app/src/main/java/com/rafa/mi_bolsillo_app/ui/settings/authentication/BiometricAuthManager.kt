package com.rafa.mi_bolsillo_app.ui.settings.authentication

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

fun launchBiometricAuth(
    activity: AppCompatActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricManager = BiometricManager.from(activity)

    // Comprobar si el dispositivo es compatible
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
        }
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            onError("Este dispositivo no tiene sensor de huella dactilar.")
            return
        }
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            onError("El sensor de huella dactilar no está disponible en este momento.")
            return
        }
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            onError("No tienes ninguna huella o PIN configurado en tu dispositivo.")
            return
        }
        else -> {
            onError("Error de biometría desconocido.")
            return
        }
    }

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Confirmación requerida")
        .setSubtitle("Confirma tu identidad para cambiar este ajuste")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        .build()

    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Autenticación exitosa
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                    onError("Error de autenticación: $errString")
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // La huella/PIN no es correcta
                // El diálogo ya muestra un mensaje, así que no es necesario hacer nada aquí.
            }
        })

    biometricPrompt.authenticate(promptInfo)
}
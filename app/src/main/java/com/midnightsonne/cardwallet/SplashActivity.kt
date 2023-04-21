@file:Suppress("DEPRECATION")

package com.midnightsonne.cardwallet

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private var failedAttempts = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_key), Context.MODE_PRIVATE)
        val isBiometricEnabled = sharedPreferences.getBoolean("is_biometric_enabled", false)
        val userHasDecided = sharedPreferences.getBoolean("user_has_decided", false)

        if (!userHasDecided) {
            askUserToEnableBiometric()
        } else if (isBiometricEnabled && isBiometricSupported(this)) {
            authenticateUser()
        } else {
            openMainActivity()
        }

        val fingerprintImage: ImageView = findViewById(R.id.fingerprint_image)
        fingerprintImage.setOnClickListener {
            authenticateUser()
        }
    }

    private fun askUserToEnableBiometric() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enable Biometric Authentication")
        builder.setMessage("Would you like to enable biometric authentication for enhanced security?\n\nThis choice cannot be changed later.")
        builder.setPositiveButton("Yes") { _, _ ->
            val sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_key), Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("is_biometric_enabled", true).apply()
            sharedPreferences.edit().putBoolean("user_has_decided", true).apply()
            authenticateUser()
        }
        builder.setNegativeButton("No") { _, _ ->
            val sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_key), Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("is_biometric_enabled", false).apply()
            sharedPreferences.edit().putBoolean("user_has_decided", true).apply()
            openMainActivity()
        }
        builder.setCancelable(false)
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun authenticateUser() {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_LOCKOUT) {
                    finish()
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                openMainActivity()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                failedAttempts++
                if (failedAttempts >= 5) {
                    finish()
                } else {
                    Toast.makeText(this@SplashActivity, "Authentication failed. Attempt: $failedAttempts", Toast.LENGTH_SHORT).show()
                }
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("Biometric Authentication").setSubtitle("Authenticate to continue").setDeviceCredentialAllowed(true).build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun isBiometricSupported(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

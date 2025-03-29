// SplashActivity.kt
package com.example.watchmap

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity

class SplashActivity : ComponentActivity() {
    private val SPLASH_DURATION = 2000L // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Make sure your layout only has an ImageView

        // Navigate to main activity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, SPLASH_DURATION)
    }
}
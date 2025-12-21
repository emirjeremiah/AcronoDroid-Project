package com.example.acronodroid

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private lateinit var tvWelcomeMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage)

        val user = auth.currentUser
        if (user != null) {
            tvWelcomeMessage.text = "Welcome back!"
        } else {
            tvWelcomeMessage.text = "Loading..."
        }

        // Navigate to LibraryActivity after 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LibraryActivity::class.java))
            finish()
        }, 2000)
    }
}
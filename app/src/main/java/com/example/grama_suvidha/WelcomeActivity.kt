package com.example.grama_suvidha

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class WelcomeActivity : AppCompatActivity() {

    private val TAG = "WelcomeActivity"

    private var isKannada = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // 1. Initial State Setup
            // enableEdgeToEdge() // Disabled temporarily for stability check
            LocaleHelper.updateConfiguration(this, LocaleHelper.getLanguage(this))
            isKannada = LocaleHelper.getLanguage(this) == "kn"
            
            Log.d(TAG, "onCreate: WelcomeActivity started")

            // 2. Immediate Session Check
            val prefs = getSharedPreferences("GramaSuvidhaPrefs", Context.MODE_PRIVATE)
            val userId = prefs.getInt("loggedInUserId", 0)
            
            if (userId != 0) {
                lifecycleScope.launch {
                    try {
                        val user = withContext(Dispatchers.IO) {
                            AppDatabase.getDatabase(applicationContext).userDao().getUserById(userId)
                        }
                        
                        if (user != null) {
                            navigateToMain()
                        } else {
                            prefs.edit().remove("loggedInUserId").apply()
                            showWelcomeUI()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Database error during session check", e)
                        prefs.edit().remove("loggedInUserId").apply()
                        showWelcomeUI()
                    }
                }
                return
            }

            // 3. No session found
            showWelcomeUI()
        } catch (t: Throwable) {
            Log.e(TAG, "FATAL STARTUP ERROR", t)
            android.widget.Toast.makeText(this, "Startup Error: ${t.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
            setContentView(android.widget.FrameLayout(this)) // Empty view to prevent further crashes
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showWelcomeUI() {
        setContentView(R.layout.activity_welcome)
        setupWelcomeUI()
    }

    private fun setupWelcomeUI() {
        // Logo Diagnostic Listener
        findViewById<android.view.View>(R.id.logoCard).setOnClickListener {
            Toast.makeText(this, "Grama-Suvidha v1.0 | Ready", Toast.LENGTH_SHORT).show()
        }

        // Handle Safe Area / Insets manually for the content
        val rootView = findViewById<android.view.View>(R.id.welcomeRoot)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnLogin: Button = findViewById(R.id.btnLogin)
        val btnCreateAccount: Button = findViewById(R.id.btnCreateAccount)
        val btnTrack: Button = findViewById(R.id.btnTrack)
        val etTrackingId: TextInputEditText = findViewById(R.id.etTrackingId)
        val btnLanguage: Button = findViewById(R.id.btnWelcomeLanguage)
        val btnToggleTheme: com.google.android.material.button.MaterialButton = findViewById(R.id.btnToggleTheme)
        val btnAboutProject: com.google.android.material.button.MaterialButton = findViewById(R.id.btnAboutProject)


        // Update theme icon
        val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        btnToggleTheme.setIconResource(if (isDarkMode) R.drawable.ic_light_mode else R.drawable.ic_dark_mode)

        btnToggleTheme.setOnClickListener {
            val newMode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
            AppCompatDelegate.setDefaultNightMode(newMode)
        }

        btnAboutProject.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        btnLanguage.setOnClickListener {
            toggleLanguage()
        }

        btnLogin.setOnClickListener {
            try {
                startActivity(Intent(this, LoginActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, "Nav Error: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("WelcomeActivity", "Failed to start LoginActivity", e)
            }
        }

        btnCreateAccount.setOnClickListener {
            try {
                startActivity(Intent(this, RegisterActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, "Nav Error: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("WelcomeActivity", "Failed to start RegisterActivity", e)
            }
        }



        btnTrack.setOnClickListener {
            val trackingId = etTrackingId.text.toString().trim()
            if (trackingId.isNotEmpty()) {
                val id = trackingId.toIntOrNull() ?: 0
                val repository = ProjectRepository(this)
                
                // Use lifecycleScope to launch a coroutine to fetch the data
                lifecycleScope.launch {
                    val project = repository.getProjectById(id)
                    
                    if (project != null) {
                        val intent = Intent(this@WelcomeActivity, ProjectDetailsActivity::class.java)
                        intent.putExtra("PROJECT_DATA", project)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@WelcomeActivity, "Project not found with this ID", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.error_invalid_tracking), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleLanguage() {
        val newLang = if (isKannada) "en" else "kn"
        LocaleHelper.setLocale(this, newLang)

        // Restart activity to apply language changes
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}

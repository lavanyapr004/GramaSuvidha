package com.example.grama_suvidha

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import android.widget.TextView
import android.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfileAvatar: ShapeableImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        // enableEdgeToEdge() // Disabled for stability check
        LocaleHelper.updateConfiguration(this, LocaleHelper.getLanguage(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<MaterialButton>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnTrackIssues).setOnClickListener {
            startActivity(Intent(this, UserIssuesActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            // Clear session
            val prefs = getSharedPreferences("GramaSuvidhaPrefs", Context.MODE_PRIVATE)
            prefs.edit().remove("loggedInUserId").apply()

            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        ivProfileAvatar = findViewById(R.id.ivProfileAvatar)
        findViewById<FloatingActionButton>(R.id.fabEditPhoto).setOnClickListener {
            showAvatarSelectionDialog()
        }
    }

    private fun showAvatarSelectionDialog() {
        val avatarNames = arrayOf("Default Citizen", "Rural Scene", "Quality Badge", "Nature Leaf")
        val avatarIcons = intArrayOf(
            R.drawable.ic_profile,
            R.drawable.ic_rural_scene,
            R.drawable.ic_quality,
            R.drawable.ic_rural_leaf
        )

        AlertDialog.Builder(this)
            .setTitle("Choose an Avatar")
            .setItems(avatarNames) { _, which ->
                val selectedIcon = avatarIcons[which]
                ivProfileAvatar.setImageResource(selectedIcon)
                
                // Save selection
                val sharedPrefs = getSharedPreferences("GramaSuvidhaPrefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putInt("profile_avatar_res", selectedIcon).apply()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
        loadRealStats()
    }

    private fun loadUserProfile() {
        val sharedPrefs = getSharedPreferences("GramaSuvidhaPrefs", Context.MODE_PRIVATE)
        val name = sharedPrefs.getString("name", "Lavanya P R")
        val email = sharedPrefs.getString("email", "lavanya.pr@example.com")
        val phone = sharedPrefs.getString("phone", "+91 98765 43210")
        val joinedDate = sharedPrefs.getString("joinedDate", "Jan 2026")

        findViewById<TextView>(R.id.tvProfileName).text = name
        findViewById<TextView>(R.id.tvProfileEmail).text = email
        findViewById<TextView>(R.id.tvProfilePhone).text = "Phone: $phone"
        findViewById<TextView>(R.id.tvJoinedDate).text = getString(R.string.member_since, joinedDate)

        val avatarRes = sharedPrefs.getInt("profile_avatar_res", R.drawable.ic_profile)
        ivProfileAvatar.setImageResource(avatarRes)
    }

    /**
     * Load real activity stats from the Room database instead of hardcoded values.
     */
    private fun loadRealStats() {
        val db = AppDatabase.getDatabase(this)
        val prefs = getSharedPreferences("GramaSuvidhaPrefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("loggedInUserId", 0)

        lifecycleScope.launch {
            val projectCount = withContext(Dispatchers.IO) {
                db.projectDao().getCount()
            }
            val feedbackCount = withContext(Dispatchers.IO) {
                db.feedbackDao().getFeedbackCountByUser(userId)
            }
            val issueCount = withContext(Dispatchers.IO) {
                db.issueDao().getIssueCountByUser(userId)
            }

            // Update the stat TextViews
            findViewById<TextView>(R.id.tvStatProjects).text = projectCount.toString()
            findViewById<TextView>(R.id.tvStatFeedback).text = feedbackCount.toString()
            findViewById<TextView>(R.id.tvStatIssues).text = issueCount.toString()
        }
    }
}

package com.example.grama_suvidha

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import coil.load
import coil.transform.RoundedCornersTransformation
import androidx.activity.enableEdgeToEdge
import com.example.grama_suvidha.R

class ProjectDetailsActivity : AppCompatActivity() {

    private lateinit var viewModel: ProjectViewModel
    private lateinit var currentProject: Project


    override fun onCreate(savedInstanceState: Bundle?) {
        // enableEdgeToEdge() // Disabled for stability check
        LocaleHelper.updateConfiguration(this, LocaleHelper.getLanguage(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_details)

        viewModel = ViewModelProvider(this)[ProjectViewModel::class.java]

        // Back arrow
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.detailToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val btnToggleLanguage: Button = findViewById(R.id.btnToggleLanguage)
        btnToggleLanguage.setOnClickListener {
            val currentLang = LocaleHelper.getLanguage(this)
            val newLang = if (currentLang == "kn") "en" else "kn"
            LocaleHelper.setLocale(this, newLang)
            recreate()
        }

        // Receive Data
        val initialProject = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("PROJECT_DATA", Project::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("PROJECT_DATA") as? Project
        }

        if (initialProject == null) {
            Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        currentProject = initialProject

        // Observe ViewModel for real-time updates from Database
        viewModel.projects.observe(this) { projects ->
            projects.find { it.id == initialProject.id }?.let { updatedProject ->
                currentProject = updatedProject
                updateUI(updatedProject)
            }
        }

        // Initial UI Update
        updateUI(initialProject)

        // Setup Feedback Wall
        val rvFeedbackWall: androidx.recyclerview.widget.RecyclerView = findViewById(R.id.rvFeedbackWall)
        val feedbackAdapter = FeedbackAdapter(emptyList())
        rvFeedbackWall.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rvFeedbackWall.adapter = feedbackAdapter

        val tvEmptyWall: TextView = findViewById(R.id.tvEmptyWall)

        // Observe Feedback
        viewModel.getProjectFeedback(currentProject.id).observe(this) { feedbacks ->
            feedbackAdapter.updateData(feedbacks)
            if (feedbacks.isEmpty()) {
                tvEmptyWall.visibility = android.view.View.VISIBLE
                rvFeedbackWall.visibility = android.view.View.GONE
            } else {
                tvEmptyWall.visibility = android.view.View.GONE
                rvFeedbackWall.visibility = android.view.View.VISIBLE
            }
        }

        // Get logged-in user info for feedback
        val prefs = getSharedPreferences("GramaSuvidhaPrefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("loggedInUserId", 0)
        val userName = prefs.getString("name", "Anonymous") ?: "Anonymous"

        var selectedImageUri: android.net.Uri? = null
        val ivPhotoPreview: com.google.android.material.imageview.ShapeableImageView = findViewById(R.id.ivFeedbackPhotoPreview)

        val pickImageLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
            uri?.let {
                selectedImageUri = it
                ivPhotoPreview.visibility = android.view.View.VISIBLE
                ivPhotoPreview.setImageURI(it)
            }
        }

        findViewById<Button>(R.id.btnAddFeedbackPhoto).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Handle Feedback Submission — linked to user
        findViewById<Button>(R.id.btnSubmitFeedback).setOnClickListener {
            val rating = findViewById<RatingBar>(R.id.ratingBar).rating
            if (rating > 0) {
                // Here we could add a comment field, but for now we'll just submit the rating
                viewModel.submitFeedback(
                    projectId = currentProject.id,
                    rating = rating,
                    userId = userId,
                    userName = userName,
                    comment = "Rated ${rating.toInt()} stars via Grama-Suvidha Portal.",
                    imageUri = selectedImageUri?.toString()
                )
                Toast.makeText(this, getString(R.string.feedback_submitted), Toast.LENGTH_SHORT).show()
                findViewById<RatingBar>(R.id.ratingBar).rating = 0f
                ivPhotoPreview.visibility = android.view.View.GONE
                selectedImageUri = null
            } else {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
            }
        }

        // View on Map
        findViewById<Button>(R.id.btnViewOnMap).setOnClickListener {
            val uri = "geo:${currentProject.latitude},${currentProject.longitude}?q=${currentProject.latitude},${currentProject.longitude}(${currentProject.name})"
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri))
            intent.setPackage("com.google.android.apps.maps")
            try {
                startActivity(intent)
            } catch (e: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=${currentProject.latitude},${currentProject.longitude}"))
                startActivity(webIntent)
            }
        }

        // Report Issue
        findViewById<Button>(R.id.btnReportIssue).setOnClickListener {
            val intent = Intent(this, ReportIssueActivity::class.java)
            intent.putExtra("PROJECT_NAME", currentProject.name)
            intent.putExtra("PROJECT_ID", currentProject.id)
            startActivity(intent)
        }

        // Enhanced Sharing logic for WhatsApp and Business versions
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnShare).setOnClickListener {
            val shareText = "Project Update: ${currentProject.name}\nStatus: ${currentProject.status}\nProgress: ${currentProject.progress}%\nTrack it on Grama-Suvidha app!"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            try {
                // Try to launch WhatsApp directly
                val waIntent = Intent(intent).setPackage("com.whatsapp")
                startActivity(waIntent)
            } catch (e: Exception) {
                try {
                    // Fallback to WhatsApp Business if regular is not found
                    val wbIntent = Intent(intent).setPackage("com.whatsapp.w4b")
                    startActivity(wbIntent)
                } catch (e2: Exception) {
                    // Final fallback to generic system share chooser
                    startActivity(Intent.createChooser(intent, getString(R.string.share_project_via)))
                }
            }
        }

        // PDF Download
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDownload).setOnClickListener {
            PdfGenerator.generateProjectReport(this, currentProject)
        }

        // Simulate Progress — LiveData updates the UI in real-time
        findViewById<Button>(R.id.btnSimulateProgress).setOnClickListener {
            if (currentProject.progress < 100) {
                viewModel.simulateProgress(currentProject.id, currentProject.progress)
                Toast.makeText(this, getString(R.string.progress_simulated), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.progress_already_complete), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(project: Project) {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.detailToolbar)
        toolbar.title = project.name

        findViewById<TextView>(R.id.tvDetailProjectId).text = "Project ID: #${project.id}"
        findViewById<TextView>(R.id.tvDetailTitle).text = project.name
        findViewById<TextView>(R.id.tvDetailLocation).text = project.location
        
        val tvStatus = findViewById<TextView>(R.id.tvDetailStatus)
        tvStatus.text = getString(R.string.status_text, project.status)
        
        findViewById<TextView>(R.id.tvDetailProgress).text = getString(R.string.progress_text, project.progress)
        findViewById<TextView>(R.id.tvDetailDescription).text = project.description
        findViewById<ProgressBar>(R.id.detailProgressBar).progress = project.progress
        findViewById<RatingBar>(R.id.ratingBar).rating = project.rating

        findViewById<TextView>(R.id.tvDetailBudget).text = "💰 Total: ${project.budget}"
        findViewById<TextView>(R.id.tvDetailExpectedDate).text = getString(R.string.expected_date_text, project.expectedCompletion)

        // Update Budget Meter
        val budgetMeter: com.google.android.material.progressindicator.LinearProgressIndicator = findViewById(R.id.budgetMeter)
        val tvReleasedFunds: TextView = findViewById(R.id.tvReleasedFunds)
        
        if (project.totalBudget > 0) {
            val clampedFunds = project.fundsReleased.coerceAtMost(project.totalBudget)
            val budgetProgress = ((clampedFunds.toFloat() / project.totalBudget.toFloat()) * 100).toInt().coerceIn(0, 100)
            budgetMeter.progress = budgetProgress
            val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "IN"))
            val releasedStr = formatter.format(clampedFunds)
            tvReleasedFunds.text = "Funds Released: $releasedStr ($budgetProgress%)"
        } else {
            budgetMeter.progress = 0
            tvReleasedFunds.text = "Budget details not yet finalized."
        }

        val ivBefore: ImageView = findViewById(R.id.ivBefore)
        val ivAfter: ImageView = findViewById(R.id.ivAfter)

        if (!project.imageUrlBefore.isNullOrEmpty()) {
            ivBefore.load(android.net.Uri.parse(project.imageUrlBefore)) {
                crossfade(true)
                placeholder(R.drawable.ic_project_placeholder)
                error(R.drawable.ic_project_placeholder)
                transformations(RoundedCornersTransformation(16f))
            }
        }

        if (!project.imageUrlAfter.isNullOrEmpty()) {
            ivAfter.load(android.net.Uri.parse(project.imageUrlAfter)) {
                crossfade(true)
                placeholder(R.drawable.ic_project_placeholder)
                error(R.drawable.ic_project_placeholder)
                transformations(RoundedCornersTransformation(16f))
            }
        }

        val detailCardStatus: com.google.android.material.card.MaterialCardView = findViewById(R.id.detailCardStatus)
        when (project.statusEn.lowercase()) {
            "completed" -> {
                tvStatus.setTextColor(getColor(R.color.status_completed))
                detailCardStatus.setCardBackgroundColor(getColor(R.color.status_completed_bg))
            }
            "in progress" -> {
                tvStatus.setTextColor(getColor(R.color.status_in_progress))
                detailCardStatus.setCardBackgroundColor(getColor(R.color.status_in_progress_bg))
            }
            else -> {
                tvStatus.setTextColor(getColor(R.color.status_pending))
                detailCardStatus.setCardBackgroundColor(getColor(R.color.status_pending_bg))
            }
        }
    }
}

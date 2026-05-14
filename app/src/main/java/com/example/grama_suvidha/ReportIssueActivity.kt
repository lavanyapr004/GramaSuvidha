package com.example.grama_suvidha

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.android.material.textfield.TextInputEditText
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportIssueActivity : AppCompatActivity() {

    private var attachedFileUri: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        // enableEdgeToEdge() // Disabled for stability check
        LocaleHelper.updateConfiguration(this, LocaleHelper.getLanguage(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_issue)

        // Toolbar Setup
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.reportToolbar)
            .setNavigationOnClickListener { finish() }

        val btnToggleLanguage: Button = findViewById(R.id.btnToggleLanguage)
        btnToggleLanguage.setOnClickListener {
            val currentLang = LocaleHelper.getLanguage(this)
            val newLang = if (currentLang == "kn") "en" else "kn"
            LocaleHelper.setLocale(this, newLang)
            recreate()
        }

        // Get Project Name and ID from Intent
        val projectName = intent.getStringExtra("PROJECT_NAME") ?: "Project"
        val projectId = intent.getIntExtra("PROJECT_ID", 0)
        findViewById<TextView>(R.id.tvReportProjectName).text = 
            getString(R.string.projects_title) + ": " + projectName + " (ID: " + projectId + ")"

        val etSubject: TextInputEditText = findViewById(R.id.etIssueSubject)
        val etDesc: TextInputEditText = findViewById(R.id.etIssueDesc)
        val tvFileName: TextView = findViewById(R.id.tvAttachedFileName)
        val cardAttach: com.google.android.material.card.MaterialCardView = findViewById(R.id.cardAttachDocument)
        val btnSubmit: Button = findViewById(R.id.btnSubmitReport)

        // Setup Spinners
        val categories = arrayOf("Construction Quality", "Project Delay", "Safety Concern", "Resource Misuse", "Other")
        val priorities = arrayOf("Low", "Medium", "High", "Urgent")
        
        val categorySpinner: Spinner = findViewById(R.id.spinnerCategory)
        val prioritySpinner: Spinner = findViewById(R.id.spinnerPriority)
        
        categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        prioritySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorities)

        // File Picker Launcher
        val filePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.data
                attachedFileUri = data?.toString()
                tvFileName.text = data?.path?.substringAfterLast("/") ?: "document.pdf"
                tvFileName.setTextColor(getColor(R.color.primary))
            }
        }

        cardAttach.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            filePicker.launch(intent)
        }

        btnSubmit.setOnClickListener {
            val subject = etSubject.text.toString().trim()
            val desc = etDesc.text.toString().trim()

            if (subject.isNotEmpty() && desc.isNotEmpty()) {
                // Get logged-in user ID from session
                val prefs = getSharedPreferences("GramaSuvidhaPrefs", Context.MODE_PRIVATE)
                val userId = prefs.getInt("loggedInUserId", 0)

                // Persist the issue to Room Database
                val issueDao = AppDatabase.getDatabase(this).issueDao()
                val issue = Issue(
                    projectId = projectId,
                    userId = userId,
                    subject = subject,
                    description = desc,
                    category = categorySpinner.selectedItem.toString(),
                    priority = prioritySpinner.selectedItem.toString(),
                    attachmentUri = attachedFileUri,
                    projectName = projectName,
                    status = "Open"
                )

                lifecycleScope.launch {
                    val issueId = withContext(Dispatchers.IO) {
                        issueDao.insertIssue(issue)
                    }
                    Toast.makeText(
                        this@ReportIssueActivity,
                        getString(R.string.report_success_toast) + " (Issue #$issueId)",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            } else {
                Toast.makeText(this, getString(R.string.error_fill_all), Toast.LENGTH_SHORT).show()
            }
        }
    }
}

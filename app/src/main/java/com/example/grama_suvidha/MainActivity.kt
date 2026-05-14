package com.example.grama_suvidha

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.enableEdgeToEdge
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.google.android.material.progressindicator.CircularProgressIndicator

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var projectAdapter: ProjectAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: ProjectViewModel

    // To track current language
    private var isKannada = false



    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Session Guard (Security check)
        val prefs = getSharedPreferences("GramaSuvidhaPrefs", Context.MODE_PRIVATE)
        if (prefs.getInt("loggedInUserId", 0) == 0) {
            Log.d(TAG, "No active session found in MainActivity, redirecting to Welcome.")
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // enableEdgeToEdge() // Disabled temporarily for stability check
        LocaleHelper.updateConfiguration(this, LocaleHelper.getLanguage(this))
        isKannada = LocaleHelper.getLanguage(this) == "kn"
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "onCreate: MainActivity started")
        android.widget.Toast.makeText(this, "Dashboard Loaded Successfully", android.widget.Toast.LENGTH_SHORT).show()
        
        // Check intent data to see if it was triggered by an external source
        intent?.data?.let {
            Log.d(TAG, "onCreate: Started with data: $it")
        }

        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewProjects)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Add divider between items
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        
        // Initialize Adapter and set to RecyclerView
        projectAdapter = ProjectAdapter()
        recyclerView.adapter = projectAdapter

        val loadingIndicator = findViewById<CircularProgressIndicator>(R.id.loadingIndicator)


        val btnToggleLanguage: Button = findViewById(R.id.btnToggleLanguage)
        val btnToggleTheme: com.google.android.material.button.MaterialButton = findViewById(R.id.btnToggleTheme)
        val btnProfile = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnProfile)

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDirectory).setOnClickListener {
            startActivity(Intent(this, PanchayatActivity::class.java))
        }

        // Update theme icon
        val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        btnToggleTheme.setIconResource(if (isDarkMode) R.drawable.ic_light_mode else R.drawable.ic_dark_mode)

        btnToggleTheme.setOnClickListener {
            val newMode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
            AppCompatDelegate.setDefaultNightMode(newMode)
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ProjectViewModel::class.java]

        // Setup Search
        val searchView: androidx.appcompat.widget.SearchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.filterBySearch(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterBySearch(newText ?: "")
                return true
            }
        })

        // Setup Category Filters
        val chipGroup: com.google.android.material.chip.ChipGroup = findViewById(R.id.chipGroupCategories)
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                viewModel.filterByCategory("All")
            } else {
                val chip = group.findViewById<com.google.android.material.chip.Chip>(checkedIds[0])
                chip?.let {
                    viewModel.filterByCategory(it.text.toString())
                } ?: viewModel.filterByCategory("All")
            }
        }

        // Observe filtered projects — UI updates reactively using submitList
        viewModel.filteredProjects.observe(this) { projects ->
            projectAdapter.submitList(projects)
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        btnToggleLanguage.setOnClickListener {
            toggleLanguage()
        }
    }

    private fun toggleLanguage() {
        val newLang = if (isKannada) "en" else "kn"
        LocaleHelper.setLocale(this, newLang)

        // Restart activity to apply language changes
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}

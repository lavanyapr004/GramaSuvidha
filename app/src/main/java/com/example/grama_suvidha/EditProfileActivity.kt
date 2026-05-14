package com.example.grama_suvidha

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        // enableEdgeToEdge() // Disabled for stability check
        LocaleHelper.updateConfiguration(this, LocaleHelper.getLanguage(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val toolbar: Toolbar = findViewById(R.id.editToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val etName: TextInputEditText = findViewById(R.id.etEditName)
        val etEmail: TextInputEditText = findViewById(R.id.etEditEmail)
        val etPhone: TextInputEditText = findViewById(R.id.etEditPhone)
        val btnSave: MaterialButton = findViewById(R.id.btnSaveProfile)

        // Load current data
        val sharedPrefs = getSharedPreferences("GramaSuvidhaPrefs", Context.MODE_PRIVATE)
        etName.setText(sharedPrefs.getString("name", ""))
        etEmail.setText(sharedPrefs.getString("email", ""))
        etPhone.setText(sharedPrefs.getString("phone", ""))

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (name.isNotEmpty() && (email.isNotEmpty() || phone.isNotEmpty())) {
                // Update SharedPreferences
                with(sharedPrefs.edit()) {
                    putString("name", name)
                    putString("email", email)
                    putString("phone", phone)
                    apply()
                }

                // Also update Room database if user is logged in
                val userId = sharedPrefs.getInt("loggedInUserId", 0)
                if (userId > 0) {
                    val userDao = AppDatabase.getDatabase(this).userDao()
                    lifecycleScope.launch {
                        val user = withContext(Dispatchers.IO) { userDao.getUserById(userId) }
                        if (user != null) {
                            val updatedUser = user.copy(name = name, email = email, phone = phone)
                            withContext(Dispatchers.IO) { userDao.updateUser(updatedUser) }
                        }
                    }
                }

                Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, R.string.error_fill_all, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

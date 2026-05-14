package com.example.grama_suvidha

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge

class PanchayatActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        // enableEdgeToEdge() // Disabled for stability check
        LocaleHelper.updateConfiguration(this, LocaleHelper.getLanguage(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panchayat)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.panchayatToolbar).setNavigationOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnCallPresident).setOnClickListener {
            makeCall("9876543210")
        }

        findViewById<Button>(R.id.btnCallSecretary).setOnClickListener {
            makeCall("9876543211")
        }

        findViewById<Button>(R.id.btnCallWardMember).setOnClickListener {
            makeCall("9876543212")
        }

        findViewById<Button>(R.id.btnCallOffice).setOnClickListener {
            makeCall("080-22221111") // Representative Village Office Number
        }
    }

    private fun makeCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }
}

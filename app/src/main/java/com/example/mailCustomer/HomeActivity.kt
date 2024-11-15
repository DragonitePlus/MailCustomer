package com.example.mailCustomer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mailcostomer.R
import com.google.android.material.card.MaterialCardView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val inboxBlock = findViewById<MaterialCardView>(R.id.inboxBlock)
        val sendMailBlock = findViewById<MaterialCardView>(R.id.sendMailBlock)

        inboxBlock.setOnClickListener {
            val intent = Intent(this, InboxActivity::class.java)
            startActivity(intent)
        }

        sendMailBlock.setOnClickListener {
            val intent = Intent(this, SendMailActivity::class.java)
            startActivity(intent)
        }
    }
}
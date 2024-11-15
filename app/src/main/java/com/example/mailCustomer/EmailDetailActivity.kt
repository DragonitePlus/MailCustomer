package com.example.mailCustomer

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mailcostomer.R

class EmailDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_detail)

        val senderEmail = intent.getStringExtra("senderEmail")
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")

        findViewById<TextView>(R.id.senderEmail).text = senderEmail
        findViewById<TextView>(R.id.emailTitle).text = title
        findViewById<TextView>(R.id.emailContent).text = content
    }
}

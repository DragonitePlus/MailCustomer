package com.example.mailCustomer

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mailcostomer.R
import com.google.android.material.appbar.MaterialToolbar

class EmailDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_detail)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val title = intent.getStringExtra("email_title")
        val sender = intent.getStringExtra("email_sender")
        val content = intent.getStringExtra("email_content")

        val titleTextView = findViewById<TextView>(R.id.titleTextView)
        val senderTextView = findViewById<TextView>(R.id.senderTextView)
        val contentTextView = findViewById<TextView>(R.id.contentTextView)

        titleTextView.text = title
        senderTextView.text = sender
        contentTextView.text = content

        toolbar.setNavigationOnClickListener {
            finish() // 返回上一页面
        }
    }
}

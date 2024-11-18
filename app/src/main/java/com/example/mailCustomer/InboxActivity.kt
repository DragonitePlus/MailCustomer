package com.example.mailCustomer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mailcostomer.R
import com.google.android.material.appbar.MaterialToolbar

class InboxActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val emailAdapter = EmailAdapter(emptyList()) { email -> fetchEmailContentAndOpenDetail(email) }
    private val viewModel: InboxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = emailAdapter

        toolbar.setNavigationOnClickListener {
            finish() // 返回上一页面
        }

        observeViewModel()
        viewModel.fetchEmails(getEmailCredentials())
    }

    private fun observeViewModel() {
        viewModel.emailList.observe(this, Observer { emails ->
            if (emails.isNotEmpty()) {
                emailAdapter.updateEmails(emails)
                progressBar.visibility = View.GONE
            } else {
                Toast.makeText(this, "No emails found", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.isLoading.observe(this, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.errorMessage.observe(this, Observer { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchEmailContentAndOpenDetail(email: Email) {
        val intent = Intent(this, EmailDetailActivity::class.java).apply {
        putExtra("email_sender", email.sender)
        putExtra("email_title", email.title)
        putExtra("email_content", email.content)
    }
    startActivity(intent)
    }

    private fun getEmailCredentials(): Pair<String, String> {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("email_username", "t1740084968@163.com")
        val password = sharedPreferences.getString("email_password", "ATSTcmxMFDJRtw7R")
        return Pair(username ?: "", password ?: "")
    }
}
data class Email(val sender: String, val title: String, val content: String)

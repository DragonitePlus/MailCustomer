package com.example.mailCustomer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mailcostomer.R
import com.google.android.material.appbar.MaterialToolbar

class InboxActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val emailAdapter = EmailAdapter(
        emptyList(),
        onClick = { email -> fetchEmailContentAndOpenDetail(email) },
        onDelete = { email -> deleteEmail(email) }
    )
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
            finish()
        }

        observeViewModel()

        // Fetch POP3 host and port from SharedPreferences
        val sharedPreferences = getSharedPreferences("server_config", Context.MODE_PRIVATE)
        val pop3Host = sharedPreferences.getString("domain", "10.0.2.2") ?: "10.0.2.2"
        val pop3Port = sharedPreferences.getInt("pop3_port", 1100)

        viewModel.fetchEmails(getEmailCredentials(), pop3Host, pop3Port)
    }

    private fun observeViewModel() {
        viewModel.emailList.observe(this) { emails ->
            emailAdapter.updateEmails(emails)
        }
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchEmailContentAndOpenDetail(email: Email) {
        val intent = Intent(this, EmailDetailActivity::class.java).apply {
            putExtra("email_sender", email.sender)
            putExtra("email_title", email.title)
            putExtra("email_content", email.content)
        }
        startActivity(intent)
    }

    private fun deleteEmail(email: Email) {
        val emailIndex = viewModel.emailList.value?.indexOf(email)
        if (emailIndex != null && emailIndex >= 0) {
            val sharedPreferences = getSharedPreferences("server_config", Context.MODE_PRIVATE)
            val pop3Host = sharedPreferences.getString("domain", "10.0.2.2") ?: "10.0.2.2"
            val pop3Port = sharedPreferences.getInt("pop3_port", 1100)
            viewModel.deleteEmail(getEmailCredentials(), emailIndex, pop3Host, pop3Port)
        } else {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getEmailCredentials(): String {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("email_username", "") ?: ""
    }
}

data class Email(
    val sender: String,
    val title: String,
    val content: String
)

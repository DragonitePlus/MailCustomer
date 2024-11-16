package com.example.mailCustomer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mailcostomer.R
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Folder
import javax.mail.Session
import javax.mail.internet.InternetAddress

class InboxActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private val emailList = mutableListOf<Email>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = EmailAdapter(emailList) { email ->
            fetchEmailContentAndOpenDetail(email)
        }

        toolbar.setNavigationOnClickListener {
            finish() // 返回上一页面
        }

        fetchEmails()
    }

    private fun fetchEmails() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("InboxActivity", "Starting to fetch emails")

            try {
                val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val username = sharedPreferences.getString("email_username", "t1740084968@163.com")
                val password = sharedPreferences.getString("email_password", "ATSTcmxMFDJRtw7R")

                if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@InboxActivity, "Email credentials not found", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                Log.d("InboxActivity", "Username: $username, Password: [hidden]")

                val props = Properties().apply {
                    this["mail.store.protocol"] = "pop3"
                    this["mail.pop3.host"] = "pop.163.com"
                    this["mail.pop3.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
                    this["mail.pop3.socketFactory.fallback"] = "false"
                    this["mail.pop3.port"] = "995"
                    this["mail.pop3.socketFactory.port"] = "995"
                }

                val session = Session.getInstance(props, null)
                val store = session.getStore("pop3")
                store.connect("pop.163.com", username, password)

                Log.d("InboxActivity", "Connected to POP3 server")

                val inbox = store.getFolder("INBOX")
                inbox.open(Folder.READ_ONLY)

                val messages = inbox.messages
                val emailList = mutableListOf<Email>()

                for (message in messages) {
                    val sender = extractEmailAddress(message.from[0].toString())
                    val title = message.subject
                    val content = extractContent(message.content) // 提取邮件正文
                    emailList.add(Email(sender, title, content))

                    // 打印邮件信息
                    Log.d("InboxActivity", "Received email: Sender=$sender, Title=$title")
                }

                withContext(Dispatchers.Main) {
                    if (emailList.isEmpty()) {
                        Toast.makeText(this@InboxActivity, "No emails received", Toast.LENGTH_SHORT).show()
                    }
                    Log.d("InboxActivity", "Updating email list in UI thread")
                    this@InboxActivity.emailList.clear()
                    this@InboxActivity.emailList.addAll(emailList)
                    recyclerView.adapter?.notifyDataSetChanged()
                    Log.d("InboxActivity", "Email list updated, size=${this@InboxActivity.emailList.size}")
                }

                inbox.close(false)
                store.close()

                Log.d("InboxActivity", "Finished fetching emails")
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InboxActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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

    private fun extractContent(content: Any): String {
        return when (content) {
            is String -> content
            is javax.mail.Multipart -> extractTextFromMimeMultipart(content)
            else -> "Unsupported content type"
        }
    }

    private fun extractTextFromMimeMultipart(multipart: javax.mail.Multipart): String {
        val result = StringBuilder()
        for (i in 0 until multipart.count) {
            val bodyPart = multipart.getBodyPart(i)
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.content.toString())
            } else if (bodyPart.content is javax.mail.Multipart) {
                result.append(extractTextFromMimeMultipart(bodyPart.content as javax.mail.Multipart))
            }
        }
        return result.toString()
    }

    private fun extractEmailAddress(sender: String): String {
        return try {
            val internetAddress = InternetAddress(sender)
            internetAddress.address ?: "Unknown"
        } catch (e: Exception) {
            Log.e("InboxActivity", "Error parsing sender address: ${e.message}")
            "Unknown"
        }
    }
}

data class Email(val sender: String, val title: String, val content: String)

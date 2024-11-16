package com.example.mailCustomer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
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

class InboxActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var noEmailTextView: TextView
    private val emailList = mutableListOf<Email>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        noEmailTextView = findViewById(R.id.noEmailTextView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = EmailAdapter(emailList) { email ->
            fetchEmailContentAndOpenDetail(email.sender, email.title)
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

                Log.d("InboxActivity", "Username: $username, Password: $password")

                val props = Properties()
                props["mail.store.protocol"] = "pop3"
                props["mail.pop3.host"] = "pop.163.com" // 163 邮箱的 POP3 服务器地址
                props["mail.pop3.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
                props["mail.pop3.socketFactory.fallback"] = "false"
                props["mail.pop3.port"] = "995"
                props["mail.pop3.socketFactory.port"] = "995"

                val session = Session.getInstance(props, null)
                val store = session.getStore("pop3")
                store.connect("pop.163.com", username, password)

                Log.d("InboxActivity", "Connected to POP3 server")

                val inbox = store.getFolder("INBOX")
                inbox.open(Folder.READ_ONLY)

                val messages = inbox.messages

                if (messages.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        noEmailTextView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                } else {
                    val emailList = mutableListOf<Email>()

                    for (message in messages) {
                        val sender = message.from[0].toString()
                        val title = message.subject
                        emailList.add(Email(sender, title))

                        Log.d("InboxActivity", "Received email: Sender=$sender, Title=$title")
                    }

                    withContext(Dispatchers.Main) {
                        noEmailTextView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        this@InboxActivity.emailList.clear()
                        this@InboxActivity.emailList.addAll(emailList)
                        recyclerView.adapter?.notifyDataSetChanged()
                        Log.d("InboxActivity", "Email list updated, size=${this@InboxActivity.emailList.size}")
                    }
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

    private fun fetchEmailContentAndOpenDetail(senderEmail: String, title: String) {
        // 略去与原始代码一致的逻辑
    }
}

data class Email(val sender: String, val title: String)

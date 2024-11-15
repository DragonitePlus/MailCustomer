package com.example.mailCustomer

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mailcostomer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class InboxActivity : AppCompatActivity() {
    private lateinit var mailContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        mailContainer = findViewById(R.id.mailContainer)
        fetchEmails()
    }

    private fun fetchEmails() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://localhost:8080/emails") // 替换为实际的服务器URL
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream.bufferedReader().use { it.readText() }
                    val emailArray = JSONArray(inputStream)
                    withContext(Dispatchers.Main) {
                        displayEmails(emailArray)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@InboxActivity, "Failed to fetch emails", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InboxActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayEmails(emailArray: JSONArray) {
        for (i in 0 until emailArray.length()) {
            val email = emailArray.getJSONObject(i)
            val senderEmail = email.getString("sender")
            val title = email.getString("title")

            val emailView = TextView(this).apply {
                text = title
                textSize = 18f
                setPadding(16, 16, 16, 16)
                setOnClickListener {
                    fetchEmailContentAndOpenDetail(senderEmail, title)
                }
            }

            mailContainer.addView(emailView)
        }
    }

    private fun fetchEmailContentAndOpenDetail(senderEmail: String, title: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 使用 POP3 连接获取邮件正文（需使用第三方库如 `javax.mail`）
                // 假设服务器 POP3 相关设置如下：
                // POP3 服务器地址：pop.example.com
                // 用户名和密码需要通过安全存储方式获取

                val pop3Host = "pop.example.com"
                val username = "your_email@example.com"
                val password = "your_password"

                val properties = System.getProperties()
                properties.setProperty("mail.pop3.host", pop3Host)
                properties.setProperty("mail.pop3.port", "110") // 或 "995" (SSL)
                properties.setProperty("mail.pop3.starttls.enable", "true")

                val session = javax.mail.Session.getDefaultInstance(properties)
                val store = session.getStore("pop3")
                store.connect(pop3Host, username, password)

                val inbox = store.getFolder("INBOX")
                inbox.open(javax.mail.Folder.READ_ONLY)

                val messages = inbox.messages.filter {
                    it.subject == title // 查找匹配标题的邮件
                }

                if (messages.isNotEmpty()) {
                    val message = messages[0] // 假设只有一封匹配的邮件
                    val content = message.content.toString()

                    withContext(Dispatchers.Main) {
                        openEmailDetail(senderEmail, title, content)
                    }
                }

                inbox.close(false)
                store.close()

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InboxActivity, "Failed to fetch email content", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openEmailDetail(senderEmail: String, title: String, content: String) {
        val intent = Intent(this, EmailDetailActivity::class.java).apply {
            putExtra("senderEmail", senderEmail)
            putExtra("title", title)
            putExtra("content", content)
        }
        startActivity(intent)
    }
}
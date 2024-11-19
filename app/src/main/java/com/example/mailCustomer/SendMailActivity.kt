package com.example.mailCustomer

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mailcostomer.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Properties
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SendMailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_mail)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val senderEmail = findViewById<EditText>(R.id.senderEmail)
        val recipientEmail = findViewById<EditText>(R.id.recipientEmail)
        val emailTitle = findViewById<EditText>(R.id.emailTitle)
        val emailContent = findViewById<EditText>(R.id.emailMessage)
        val sendButton = findViewById<Button>(R.id.sendButton)

        // 获取 username 作为 sender
        val sender = getUsernameFromToken() + "@example.com"
        senderEmail.setText(sender)

        sendButton.setOnClickListener {
            val recipient = recipientEmail.text.toString().trim()
            val title = emailTitle.text.toString().trim()
            val content = emailContent.text.toString().trim()

            if (recipient.isNotEmpty() && title.isNotEmpty() && content.isNotEmpty()) {
                sender?.let {
                    sendEmail(it, recipient, title, content)
                } ?: run {
                    Toast.makeText(this, "Failed to retrieve sender email from token", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        toolbar.setNavigationOnClickListener {
            finish() // 返回上一页面
        }
    }

    private fun sendEmail(sender: String, recipient: String, title: String, content: String) {
        // 打印调试信息
        println("Sender: $sender")
        println("Recipient: $recipient")
        println("Title: $title")
        println("Content: $content")

        // Configure SMTP properties
        val props = Properties()
        props["mail.smtp.auth"] = "false"
        props["mail.smtp.host"] = "10.0.2.2"
        props["mail.smtp.port"] = "2525"

        // Create a new coroutine to send the email
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val session = Session.getInstance(props)

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(sender))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
                    setSubject(title)
                    // 使用 setText 方法设置内容
                    setText(content, "UTF-8")
                }

                Transport.send(message)
                runOnUiThread {
                    Toast.makeText(this@SendMailActivity, "Email sent successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showResendDialog(sender, recipient, title, content)
                }
            }
        }
    }

    private fun showResendDialog(sender: String, recipient: String, title: String, content: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Failed to send email")
        builder.setMessage("There was an error sending the email. Do you want to try again?")
        builder.setPositiveButton("Retry") { _, _ ->
            sendEmail(sender, recipient, title, content)
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun getUsernameFromToken(): String? {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        return token?.let { decodeToken(it)?.get("sub") as? String }
    }

    private fun decodeToken(token: String): Map<String, Any>? {
        try {
            val parts = token.split("\\.".toRegex())
            if (parts.size != 3) {
                return null
            }

            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedString = String(decodedBytes, Charsets.UTF_8)

            return jsonToMap(decodedString)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun jsonToMap(jsonString: String): Map<String, Any> {
        return try {
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            Gson().fromJson(jsonString, mapType)
        } catch (e: JsonSyntaxException) {
            emptyMap()
        }
    }
}

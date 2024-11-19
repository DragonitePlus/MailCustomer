package com.example.mailCustomer

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mailcostomer.R
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SendMailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_mail)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val recipientEmail = findViewById<EditText>(R.id.recipientEmail)
        val emailTitle = findViewById<EditText>(R.id.emailTitle)
        val emailContent = findViewById<EditText>(R.id.emailMessage)
        val sendButton = findViewById<Button>(R.id.sendButton)

        sendButton.setOnClickListener {
            val recipient = recipientEmail.text.toString().trim()
            val title = emailTitle.text.toString().trim()
            val content = emailContent.text.toString().trim()

            if (recipient.isNotEmpty() && title.isNotEmpty() && content.isNotEmpty()) {
                sendEmail(recipient, title, content)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        toolbar.setNavigationOnClickListener {
            finish() // 返回上一页面
        }
    }

    private fun sendEmail(recipient: String, title: String, content: String) {
        // Configure SMTP properties
        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.host"] = "10.0.2.2"
        props["mail.smtp.port"] = "2525"

        // Read the saved username from SharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: ""
        val password = "sender" // 发件人邮箱密码或应用专用密码

        // Create a new coroutine to send the email
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val session = Session.getInstance(props, object : javax.mail.Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(username, password)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(username))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
                    subject = title
                    setText(content)
                }

                Transport.send(message)
                runOnUiThread {
                    Toast.makeText(this@SendMailActivity, "Email sent successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showResendDialog(recipient, title, content)
                }
            }
        }
    }

    private fun showResendDialog(recipient: String, title: String, content: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Failed to send email")
        builder.setMessage("There was an error sending the email. Do you want to try again?")
        builder.setPositiveButton("Retry") { _, _ ->
            sendEmail(recipient, title, content)
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
}

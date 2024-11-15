package com.example.mailCustomer

import com.example.mailcostomer.R
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        val recipientEmail = findViewById<EditText>(R.id.recipientEmail)
        val emailContent = findViewById<EditText>(R.id.emailContent)
        val sendButton = findViewById<Button>(R.id.sendButton)

        sendButton.setOnClickListener {
            val recipient = recipientEmail.text.toString().trim()
            val content = emailContent.text.toString().trim()

            if (recipient.isNotEmpty() && content.isNotEmpty()) {
                sendEmail(recipient, content)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendEmail(recipient: String, content: String) {
        // Configure SMTP properties
        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.example.com" // 请提供SMTP服务器地址
        props["mail.smtp.port"] = "587" // 常用端口为587或465

        // 请提供以下信息
        val username = "your_email@example.com" // 发件人邮箱地址
        val password = "your_email_password" // 发件人邮箱密码或应用专用密码

        // 创建一个新的线程来发送邮件
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
                    subject = "Test Email from Android App"
                    setText(content)
                }

                Transport.send(message)
                runOnUiThread {
                    Toast.makeText(this@SendMailActivity, "Email sent successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@SendMailActivity, "Failed to send email: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}


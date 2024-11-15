package com.example.mailCustomer

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.mailcostomer.R
import com.qmuiteam.qmui.arch.QMUIActivity
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class ComposeActivity : QMUIActivity() {
    private lateinit var topBar: QMUITopBarLayout
    private lateinit var toEditText: EditText
    private lateinit var subjectEditText: EditText
    private lateinit var bodyEditText: EditText
    private lateinit var sendButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)

        // 初始化顶部栏
        topBar = findViewById(R.id.topbar)
        topBar.setTitle(R.string.compose)
        topBar.addLeftBackImageButton().setOnClickListener { finish() }

        // 初始化输入字段
        toEditText = findViewById(R.id.toEditText)
        subjectEditText = findViewById(R.id.subjectEditText)
        bodyEditText = findViewById(R.id.bodyEditText)
        sendButton = findViewById(R.id.sendButton)

        sendButton.setOnClickListener {
            val to = toEditText.text.toString()
            val subject = subjectEditText.text.toString()
            val body = bodyEditText.text.toString()
            sendEmail(to, subject, body)
        }
    }

    private fun sendEmail(to: String, subject: String, body: String) {
        val loadingDialog = QMUITipDialog.Builder(this)
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
            .setTipWord(getString(R.string.sending))
            .create()
        loadingDialog.show()

        GlobalScope.launch(Dispatchers.IO) {
            val props = Properties().apply {
                put("mail.smtp.host", "smtp.example.com")
                put("mail.smtp.port", "587")
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication("your_email@example.com", "your_password")
                }
            })

            try {
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress("your_email@example.com"))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                    this.subject = subject
                    setText(body)
                }

                Transport.send(message)

                runOnUiThread {
                    loadingDialog.dismiss()
                    QMUITipDialog.Builder(this@ComposeActivity)
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                        .setTipWord(getString(R.string.email_sent))
                        .create()
                        .show()
                    finish()
                }
            } catch (e: MessagingException) {
                e.printStackTrace()
                runOnUiThread {
                    loadingDialog.dismiss()
                    QMUITipDialog.Builder(this@ComposeActivity)
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                        .setTipWord(getString(R.string.send_failed))
                        .create()
                        .show()
                }
            }
        }
    }
}


package com.example.mailCustomer

import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.example.mailcostomer.R
import com.qmuiteam.qmui.arch.QMUIActivity
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class ComposeActivity : QMUIActivity() {
    private lateinit var topBar: QMUITopBarLayout
    private lateinit var groupListView: QMUIGroupListView
    private lateinit var sendButton: QMUIRoundButton
    private lateinit var toEditText: EditText
    private lateinit var subjectEditText: EditText
    private lateinit var bodyEditText: EditText

    private fun createGroupListItemView(title: String, editText: EditText): QMUICommonListItemView {
        val itemView = groupListView.createItemView(title)
        itemView.accessoryType = QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM
        // 设置 `EditText` 为 `itemView` 的子视图
        itemView.addView(editText)
        return itemView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)

        topBar = findViewById(R.id.topbar)
        topBar.setTitle(R.string.compose)
        topBar.addLeftBackImageButton().setOnClickListener { finish() }

        groupListView = findViewById(R.id.groupListView)
        val section = QMUIGroupListView.newSection(this)

        toEditText = EditText(this)
        subjectEditText = EditText(this)
        bodyEditText = EditText(this)

        // 使用 `addItemView` 方法添加视图
        section.addItemView(createGroupListItemView(getString(R.string.to), toEditText), null)
        section.addItemView(createGroupListItemView(getString(R.string.subject), subjectEditText), null)
        section.addItemView(createGroupListItemView(getString(R.string.body), bodyEditText), null)

        section.addTo(groupListView)

        sendButton = findViewById(R.id.sendButton)
        sendButton.setOnClickListener {
            val to = toEditText.text.toString()
            val subject = subjectEditText.text.toString()
            val body = bodyEditText.text.toString()
            sendEmail(to, subject, body)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun sendEmail(to: String, subject: String, body: String) {
        val loadingDialog = QMUITipDialog.Builder(this)
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
            .setTipWord(getString(R.string.sending))
            .create()
        loadingDialog.show()

        GlobalScope.launch(Dispatchers.IO) {
            val props = Properties()
            props["mail.smtp.host"] = "smtp.example.com"
            props["mail.smtp.port"] = "587"
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.starttls.enable"] = "true"

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication("your_email@example.com", "your_password")
                }
            })

            try {
                val message = MimeMessage(session)
                message.setFrom(InternetAddress("your_email@example.com"))
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                message.subject = subject
                message.setText(body)

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
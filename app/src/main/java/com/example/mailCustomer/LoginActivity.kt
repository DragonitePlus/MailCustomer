package com.example.mailCustomer

import android.content.Intent
import android.os.Bundle
import android.text.InputType
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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : QMUIActivity() {
    private lateinit var topBar: QMUITopBarLayout
    private lateinit var groupListView: QMUIGroupListView
    private lateinit var loginButton: QMUIRoundButton
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        topBar = findViewById(R.id.topbar)
        topBar.setTitle(R.string.login)

        groupListView = findViewById(R.id.groupListView)
        val section = QMUIGroupListView.newSection(this)

        // 初始化 EditText 并设置输入类型
        emailEditText = EditText(this)
        passwordEditText = EditText(this)
        passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        // 使用 addItemView 添加自定义视图
        section.addItemView(createGroupListItemView(getString(R.string.email), emailEditText), null)
        section.addItemView(createGroupListItemView(getString(R.string.password), passwordEditText), null)

        section.addTo(groupListView)

        loginButton = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            login(email, password)
        }
    }

    private fun createGroupListItemView(title: String, editText: EditText): QMUICommonListItemView {
        val itemView = groupListView.createItemView(title)
        itemView.accessoryType = QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM
        itemView.addView(editText) // 将 EditText 作为子视图添加到 itemView 中
        return itemView
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun login(email: String, password: String) {
        val loadingDialog = QMUITipDialog.Builder(this)
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
            .setTipWord(getString(R.string.loading))
            .create()
        loadingDialog.show()

        GlobalScope.launch(Dispatchers.IO) {
            val url = URL("https://your-api-endpoint.com/login")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonInputString = JSONObject().apply {
                put("email", email)
                put("password", password)
            }.toString()

            connection.outputStream.use { os ->
                val input = jsonInputString.toByteArray(charset("utf-8"))
                os.write(input, 0, input.size)
            }

            val responseCode = connection.responseCode
            runOnUiThread {
                loadingDialog.dismiss()
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    QMUITipDialog.Builder(this@LoginActivity)
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                        .setTipWord(getString(R.string.login_failed))
                        .create()
                        .show()
                }
            }
        }
    }
}

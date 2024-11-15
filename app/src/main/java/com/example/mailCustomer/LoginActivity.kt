package com.example.mailCustomer

import android.content.Intent
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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : QMUIActivity() {
    private lateinit var topBar: QMUITopBarLayout
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 初始化顶部栏
        topBar = findViewById(R.id.topbar)
        topBar.setTitle(R.string.login)

        // 初始化输入字段
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            login(email, password)
        }
    }

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

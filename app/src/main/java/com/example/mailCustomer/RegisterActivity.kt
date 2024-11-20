     package com.example.mailCustomer

     import android.content.Intent
     import android.os.Bundle
     import android.util.Log
     import android.widget.EditText
     import android.widget.Toast
     import androidx.appcompat.app.AppCompatActivity
     import com.example.mailcostomer.R
     import com.google.android.material.appbar.MaterialToolbar
     import mehdi.sakout.fancybuttons.FancyButton
     import okhttp3.*
     import okhttp3.MediaType.Companion.toMediaType
     import okhttp3.RequestBody.Companion.toRequestBody
     import java.io.IOException

     class RegisterActivity : AppCompatActivity() {

         private lateinit var usernameInput: EditText
         private lateinit var passwordInput: EditText
         private lateinit var registerButton: FancyButton

         override fun onCreate(savedInstanceState: Bundle?) {
             super.onCreate(savedInstanceState)
             setContentView(R.layout.activity_register)

             val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
             usernameInput = findViewById(R.id.username)
             passwordInput = findViewById(R.id.password)
             registerButton = findViewById(R.id.registerButton)

             toolbar.setNavigationOnClickListener {
                 finish() // 返回上一页面
             }

             registerButton.setOnClickListener {
                 val username = usernameInput.text.toString().trim()
                 val password = passwordInput.text.toString().trim()

                 if (username.isEmpty() || password.isEmpty()) {
                     Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                 } else {
                     sendRegistrationRequest(username, password)
                 }
             }
         }

         private fun sendRegistrationRequest(username: String, password: String) {
             val client = OkHttpClient()

             // 创建 JSON 对象
             val json = """
                 {
                     "username": "$username",
                     "email": "$username@example.com",
                     "password": "$password"
                 }
             """.trimIndent()

             // 创建 JSON 请求体
             val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

             val request = Request.Builder()
                 .url("http://10.0.2.2:8080/user/register")
                 .post(requestBody)
                 .build()

             client.newCall(request).enqueue(object : Callback {
                 override fun onFailure(call: Call, e: IOException) {
                     runOnUiThread {
                         Toast.makeText(this@RegisterActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                     }
                     Log.e("RegisterActivity", "Network error: ${e.message}", e)  // 打印错误信息到后台
                 }

                 override fun onResponse(call: Call, response: Response) {
                     if (response.isSuccessful) {
                         runOnUiThread {
                             Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                             startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                             finish()
                         }
                     } else {
                         runOnUiThread {
                             Toast.makeText(this@RegisterActivity, "Registration failed: ${response.code} - ${response.message}", Toast.LENGTH_SHORT).show()
                         }
                         Log.e("RegisterActivity", "Registration failed: ${response.code} - ${response.message}")  // 打印错误信息到后台
                     }
                 }
             })
         }
     }

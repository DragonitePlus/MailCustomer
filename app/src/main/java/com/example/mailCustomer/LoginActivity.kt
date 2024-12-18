package com.example.mailCustomer

import TokenUtils
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mailcostomer.R
import mehdi.sakout.fancybuttons.FancyButton
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: FancyButton
    private lateinit var registerButton: FancyButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        loginButton.setOnClickListener {
            val user = username.text.toString()
            val pass = password.text.toString()

            if (user.isNotEmpty() && pass.isNotEmpty()) {
                sendLoginRequest(user, pass)
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun sendLoginRequest(user: String, pass: String) {
        val client = OkHttpClient()

        // 构建 JSON 请求体
        val json = JSONObject()
        json.put("username", user)
        json.put("password", pass)

        // 将 JSON 转换为 RequestBody
        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("http://10.0.2.2:8080/user/login") // 使用 HTTP
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Network Error: ${e.message}", e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBody = it.body?.string()
                        if (responseBody != null) {
                            try {
                                if (responseBody.startsWith("<!DOCTYPE")) {
                                    // 处理 HTML 响应
                                    runOnUiThread {
                                        Toast.makeText(this@LoginActivity, "Invalid credentials or server error", Toast.LENGTH_SHORT).show()
                                    }
                                    Log.e("LoginActivity", "Received HTML response instead of JSON\nResponse Body: $responseBody")
                                } else {
                                    val jsonObject = JSONObject(responseBody)
                                    if (jsonObject.has("token")) {
                                        val token = jsonObject.getString("token")

                                        // 解码token以获取username
                                        val username = TokenUtils.getUsernameFromToken(token)

                                        // Save token and username to SharedPreferences
                                        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                        sharedPreferences.edit().putString("auth_token", token).apply()
                                        username?.let { sharedPreferences.edit().putString("email_username", it).apply() }

                                        runOnUiThread {
                                            Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                            startActivity(intent)
                                            finish() // Optional: Prevent returning to the login screen
                                        }
                                    } else {
                                        // 处理没有 token 的情况
                                        runOnUiThread {
                                            Toast.makeText(this@LoginActivity, "Login Successful but no token found", Toast.LENGTH_SHORT).show()
                                        }
                                        Log.e("LoginActivity", "Login Successful but no token found\nResponse Body: $responseBody")
                                    }
                                }
                            } catch (e: JSONException) {
                                runOnUiThread {
                                    Toast.makeText(this@LoginActivity, "Failed to parse JSON: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                                Log.e("LoginActivity", "Failed to parse JSON: ${e.message}\nResponse Body: $responseBody", e)
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@LoginActivity, "Empty response body", Toast.LENGTH_SHORT).show()
                            }
                            Log.e("LoginActivity", "Empty response body")
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Login Failed: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                        Log.e("LoginActivity", "Login Failed: ${response.message}\nResponse Code: ${response.code}\nResponse Body: ${response.body?.string()}")
                    }
                }
            }
        })
    }
}

package com.example.mailCustomer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mailcostomer.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var usernameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var statusEditText: TextInputEditText
    private lateinit var roleEditText: TextInputEditText
    private lateinit var editButton: MaterialButton
    private lateinit var saveButton: MaterialButton

    private var isEditable = false // 是否可编辑

    private var userId: Int? = null // 添加类成员变量

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info)

        // 初始化控件
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        statusEditText = findViewById(R.id.statusEditText)
        roleEditText = findViewById(R.id.roleEditText)
        editButton = findViewById(R.id.editButton)
        saveButton = findViewById(R.id.saveButton)

        // 从Token中解析用户名
        val username = getUsernameFromToken()

        if (username != null) {
            fetchUserInfo(username)
        } else {
            Toast.makeText(this, "无法解析用户名", Toast.LENGTH_SHORT).show()
        }

        // 编辑按钮点击事件
        editButton.setOnClickListener {
            toggleEditable()
        }

        // 保存按钮点击事件
        saveButton.setOnClickListener {
                saveUserInfo()
        }

        toolbar.setNavigationOnClickListener {
            finish() // 返回上一页面
        }
    }

    /**
     * 获取SharedPreferences中存储的Token
     */
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

    /**
     * 切换可编辑状态
     */
    private fun toggleEditable() {
        isEditable = !isEditable
        usernameEditText.isEnabled = isEditable
        emailEditText.isEnabled = isEditable
        editButton.text = if (isEditable) "完成编辑" else "编辑"
    }

    /**
     * 请求后端获取用户信息
     */
    private fun fetchUserInfo(username: String) {
        val url = "http://10.0.2.2:8080/user/selectByUsername"
        val client = OkHttpClient()

        val requestBody = JSONObject().put("username", username).toString()
        val request = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@PersonalInfoActivity, "获取用户信息失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val userJson = JSONObject(responseBody!!)
                    userId = userJson.getInt("userId")
                    val user = User(
                        userId = userJson.getInt("userId"),
                        username = userJson.getString("username"),
                        email = userJson.getString("email"),
                        status = userJson.getString("status"),
                        role = userJson.getString("role")
                    )
                    runOnUiThread { fillUserInfo(user) }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@PersonalInfoActivity, "获取用户信息失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    /**
     * 填充用户信息到页面
     */
    private fun fillUserInfo(user: User) {
        usernameEditText.setText(user.username)
        emailEditText.setText(user.email)
        statusEditText.setText(user.status)
        roleEditText.setText(user.role)
    }

    /**
     * 保存用户信息到后端
     */
    private fun saveUserInfo() {
        val url = "http://10.0.2.2:8080/user/updateDetails"
        val client = OkHttpClient()

        val userJson = JSONObject()
            .put("userId", userId)
            .put("username", usernameEditText.text.toString())
            .put("email", emailEditText.text.toString())
            .put("status", statusEditText.text.toString())
            .put("role", roleEditText.text.toString())

        val request = Request.Builder()
            .url(url)
            .post(userJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@PersonalInfoActivity, "保存失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
            runOnUiThread {
                if (response.isSuccessful) {
                    Toast.makeText(this@PersonalInfoActivity, "保存成功，请重新登录", Toast.LENGTH_SHORT).show()
                    toggleEditable()

                    // 跳转到登录界面
                    val intent = Intent(this@PersonalInfoActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // 关闭当前活动
                } else {
                    Toast.makeText(this@PersonalInfoActivity, "保存失败", Toast.LENGTH_SHORT).show()
                }
            }
            }
        })
    }
}

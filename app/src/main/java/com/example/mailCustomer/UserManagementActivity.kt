package com.example.mailCustomer

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mailcostomer.R
import com.google.android.material.appbar.MaterialToolbar
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class UserManagementActivity : AppCompatActivity() {
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val recyclerView = findViewById<RecyclerView>(R.id.userRecyclerView)

        toolbar.setNavigationOnClickListener { finish() }

        userAdapter = UserAdapter(
            onEditClick = { user -> showEditUserDialog(user) },
            onBanClick = { user, button -> handleBanClick(user, button) },
            onSetAdminClick = { user, button -> handleSetAdminClick(user, button) },
            onDeleteClick = { user -> handleDeleteClick(user) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = userAdapter

        fetchUserData()
    }

    private fun fetchUserData() {
        val url = "http://10.0.2.2:8080/user/selectAll"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserManagementActivity, "获取用户数据失败", Toast.LENGTH_SHORT).show()
                }
                Log.e("UserManagement", "请求失败", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val json = JSONArray(responseBody.string())
                    val users = mutableListOf<User>()

                    for (i in 0 until json.length()) {
                        val userJson = json.getJSONObject(i)
                        val user = User(
                            userId = userJson.getInt("userId"),
                            username = userJson.getString("username"),
                            email = userJson.getString("email"),
                            status = userJson.getString("status"),
                            role = userJson.getString("role")
                        )
                        users.add(user)
                    }

                    runOnUiThread {
                        userAdapter.updateData(users)
                    }
                }
            }
        })
    }

    private fun handleBanClick(user: User, button: android.widget.Button) {
        val newStatus = if (user.status == "active") "banned" else "active"
        val url = if (user.status == "active") "http://10.0.2.2:8080/user/ban" else "http://10.0.2.2:8080/user/activate"
        val client = OkHttpClient()

        val requestBody = JSONObject()
            .put("username", user.username)
            .toString()
            .toRequestBody()

        val request = Request.Builder().url(url).post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserManagementActivity, "操作失败", Toast.LENGTH_SHORT).show()
                }
                Log.e("UserManagement", "封禁/启用请求失败", e)
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        user.status = newStatus
                        button.text = if (newStatus == "active") "封禁" else "启用"
                        Toast.makeText(this@UserManagementActivity, "操作成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@UserManagementActivity, "操作失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun handleSetAdminClick(user: User, button: android.widget.Button) {
        val newRole = if (user.role == "admin") "user" else "admin"
        val url = if (user.role == "admin") "http://10.0.2.2:8080/user/setUser" else "http://10.0.2.2:8080/user/setAdmin"
        val client = OkHttpClient()

        val requestBody = JSONObject()
            .put("username", user.username)
            .toString()
            .toRequestBody()

        val request = Request.Builder().url(url).post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserManagementActivity, "操作失败", Toast.LENGTH_SHORT).show()
                }
                Log.e("UserManagement", "设置管理员/用户请求失败", e)
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        user.role = newRole
                        button.text = if (newRole == "admin") "设置为用户" else "设置为管理员"
                        Toast.makeText(this@UserManagementActivity, "操作成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@UserManagementActivity, "操作失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun handleDeleteClick(user: User) {
        val url = "http://10.0.2.2:8080/user/delete"
        val client = OkHttpClient()

        val requestBody = JSONObject()
            .put("username", user.username)
            .toString()
            .toRequestBody()

        val request = Request.Builder().url(url).post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserManagementActivity, "删除失败", Toast.LENGTH_SHORT).show()
                }
                Log.e("UserManagement", "删除请求失败", e)
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        userAdapter.updateData(userAdapter.users.filter { it != user })
                        Toast.makeText(this@UserManagementActivity, "删除成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@UserManagementActivity, "删除失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
     /**
     * 显示编辑用户信息的对话框
     */
    private fun showEditUserDialog(user: User) {
         val builder = AlertDialog.Builder(this)
         val view = layoutInflater.inflate(R.layout.dialog_edit_user, null)
         val usernameEditText =
             view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.usernameEditText)
         val emailEditText =
             view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.emailEditText)
         val statusEditText =
             view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.statusEditText)
         val roleEditText =
             view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.roleEditText)

         // 预填充当前用户数据
         usernameEditText.setText(user.username)
         emailEditText.setText(user.email)
         statusEditText.setText(user.status)
         roleEditText.setText(user.role)

         builder.setView(view)
             .setTitle("编辑用户")
             .setPositiveButton("保存") { _, _ ->
                 val updatedUser = user.copy(
                     username = usernameEditText.text.toString(),
                     email = emailEditText.text.toString(),
                     status = statusEditText.text.toString(),
                     role = roleEditText.text.toString()
                 )
                 updateUser(updatedUser)
             }
             .setNegativeButton("取消", null)
             .show()
     }

    private fun updateUser(user: User) {
        val url = "http://10.0.2.2:8080/user/updateDetails"
        val client = OkHttpClient()

        val requestBody = JSONObject()
            .put("userId", user.userId)
            .put("username", user.username)
            .put("email", user.email)
            .put("status", user.status)
            .put("role", user.role)
            .toString()
            .toRequestBody()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserManagementActivity, "更新失败", Toast.LENGTH_SHORT).show()
                }
                Log.e("UserManagement", "更新失败", e)
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@UserManagementActivity, "用户更新成功", Toast.LENGTH_SHORT).show()
                        fetchUserData()
                    } else {
                        Toast.makeText(this@UserManagementActivity, "更新失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}

/**
 * 扩展函数，将 JSON 字符串转为请求体
 */
private fun String.toRequestBody(): RequestBody {
    val mediaType = "application/json; charset=utf-8".toMediaType()
    return RequestBody.create(mediaType, this)
}

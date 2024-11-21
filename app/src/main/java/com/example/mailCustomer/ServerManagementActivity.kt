package com.example.mailCustomer

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mailcostomer.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class ServerManagementActivity : AppCompatActivity() {
    private lateinit var smtpPort: EditText
    private lateinit var pop3Port: EditText
    private lateinit var domain: EditText
    private lateinit var saveButton: Button
    private lateinit var smtpControlButton: Button
    private lateinit var pop3ControlButton: Button
    private var isSmtpRunning = true
    private var isPop3Running = true


override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_server_management)

    val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
    smtpPort = findViewById(R.id.smtpPort)
    pop3Port = findViewById(R.id.pop3Port)
    domain = findViewById(R.id.domain)
    saveButton = findViewById(R.id.saveButton)
    smtpControlButton = findViewById(R.id.smtpControlButton)
    pop3ControlButton = findViewById(R.id.pop3ControlButton)

    fetchServerConfig()

    saveButton.setOnClickListener {
        saveServerConfig()
    }

    smtpControlButton.setOnClickListener {
        toggleServer("smtp", isSmtpRunning)
        isSmtpRunning = !isSmtpRunning
        smtpControlButton.text = if (isSmtpRunning) "关闭 SMTP 服务器" else "开启 SMTP 服务器"
    }

    pop3ControlButton.setOnClickListener {
        toggleServer("pop3", isPop3Running)
        isPop3Running = !isPop3Running
        pop3ControlButton.text = if (isPop3Running) "关闭 POP3 服务器" else "开启 POP3 服务器"
    }

    toolbar.setNavigationOnClickListener {
        finish()
    }
}

    private fun fetchServerConfig() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/server/getPorts") // 替换为服务端地址
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ServerManagementActivity, "获取服务器信息失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        // 使用 Gson 解析 JSON 响应
                        val gson = Gson()
                        val serverResponse = gson.fromJson(responseBody, ServerResponse::class.java)

                        runOnUiThread {
                            smtpPort.setText(serverResponse.smtpPort.toString())
                            pop3Port.setText(serverResponse.pop3Port.toString())
                            domain.setText(serverResponse.domain)
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ServerManagementActivity, "获取服务器信息失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun toggleServer(type: String, isRunning: Boolean) {
    val action = if (isRunning) "stop" else "start"
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://10.0.2.2:8080/server/$action$type") // 替换为服务端地址
        .get()
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            runOnUiThread {
                Toast.makeText(this@ServerManagementActivity, "$type 服务器控制失败", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                runOnUiThread {
                    Toast.makeText(this@ServerManagementActivity, "$type 服务器已${if (isRunning) "关闭" else "开启"}", Toast.LENGTH_SHORT).show()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this@ServerManagementActivity, "$type 服务器控制失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    })
}

    private fun saveServerConfig() {
    val client = OkHttpClient()
    val json = JSONObject()
    json.put("smtpPort", smtpPort.text.toString())
    json.put("pop3Port", pop3Port.text.toString())
    json.put("domain", domain.text.toString())

    val body = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
    val request = Request.Builder()
        .url("http://10.0.2.2:8080/server/updatePorts")
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            runOnUiThread {
                Toast.makeText(this@ServerManagementActivity, "保存失败", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {  // 检查响应是否成功
                runOnUiThread {
                    Toast.makeText(this@ServerManagementActivity, "保存成功", Toast.LENGTH_SHORT).show()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this@ServerManagementActivity, "保存失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    })
}

}

// 定义数据类来表示 JSON 响应
data class ServerResponse(
    val pop3Port: Int,
    val smtpPort: Int,
    val domain: String
)

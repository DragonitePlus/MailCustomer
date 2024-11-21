package com.example.mailCustomer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mailcostomer.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException

class SystemLogsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var logAdapter: LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_logs)

        // 初始化 RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 设置 Adapter
        logAdapter = LogAdapter(mutableListOf())
        recyclerView.adapter = logAdapter

        // 获取系统公告数据
        fetchLogs()
    }

    private fun fetchLogs() {
        val url = "http://10.0.2.2:8080/log/get"
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@SystemLogsActivity, "获取公告失败，请检查网络连接", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { json ->
                        val logListType = object : TypeToken<List<LogItem>>() {}.type
                        val logs: List<LogItem> = Gson().fromJson(json, logListType)

                        runOnUiThread {
                            logAdapter.updateLogs(logs)
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@SystemLogsActivity, "获取公告失败，服务器错误", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}

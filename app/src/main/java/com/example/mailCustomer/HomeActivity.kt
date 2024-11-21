package com.example.mailCustomer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mailcostomer.R
import com.github.florent37.shapeofview.shapes.RoundRectView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val inboxBlock = findViewById<RoundRectView>(R.id.inboxBlock)
        val sendMailBlock = findViewById<RoundRectView>(R.id.sendMailBlock)
        val serverManagementBlock = findViewById<RoundRectView>(R.id.serverManagementBlock)

        // 从 SharedPreferences 获取 Token
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", "")

        // 判断是否为管理员
        val isAdmin = TokenUtils.isAdminFromToken(token ?: "")

        // 动态显示服务器管理模块
        if (isAdmin) {
            serverManagementBlock.visibility = View.VISIBLE
        }

        // 点击事件绑定
        inboxBlock.setOnClickListener {
            val intent = Intent(this, InboxActivity::class.java)
            startActivity(intent)
        }

        sendMailBlock.setOnClickListener {
            val intent = Intent(this, SendMailActivity::class.java)
            startActivity(intent)
        }

        serverManagementBlock.setOnClickListener {
            val intent = Intent(this, ServerManagementActivity::class.java)
            startActivity(intent)
        }
    }
}

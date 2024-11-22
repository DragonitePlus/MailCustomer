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

        // 获取界面中的卡片视图
        val inboxBlock = findViewById<RoundRectView>(R.id.inboxBlock)
        val sendMailBlock = findViewById<RoundRectView>(R.id.sendMailBlock)
        val personalInfo = findViewById<RoundRectView>(R.id.personalInfoBlock)
        val mailBoxManagementBlock = findViewById<RoundRectView>(R.id.mailBoxManagementBlock)
        val serverManagementBlock = findViewById<RoundRectView>(R.id.serverManagementBlock)
        val systemLogs = findViewById<RoundRectView>(R.id.systemLogs)
        val userManagementBlock = findViewById<RoundRectView>(R.id.userManagementBlock)

        // 从 SharedPreferences 获取 Token
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", "")

        // 判断是否为管理员
        val isAdmin = TokenUtils.isAdminFromToken(token ?: "")

        // 动态显示管理员相关模块
        if (isAdmin) {
            serverManagementBlock.visibility = View.VISIBLE
            systemLogs.visibility = View.VISIBLE
            userManagementBlock.visibility = View.VISIBLE
        }

        // 绑定点击事件
        setupClickListeners(
            inboxBlock to InboxActivity::class.java,
            sendMailBlock to SendMailActivity::class.java,
            personalInfo to PersonalInfoActivity::class.java,
            mailBoxManagementBlock to MailBoxManagementActivity::class.java,
            serverManagementBlock to ServerManagementActivity::class.java,
            systemLogs to SystemLogsActivity::class.java,
            userManagementBlock to UserManagementActivity::class.java
        )
    }

    /**
     * 封装点击事件绑定逻辑
     */
    private fun setupClickListeners(vararg viewsAndActivities: Pair<View, Class<*>>) {
        for ((view, activity) in viewsAndActivities) {
            view.setOnClickListener {
                val intent = Intent(this, activity)
                startActivity(intent)
            }
        }
    }
}

package com.example.mailCustomer

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mailcostomer.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class MailBoxManagementActivity : AppCompatActivity() {


    // 模拟本地数据存储
    private var maxMailboxSize: Int = 50 // 默认邮箱大小
    private val blockedAccounts = mutableSetOf<String>()
    private val blockedIPs = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_mailbox_management)

    val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
    val mailboxSizePicker = findViewById<NumberPicker>(R.id.mailboxSizePicker)
    val accountInput = findViewById<EditText>(R.id.accountInput)
    val addAccountButton = findViewById<Button>(R.id.addAccountButton)
    val accountChipGroup = findViewById<ChipGroup>(R.id.accountChipGroup)
    val ipInput = findViewById<EditText>(R.id.ipInput)
    val addIpButton = findViewById<Button>(R.id.addIpButton)
    val ipChipGroup = findViewById<ChipGroup>(R.id.ipChipGroup)

    // 初始化 SharedPreferences
    val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
    maxMailboxSize = sharedPreferences.getInt("maxMailboxSize", 50)
    blockedAccounts.addAll(sharedPreferences.getStringSet("blockedAccounts", emptySet())!!)
    blockedIPs.addAll(sharedPreferences.getStringSet("blockedIPs", emptySet())!!)

    // 设置 NumberPicker 的默认值
    mailboxSizePicker.minValue = 10
    mailboxSizePicker.maxValue = 200
    mailboxSizePicker.value = maxMailboxSize
    mailboxSizePicker.setOnValueChangedListener { _, _, newVal ->
        maxMailboxSize = newVal
        sharedPreferences.edit().putInt("maxMailboxSize", maxMailboxSize).apply()
        Toast.makeText(this, "邮箱大小设置为 $maxMailboxSize MB", Toast.LENGTH_SHORT).show()
    }

    toolbar.setNavigationOnClickListener {
        finish() // 返回上一页面
    }

    // 动态加载已存储的账号过滤列表
    for (account in blockedAccounts) {
        addChip(accountChipGroup, account, isAccount = true)
        println(account)
    }

    // 动态加载已存储的 IP 过滤列表
    for (ip in blockedIPs) {
        addChip(ipChipGroup, ip, isAccount = false)
    }

    // 添加账号过滤
    addAccountButton.setOnClickListener {
        val account = accountInput.text.toString().trim()
        if (account.isNotEmpty() && blockedAccounts.add(account)) {
            addChip(accountChipGroup, account, isAccount = true)
            sharedPreferences.edit().putStringSet("blockedAccounts", blockedAccounts).apply()
            accountInput.text?.clear()
        } else {
            Toast.makeText(this, "请输入有效账号，或该账号已存在", Toast.LENGTH_SHORT).show()
        }
    }

    // 添加 IP 过滤
    addIpButton.setOnClickListener {
        val ip = ipInput.text.toString().trim()
        if (ip.isNotEmpty() && blockedIPs.add(ip)) {
            addChip(ipChipGroup, ip, isAccount = false)
            sharedPreferences.edit().putStringSet("blockedIPs", blockedIPs).apply()
            ipInput.text?.clear()
        } else {
            Toast.makeText(this, "请输入有效 IP，或该 IP 已存在", Toast.LENGTH_SHORT).show()
        }
    }
}


    private fun addChip(chipGroup: ChipGroup, text: String, isAccount: Boolean) {
    val chip = Chip(this).apply {
        this.text = text
        isCloseIconVisible = true
        setOnCloseIconClickListener {
            chipGroup.removeView(this)
            if (isAccount) {
                blockedAccounts.remove(text)
                getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit().putStringSet("blockedAccounts", blockedAccounts).apply()
            } else {
                blockedIPs.remove(text)
                getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit().putStringSet("blockedIPs", blockedIPs).apply()
            }
        }
    }
    chipGroup.addView(chip)
}

}


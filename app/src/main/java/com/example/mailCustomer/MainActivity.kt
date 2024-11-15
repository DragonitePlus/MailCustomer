package com.example.mailCustomer

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.mailcostomer.R
import com.qmuiteam.qmui.arch.QMUIActivity
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView

class MainActivity : QMUIActivity() {
    private lateinit var topBar: QMUITopBarLayout
    private lateinit var groupListView: QMUIGroupListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        topBar = findViewById(R.id.topbar)
        topBar.setTitle(R.string.app_name)

        groupListView = findViewById(R.id.groupListView)
        val section = QMUIGroupListView.newSection(this)

        section.addItemView(groupListView.createItemView(getString(R.string.inbox)), View.OnClickListener {
            startActivity(Intent(this, InboxActivity::class.java))
        })

        section.addItemView(groupListView.createItemView(getString(R.string.compose)), View.OnClickListener {
            startActivity(Intent(this, ComposeActivity::class.java))
        })

        section.addTo(groupListView)
    }
}
package com.example.mailCustomer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.mailcostomer.R
import com.qmuiteam.qmui.arch.QMUIActivity
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout
import com.qmuiteam.qmui.widget.section.QMUISection
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter
import com.qmuiteam.qmui.widget.section.QMUIStickySectionLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import javax.mail.*

class InboxActivity : QMUIActivity() {
    private lateinit var topBar: QMUITopBarLayout
    private lateinit var pullRefreshLayout: QMUIPullRefreshLayout
    private lateinit var sectionLayout: QMUIStickySectionLayout
    private lateinit var adapter: EmailAdapter
    private val emailList = mutableListOf<EmailItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        topBar = findViewById(R.id.topbar)
        topBar.setTitle(R.string.inbox)
        topBar.addLeftBackImageButton().setOnClickListener { finish() }

        pullRefreshLayout = findViewById(R.id.pullRefreshLayout)
        pullRefreshLayout.setOnPullListener(object : QMUIPullRefreshLayout.OnPullListener {
            override fun onMoveTarget(offset: Int) {}
            override fun onMoveRefreshView(offset: Int) {}
            override fun onRefresh() {
                fetchEmails()
            }
        })

        sectionLayout = findViewById(R.id.sectionLayout)
        adapter = EmailAdapter()
        sectionLayout.setAdapter(adapter)

        fetchEmails()
    }

    private fun fetchEmails() {
        GlobalScope.launch(Dispatchers.IO) {
            val props = Properties()
            props["mail.pop3.host"] = "pop.example.com"
            props["mail.pop3.port"] = "995"
            props["mail.pop3.starttls.enable"] = "true"
            val session = Session.getDefaultInstance(props)

            try {
                val store = session.getStore("pop3s")
                store.connect("pop.example.com", "your_email@example.com", "your_password")

                val folder = store.getFolder("INBOX")
                folder.open(Folder.READ_ONLY)

                val messages = folder.messages
                emailList.clear()
                for (message in messages) {
                    val subject = message.subject
                    val from = message.from[0].toString()
                    emailList.add(EmailItem(from, subject))
                }

                folder.close(false)
                store.close()

                runOnUiThread {
                    adapter.setData(createSections())
                    pullRefreshLayout.finishRefresh()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    pullRefreshLayout.finishRefresh()
                }
            }
        }
    }

    private fun createSections(): List<QMUISection<EmailItem, EmailItem>> {
        val section = QMUISection(EmailItem("Header", "Emails"), emailList)
        return listOf(section)
    }

    inner class EmailAdapter : QMUIStickySectionAdapter<EmailItem, EmailItem, EmailAdapter.EmailViewHolder>() {

        inner class EmailViewHolder(itemView: View) : ViewHolder(itemView) {
            val fromTextView: TextView = itemView.findViewById(R.id.fromTextView)
            val subjectTextView: TextView = itemView.findViewById(R.id.subjectTextView)
        }

        override fun onCreateSectionHeaderViewHolder(parent: ViewGroup): EmailViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_email, parent, false)
            return EmailViewHolder(view)
        }

        override fun onCreateSectionItemViewHolder(parent: ViewGroup): EmailViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_email, parent, false)
            return EmailViewHolder(view)
        }

        override fun onCreateSectionLoadingViewHolder(viewGroup: ViewGroup): EmailViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_email, viewGroup, false)
            return EmailViewHolder(view)
        }

        override fun onCreateCustomItemViewHolder(
            viewGroup: ViewGroup,
            type: Int
        ): EmailViewHolder {
            return EmailViewHolder(viewGroup)
        }

        override fun onBindSectionHeader(holder: EmailViewHolder, sectionIndex: Int, section: QMUISection<EmailItem, EmailItem>) {
            // 绑定标题项内容
        }
    }

    data class EmailItem(val from: String, val subject: String) : QMUISection.Model<EmailItem> {

        override fun isSameItem(other: EmailItem): Boolean {
            return this.from == other.from && this.subject == other.subject
        }

        override fun cloneForDiff(): EmailItem {
            return this.copy() // 使用 Kotlin 的 `copy()` 方法进行克隆
        }

        override fun isSameContent(other: EmailItem?): Boolean {
            return this == other
        }
    }
}

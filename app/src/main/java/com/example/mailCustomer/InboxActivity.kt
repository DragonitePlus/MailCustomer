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
import kotlinx.coroutines.DelicateCoroutinesApi
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

    @OptIn(DelicateCoroutinesApi::class)
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
                    adapter.setData(listOf(QMUISection(EmailHeader("Inbox"), emailList)))
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

    inner class EmailAdapter : QMUIStickySectionAdapter<EmailHeader, EmailItem, QMUIStickySectionAdapter.ViewHolder>() {

        override fun onCreateSectionHeaderViewHolder(viewGroup: ViewGroup): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_section_header, viewGroup, false)
            return SectionHeaderViewHolder(view)
        }

        override fun onCreateSectionItemViewHolder(viewGroup: ViewGroup): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_email, viewGroup, false)
            return EmailViewHolder(view)
        }

        override fun onCreateSectionLoadingViewHolder(viewGroup: ViewGroup): ViewHolder {
            // 创建用于加载视图的 ViewHolder
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_loading, viewGroup, false)
            return LoadingViewHolder(view)
        }

        // 创建 LoadingViewHolder 类
        inner class LoadingViewHolder(itemView: View) : ViewHolder(itemView)

        override fun onCreateCustomItemViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            TODO("Not yet implemented")
        }

        override fun onBindSectionHeader(holder: ViewHolder, position: Int, section: QMUISection<EmailHeader, EmailItem>) {
            val headerHolder = holder as SectionHeaderViewHolder
            headerHolder.titleTextView.text = section.header.title
        }


        inner class SectionHeaderViewHolder(itemView: View) : QMUIStickySectionAdapter.ViewHolder(itemView) {
            val titleTextView: TextView = itemView.findViewById(R.id.headerTitleTextView)
        }

        inner class EmailViewHolder(itemView: View) : QMUIStickySectionAdapter.ViewHolder(itemView) {
            val fromTextView: TextView = itemView.findViewById(R.id.fromTextView)
            val subjectTextView: TextView = itemView.findViewById(R.id.subjectTextView)
        }

    }


    data class EmailHeader(val title: String) : QMUISection.Model<EmailHeader> {
        override fun cloneForDiff(): EmailHeader = copy()
        override fun isSameItem(other: EmailHeader): Boolean = title == other.title
        override fun isSameContent(other: EmailHeader): Boolean = this == other
    }

    data class EmailItem(val from: String, val subject: String) : QMUISection.Model<EmailItem> {
        override fun cloneForDiff(): EmailItem = copy()
        override fun isSameItem(other: EmailItem): Boolean = from == other.from && subject == other.subject
        override fun isSameContent(other: EmailItem): Boolean = this == other
    }
}
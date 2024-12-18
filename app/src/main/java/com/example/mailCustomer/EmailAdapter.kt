package com.example.mailCustomer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mailcostomer.R
import com.google.android.material.button.MaterialButton

class EmailAdapter(
    private var emails: List<Email>, // 改为可变变量
    private val onClick: (Email) -> Unit,
    private val onDelete: (Email) -> Unit
) : RecyclerView.Adapter<EmailAdapter.EmailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_email, parent, false)
        return EmailViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
        val email = emails[position]
        holder.senderView.text = email.sender
        holder.titleView.text = email.title
        holder.cardView.setOnClickListener { onClick(email) }
        holder.deleteButton.setOnClickListener { onDelete(email) }
    }

    override fun getItemCount() = emails.size

    class EmailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val senderView: TextView = view.findViewById(R.id.senderView)
        val titleView: TextView = view.findViewById(R.id.titleView)
        val cardView: CardView = view.findViewById(R.id.cardView)
        val deleteButton: MaterialButton = view.findViewById(R.id.deleteButton) // 假设你有一个删除按钮
    }

    fun updateEmails(newEmails: List<Email>) {
        this.emails = newEmails
        notifyDataSetChanged()
    }
}

package com.example.mailCustomer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mailcostomer.R

class LogAdapter(private var logList: MutableList<LogItem>) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    fun updateLogs(newLogs: List<LogItem>) {
        logList.clear()
        logList.addAll(newLogs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val logItem = logList[position]
        holder.title.text = logItem.type
        holder.content.text = logItem.content
        holder.timestamp.text = logItem.createdTime
    }

    override fun getItemCount(): Int = logList.size

    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.logTitle)
        val content: TextView = itemView.findViewById(R.id.logContent)
        val timestamp: TextView = itemView.findViewById(R.id.logTimestamp)
    }
}

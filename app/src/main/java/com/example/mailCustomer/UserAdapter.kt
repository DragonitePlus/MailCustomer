package com.example.mailCustomer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mailcostomer.R

class UserAdapter(
    private val onEditClick: (User) -> Unit,
    private val onBanClick: (User, Button) -> Unit,
    private val onSetAdminClick: (User, Button) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    val users = mutableListOf<User>()

    fun updateData(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)

        // 设置按钮的点击事件
        holder.editButton.setOnClickListener { onEditClick(user) }
        holder.banButton.setOnClickListener {
            onBanClick(user, holder.banButton) // 传递用户和按钮本身
        }
        holder.setAdminButton.setOnClickListener {
            onSetAdminClick(user, holder.setAdminButton) // 传递用户和按钮本身
        }
        holder.deleteButton.setOnClickListener { onDeleteClick(user) }
    }

    override fun getItemCount(): Int = users.size

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        private val emailTextView: TextView = view.findViewById(R.id.emailTextView)
        private val statusTextView: TextView = view.findViewById(R.id.statusTextView)
        private val roleTextView: TextView = view.findViewById(R.id.roleTextView)
        val editButton: Button = view.findViewById(R.id.editButton)
        val banButton: Button = view.findViewById(R.id.banButton)
        val setAdminButton: Button = view.findViewById(R.id.setAdminButton)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)

        fun bind(user: User) {
            usernameTextView.text = user.username
            emailTextView.text = user.email
            statusTextView.text = "状态：${user.status}"
            roleTextView.text = "角色：${user.role}"

            // 初始化按钮文本
            banButton.text = if (user.status == "active") "封禁" else "启用"
            setAdminButton.text = if (user.role == "admin") "设置为用户" else "设置为管理员"
        }
    }
}

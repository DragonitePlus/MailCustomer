package com.example.mailCustomer


import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Session
import javax.mail.internet.InternetAddress

class InboxViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _emailList = MutableLiveData<List<Email>>()
    val emailList: LiveData<List<Email>> get() = _emailList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // 获取最大邮箱大小，单位：MB
    private fun getMaxMailboxSize(): Int {
        return sharedPreferences.getInt("maxMailboxSize", 50) // 默认为50MB
    }

    // 获取当前邮箱已使用的大小，单位：字节
    private fun getCurrentMailboxSize(): Long {
        return sharedPreferences.getLong("currentMailboxSize", 0L)
    }

    // 更新当前邮箱已使用的大小
    private fun updateMailboxSize(newSize: Long) {
        sharedPreferences.edit().putLong("currentMailboxSize", newSize).apply()
    }

    // 获取邮件凭证
    private fun getEmailCredentials(): String {
        return sharedPreferences.getString("email_username", "") ?: ""
    }

    fun fetchEmails(pop3Host: String, pop3Port: Int) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val credentials = getEmailCredentials()
                if (credentials.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "Email credentials not found"
                    }
                    return@launch
                }

                val maxMailboxSize = getMaxMailboxSize() * 1024 * 1024 // 最大邮箱大小（字节）
                var currentMailboxSize = getCurrentMailboxSize() // 当前邮箱已使用大小

                val props = Properties().apply {
                    put("mail.store.protocol", "pop3")
                    put("mail.pop3.host", pop3Host)
                    put("mail.pop3.port", pop3Port.toString())
                }

                val session = Session.getInstance(props, null)
                val store = session.getStore("pop3")
                store.connect(pop3Host, credentials)

                val inbox = store.getFolder("INBOX")
                inbox.open(Folder.READ_ONLY)

                val messages = inbox.messages
                val fetchedEmails = mutableListOf<Email>()

                for (message in messages) {
                    val messageSize = message.size.toLong() // 邮件大小（字节）

                    // 判断当前已使用大小加上邮件大小是否超过最大限制
                    if (currentMailboxSize + messageSize <= maxMailboxSize) {
                        val sender = if (message.from != null && message.from.isNotEmpty()) {
                            extractEmailAddress(message.from[0].toString())
                        } else {
                            "sender@example.com"
                        }
                        val title = message.subject ?: "Title"
                        val contentPreview = extractContentPreview(message.content)

                        // 将邮件添加到列表，并更新已使用的邮箱大小
                        fetchedEmails.add(Email(sender, title, contentPreview))
                        currentMailboxSize += messageSize // 累加已使用的空间
                    } else {
                        // 如果超过最大邮箱大小，停止下载邮件
                        withContext(Dispatchers.Main) {
                            _errorMessage.value = "Mailbox is full, no more emails can be downloaded"
                        }
                        break
                    }
                }

                // 更新当前邮箱大小
                updateMailboxSize(currentMailboxSize)

                // 过滤掉发送者是过滤账户的邮件
                val filteredEmails = filterEmails(fetchedEmails)

                withContext(Dispatchers.Main) {
                    _emailList.value = filteredEmails
                    _isLoading.value = false
                }

                inbox.close(false)
                store.close()
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _errorMessage.value = e.message ?: "An error occurred"
                    _isLoading.value = false
                }
            }
        }
    }

    private fun filterEmails(emails: List<Email>): List<Email> {
        val blockedSenders = sharedPreferences.getStringSet("blockedAccounts", emptySet())?.map { "$it@example.com" } ?: emptyList()
        return emails.filter { email ->
            email.sender !in blockedSenders
        }
    }

    fun deleteEmail(emailIndex: Int, pop3Host: String, pop3Port: Int) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val props = Properties().apply {
                    put("mail.store.protocol", "pop3")
                    put("mail.pop3.host", pop3Host)
                    put("mail.pop3.port", pop3Port.toString())
                }

                val session = Session.getInstance(props, null)
                val store = session.getStore("pop3")
                store.connect(pop3Host, getEmailCredentials())

                val inbox = store.getFolder("INBOX")
                inbox.open(Folder.READ_WRITE)

                val messages = inbox.messages
                if (emailIndex < 0 || emailIndex >= messages.size) {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "Invalid email index"
                        _isLoading.value = false
                    }
                    return@launch
                }

                val messageToDelete = messages[emailIndex]
                messageToDelete.setFlag(Flags.Flag.DELETED, true)

                inbox.close(true)
                store.close()

                // 更新 UI 列表
                val updatedEmails = _emailList.value?.toMutableList()?.apply {
                    removeAt(emailIndex)
                } ?: emptyList()

                withContext(Dispatchers.Main) {
                    _emailList.value = updatedEmails
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _errorMessage.value = e.message ?: "An error occurred while deleting email"
                    _isLoading.value = false
                }
            }
        }
    }

    private fun extractContentPreview(content: Any): String {
        return try {
            when (content) {
                is String -> content.take(100)
                is javax.mail.Multipart -> extractTextFromMimeMultipart(content).take(100)
                else -> "Unsupported content type"
            }
        } catch (e: Exception) {
            "Error extracting content preview"
        }
    }

    private fun extractTextFromMimeMultipart(multipart: javax.mail.Multipart): String {
        val result = StringBuilder()
        for (i in 0 until multipart.count) {
            val bodyPart = multipart.getBodyPart(i)
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.content.toString())
            }
        }
        return result.toString()
    }

    private fun extractEmailAddress(sender: String): String {
        return try {
            val internetAddress = InternetAddress(sender)
            internetAddress.address ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}

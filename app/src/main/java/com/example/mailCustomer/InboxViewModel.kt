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

    // 使用 Application 或 SharedPreferences 中的配置来获取邮件账户凭据
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
                val fetchedEmails = messages.map { message ->
                    try {
                        val sender = if (message.from != null && message.from.isNotEmpty()) {
                            extractEmailAddress(message.from[0].toString())
                        } else {
                            "sender@example.com"
                        }
                        val title = message.subject ?: "Title"
                        val contentPreview = extractContentPreview(message.content)
                        Email(sender, title, contentPreview)
                    } catch (e: Exception) {
                        Email("sender@example.com", "Title", "Content")
                    }
                }

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
        println(blockedSenders)
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

                // 找到指定索引的邮件
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

                // 关闭文件夹并保存更改
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




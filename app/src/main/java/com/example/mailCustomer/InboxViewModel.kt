package com.example.mailCustomer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Properties
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.InternetAddress

class InboxViewModel : ViewModel() {
    private val _emailList = MutableLiveData<List<Email>>()
    val emailList: LiveData<List<Email>> get() = _emailList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun fetchEmails(credentials: String) {
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val username = credentials
                if (username.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "Email credentials not found"
                    }
                    return@launch
                }

                val props = Properties().apply {
                    put("mail.store.protocol", "pop3")
                    put("mail.pop3.host", "10.0.2.2")
                    put("mail.pop3.port", "1100")
                }

                val session = Session.getInstance(props, null)
                val store = session.getStore("pop3")
                store.connect("10.0.2.2", username)

                val inbox = store.getFolder("INBOX")
                inbox.open(Folder.READ_ONLY)

                val messages = inbox.messages
                Log.d("InboxViewModel", "Total messages: ${messages.size}")
                println(messages.toString())

                val fetchedEmails = messages.map { message ->
                    try {
                        val sender = if (message.from != null && message.from.isNotEmpty()) {
                            extractEmailAddress(message.from[0].toString())
                        } else {
                            "sender@example.com"
                        }
                        val title = message.subject
                        val contentPreview = extractContentPreview(message.content)
                        Email(sender, title, contentPreview)
                    } catch (e: MessagingException) {
                        Log.e("InboxViewModel", "Error parsing email: ${e.message}")
                        Email("sender@example.com", "Title", "test")
                    } catch (e: IOException) {
                        Log.e("InboxViewModel", "IO Error: ${e.message}")
                        Email("sender@example.com", "Title", "test")
                    }
                }

                withContext(Dispatchers.Main) {
                    _emailList.value = fetchedEmails
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

    fun deleteEmail(credentials: String, emailIndex: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val username = credentials
                if (username.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "Email credentials not found"
                    }
                    return@launch
                }

                val props = Properties().apply {
                    put("mail.store.protocol", "pop3")
                    put("mail.pop3.host", "10.0.2.2")
                    put("mail.pop3.port", "1100")
                }

                val session = Session.getInstance(props, null)
                val store = session.getStore("pop3")
                store.connect("10.0.2.2", username)

                val inbox = store.getFolder("INBOX")
                inbox.open(Folder.READ_WRITE)

                val message = inbox.getMessage(emailIndex + 1) // POP3索引从1开始
                message.setFlag(Flags.Flag.DELETED, true)
                inbox.close(true) // 关闭并删除标记的邮件
                store.close()

                withContext(Dispatchers.Main) {
                    _emailList.value = _emailList.value?.filterIndexed { index, _ -> index != emailIndex }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _errorMessage.value = e.message ?: "An error occurred"
                }
            }
        }
    }

    private fun extractContentPreview(content: Any): String {
        // Truncate content to display only the first 100 characters
        return try {
            when (content) {
                is String -> content.take(100)
                is javax.mail.Multipart -> extractTextFromMimeMultipart(content).take(100)
                else -> "Unsupported content type"
            }
        } catch (e: Exception) {
            Log.e("InboxViewModel", "Error extracting content preview: ${e.message}")
            "Error extracting content preview"
        }
    }

    private fun extractTextFromMimeMultipart(multipart: javax.mail.Multipart): String {
        val result = StringBuilder()
        for (i in 0 until multipart.count) {
            val bodyPart = multipart.getBodyPart(i)
            try {
                if (bodyPart.isMimeType("text/plain")) {
                    result.append(bodyPart.content.toString())
                } else if (bodyPart.content is javax.mail.Multipart) {
                    result.append(extractTextFromMimeMultipart(bodyPart.content as javax.mail.Multipart))
                }
            } catch (e: MessagingException) {
                Log.e("InboxViewModel", "Error extracting text from MIME multipart: ${e.message}")
            } catch (e: IOException) {
                Log.e("InboxViewModel", "IO Error extracting text from MIME multipart: ${e.message}")
            }
        }
        return result.toString()
    }

    private fun extractEmailAddress(sender: String): String {
        return try {
            val internetAddress = InternetAddress(sender)
            internetAddress.address ?: "Unknown"
        } catch (e: Exception) {
            Log.e("InboxViewModel", "Error parsing sender address: ${e.message}")
            "Unknown"
        }
    }
}

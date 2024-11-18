package com.example.mailCustomer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Folder
import javax.mail.Session
import javax.mail.internet.InternetAddress

class InboxViewModel : ViewModel() {
    private val _emailList = MutableLiveData<List<Email>>()
    val emailList: LiveData<List<Email>> get() = _emailList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun fetchEmails(credentials: Pair<String, String>) {
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val (username, password) = credentials
                if (username.isEmpty() || password.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "Email credentials not found"
                    }
                    return@launch
                }

                val props = Properties().apply {
                    put("mail.store.protocol", "pop3")
                    put("mail.pop3.host", "pop.163.com")
                    put("mail.pop3.port", "995")
                    put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                    put("mail.pop3.socketFactory.fallback", "false")
                }

                val session = Session.getInstance(props, null)
                val store = session.getStore("pop3")
                store.connect("pop.163.com", username, password)

                val inbox = store.getFolder("INBOX")
                inbox.open(Folder.READ_ONLY)

                val messages = inbox.messages
                val fetchedEmails = messages.map { message ->
                    val sender = extractEmailAddress(message.from[0].toString())
                    val title = message.subject
                    val contentPreview = extractContentPreview(message.content)
                    Email(sender, title, contentPreview)
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

    private fun extractContentPreview(content: Any): String {
        // Truncate content to display only the first 100 characters
        return when (content) {
            is String -> content.take(100)
            is javax.mail.Multipart -> extractTextFromMimeMultipart(content).take(100)
            else -> "Unsupported content type"
        }
    }

    private fun extractTextFromMimeMultipart(multipart: javax.mail.Multipart): String {
        val result = StringBuilder()
        for (i in 0 until multipart.count) {
            val bodyPart = multipart.getBodyPart(i)
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.content.toString())
            } else if (bodyPart.content is javax.mail.Multipart) {
                result.append(extractTextFromMimeMultipart(bodyPart.content as javax.mail.Multipart))
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

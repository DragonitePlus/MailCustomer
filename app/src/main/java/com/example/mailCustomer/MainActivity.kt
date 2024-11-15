package com.example.mailCustomer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mailcostomer.R
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        loginButton.setOnClickListener {
            val user = username.text.toString()
            val pass = password.text.toString()

            if (user.isNotEmpty() && pass.isNotEmpty()) {
                sendLoginRequest(user, pass)
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun sendLoginRequest(user: String, pass: String) {
        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("username", user)
            .add("password", pass)
            .build()

        val request = Request.Builder()
            .url("http://localhost:8080/login")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}

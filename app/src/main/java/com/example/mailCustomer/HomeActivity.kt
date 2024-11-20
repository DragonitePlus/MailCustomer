     package com.example.mailCustomer

     import android.content.Intent
     import android.os.Bundle
     import androidx.appcompat.app.AppCompatActivity
     import com.example.mailcostomer.R
     import com.github.florent37.shapeofview.shapes.RoundRectView

     class HomeActivity : AppCompatActivity() {
         override fun onCreate(savedInstanceState: Bundle?) {
             super.onCreate(savedInstanceState)
             setContentView(R.layout.activity_home)

             val inboxBlock = findViewById<RoundRectView>(R.id.inboxBlock)
             val sendMailBlock = findViewById<RoundRectView>(R.id.sendMailBlock)

             inboxBlock.setOnClickListener {
                 val intent = Intent(this, InboxActivity::class.java)
                 startActivity(intent)
             }

             sendMailBlock.setOnClickListener {
                 val intent = Intent(this, SendMailActivity::class.java)
                 startActivity(intent)
             }
         }
     }
     
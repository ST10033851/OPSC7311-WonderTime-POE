package com.example.opsc7311_wondertime_part2.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.opsc7311_wondertime_part2.R

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val register_btn: Button = findViewById(R.id.register_btn)
        val register_link: TextView = findViewById(R.id.login_link)
        register_btn.bringToFront()

        register_btn.setOnClickListener{
            startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
        }

        register_link.setOnClickListener{
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
        }
    }
}
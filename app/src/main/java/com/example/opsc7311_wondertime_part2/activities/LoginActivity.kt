package com.example.opsc7311_wondertime_part2.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.opsc7311_wondertime_part2.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val login_btn: Button = findViewById(R.id.Login_btn)
        val register_link: TextView = findViewById(R.id.register_link)
        login_btn.bringToFront()

        login_btn.setOnClickListener{
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
        }

        register_link.setOnClickListener{
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }


    }
}
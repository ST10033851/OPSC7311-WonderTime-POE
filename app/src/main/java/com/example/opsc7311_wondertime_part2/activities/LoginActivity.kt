package com.example.opsc7311_wondertime_part2.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.databinding.ActivityLoginBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

//Android Knowledge. (2022, October 27).
// Login and Signup using Firebase Authentication in Android Studio [Video].
// YouTube. https://www.youtube.com/watch?v=TStttJRAPhE
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.LoginBtn.setOnClickListener{
            val email =  binding.emailInput.text.toString()
            val password =  binding.passwordInput.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty())
            {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if(task.isSuccessful)
                        {
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else
                        {
                            Toast.makeText(this, "Login Unsuccessful", Toast.LENGTH_SHORT).show()

                        }
                    }

            }
            else
            {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()

            }
        }

        binding.registerLink.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }



    }
}
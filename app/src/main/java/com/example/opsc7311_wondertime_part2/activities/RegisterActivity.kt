package com.example.opsc7311_wondertime_part2.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

//Android Knowledge. (2022, October 27).
// Login and Signup using Firebase Authentication in Android Studio [Video].
// YouTube. https://www.youtube.com/watch?v=TStttJRAPhE
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth =  FirebaseAuth.getInstance()

        binding.registerBtn.setOnClickListener{
            val email =  binding.emailAdrdessInput.text.toString()
            val password =  binding.passwordInput.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty())
            {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if(task.isSuccessful)
                        {
                            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else
                        {
                            Toast.makeText(this, "Registration Unsuccessful", Toast.LENGTH_SHORT).show()

                        }
                    }

            }
            else
            {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()

            }
        }

        binding.loginLink.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
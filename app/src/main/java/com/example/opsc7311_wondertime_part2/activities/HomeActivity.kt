package com.example.opsc7311_wondertime_part2.activities

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.models.homeModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private val homeModelList = mutableListOf<homeModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> true

                R.id.categories -> {
                    handleCategoriesNavigation()
                    true
                }
                R.id.graph -> {
                    handleOtherNavigation()
                    true
                }

                R.id.profile -> {
                    handleOtherNavigation()
                    true
                }

                else -> false
            }
        }

        val logoImageView = findViewById<ImageView>(R.id.logo)
        logoImageView.setOnClickListener{
            showLogoutConfirmationDialog()
        }

        val themeSelectorImageView = findViewById<ImageView>(R.id.themeSelecter)
        themeSelectorImageView.setOnClickListener{
            handleOtherNavigation()
        }

        val achievementsSelectorImageView = findViewById<ImageView>(R.id.achievements)
        achievementsSelectorImageView.setOnClickListener{
            handleOtherNavigation()
        }
    }

    private fun handleCategoriesNavigation(){
        startActivity(Intent(this, CategoriesActivity::class.java))
    }
    private fun handleOtherNavigation(){
        Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showLogoutConfirmationDialog()
    {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Logout")
        alertDialog.setMessage("Are you sure you want to logout?")
        alertDialog.setPositiveButton("Yes")
        {
            dialog, _ ->
            startActivity(Intent(this, LoginActivity::class.java))
            dialog.dismiss()
        }
        alertDialog.setNegativeButton("No")
        {
            dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.show()
    }
}
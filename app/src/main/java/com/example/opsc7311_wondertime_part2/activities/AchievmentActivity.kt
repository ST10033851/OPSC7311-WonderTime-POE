package com.example.opsc7311_wondertime_part2.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.GridView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.adapters.AchievementAdapter
import com.example.opsc7311_wondertime_part2.models.achievement_model
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AchievmentActivity : AppCompatActivity() {

    private lateinit var achievementsRecyclerView: RecyclerView
    private lateinit var achievementAdapter: AchievementAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievment)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.categories
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    handleHomeNavigation()
                    true
                }
                R.id.categories -> {
                    handleCategoriesNavigation()
                    true
                }

                R.id.graph -> {
                    handleGraphNavigation()
                    true
                }

                R.id.profile -> {
                    handleProfileNavigation()
                    true
                }

                else -> false
            }
        }

        fetchAchievementsFromFirebase()
    }

    private fun handleCategoriesNavigation() {
        startActivity(Intent(this, CategoriesActivity::class.java))
    }

    private fun handleHomeNavigation(){
        startActivity(Intent(this, HomeActivity::class.java))
    }
    private fun handleProfileNavigation() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    private fun handleGraphNavigation() {
        startActivity(Intent(this, StatisticsActivity::class.java))
    }

    private fun fetchAchievementsFromFirebase() {
        val user = FirebaseAuth.getInstance().currentUser!!
        val userId = user.uid
        val achievementsRef = FirebaseDatabase.getInstance().getReference("achievements").child(userId)

        achievementsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val achievementsList = mutableListOf<achievement_model>()

                for (achievementSnapshot in dataSnapshot.children) {
                    val title = achievementSnapshot.child("title").getValue(String::class.java) ?: ""
                    val imageUrl = achievementSnapshot.child("imageUri").getValue(String::class.java) ?: ""
                    val completed = achievementSnapshot.child("completed").getValue(Boolean::class.java) ?: false

                    val achievement = achievement_model(title, imageUrl, completed)
                    achievementsList.add(achievement)
                }

                populateGridView(achievementsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    fun populateGridView(achievements: List<achievement_model>) {
        val user = FirebaseAuth.getInstance().currentUser!!
        val userId = user.uid
        val achievementsGridView = findViewById<GridView>(R.id.gridView)
        val adapter = AchievementAdapter(this, achievements, userId)
        achievementsGridView.adapter = adapter
    }



}
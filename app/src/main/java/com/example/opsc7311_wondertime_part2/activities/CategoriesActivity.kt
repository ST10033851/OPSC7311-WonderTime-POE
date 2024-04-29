package com.example.opsc7311_wondertime_part2.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.adapters.categoryAdapter
import com.example.opsc7311_wondertime_part2.databinding.ActivityCategoriesBinding
import com.example.opsc7311_wondertime_part2.models.CategoriesRepository
import com.example.opsc7311_wondertime_part2.models.categoriesModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class CategoriesActivity : AppCompatActivity() {

    val categoriesList = CategoriesRepository.getCategoryList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: categoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityCategoriesBinding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val window: Window = this@CategoriesActivity.window
        window.statusBarColor = ContextCompat.getColor(this@CategoriesActivity, R.color.white)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.categories
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    handleHomeNavigation()
                    true
                }
                R.id.categories -> {
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

        recyclerView = findViewById(R.id.c_recycler)
        categoryAdapter = categoryAdapter(this, categoriesList)
        recyclerView.adapter = categoryAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        binding.plusCat.setOnClickListener { showBottomDialog() }

    }

    private fun showBottomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.fragment_new_category_)

        // Initialize views after inflating the dialog layout
        val name = dialog.findViewById<EditText>(R.id.categoryNameInput)
        val saveBtn = dialog.findViewById<TextView>(R.id.saveCategory)

        saveBtn.setOnClickListener{
            val categoryName = name.text.toString()
            if (categoryName.isNotEmpty()) {
                val newCategory = categoriesModel(categoryName, 0)
                CategoriesRepository.addCategory(newCategory)
                categoryAdapter.notifyDataSetChanged()
                dialog.dismiss()
            } else {
                Toast.makeText(this@CategoriesActivity, "Please enter a category name.", Toast.LENGTH_SHORT).show()
            }
        }

        val cancelButton = dialog.findViewById<ImageView>(R.id.cancelButton)
        cancelButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    private fun handleHomeNavigation(){
        startActivity(Intent(this, HomeActivity::class.java))
    }
    private fun handleOtherNavigation(){
        Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show()
    }
}
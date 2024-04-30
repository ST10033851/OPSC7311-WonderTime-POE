package com.example.opsc7311_wondertime_part2.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.adapters.categoryAdapter
import com.example.opsc7311_wondertime_part2.databinding.ActivityCategoriesBinding
import com.example.opsc7311_wondertime_part2.models.CategoriesRepository
import com.example.opsc7311_wondertime_part2.models.categoriesModel
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CategoriesActivity : AppCompatActivity() {

    val categoriesList = CategoriesRepository.getCategoryList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: categoryAdapter
    private lateinit var drawable : ColorDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityCategoriesBinding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.CategoryRangePicker.setOnClickListener{ showRangePicker() }

        binding.plusCat.setOnClickListener { showBottomDialog() }

    }

    @SuppressLint("SetTextI18n")
    private fun showRangePicker() {
        val rangeInput = findViewById<EditText>(R.id.categoryRangeInput)

        val materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setSelection(androidx.core.util.Pair(
                MaterialDatePicker.thisMonthInUtcMilliseconds(),
                MaterialDatePicker.todayInUtcMilliseconds()
            ))
            .build()

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            val date1 = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                .format(Date(selection.first ?: 0))
            val date2 = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                .format(Date(selection.second ?: 0))

            rangeInput.setText("$date1 to $date2")

        }

        materialDatePicker.show(supportFragmentManager, "tag")
    }


    private fun showBottomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.fragment_new_category_)

        val errorDialog = Dialog(this)
        errorDialog.setContentView(R.layout.error_dialog)
        errorDialog.setCancelable(false)

        val errorMessageTextView = errorDialog.findViewById<TextView>(R.id.ErrorDescription)
        errorMessageTextView.text = "Please enter all details"

        val dismissButton = errorDialog.findViewById<Button>(R.id.ErrorDone)
        dismissButton.setOnClickListener {
            errorDialog.dismiss()
        }

        // Initialize views after inflating the dialog layout
        val name = dialog.findViewById<EditText>(R.id.categoryNameInput)
        val saveBtn = dialog.findViewById<TextView>(R.id.saveCategory)
        val colorInput = dialog.findViewById<ImageView>(R.id.uploadColor)

        colorInput.setOnClickListener{
            MaterialColorPickerDialog
                .Builder(this)
                .setTitle("Pick Color")
                .setColorShape(ColorShape.SQAURE)
                .setColorSwatch(ColorSwatch._300)
                .setDefaultColor("#FFFFFF")
                .setColorListener { color, colorHex ->
                    drawable = ColorDrawable(color)
                    colorInput.background = drawable
                }
                .show()

        }

        saveBtn.setOnClickListener{
            val categoryName = name.text.toString()
            val color = drawable

            if (categoryName.isNotEmpty()) {
                val newCategory = categoriesModel(categoryName, 0, color)
                CategoriesRepository.addCategory(newCategory)
                categoryAdapter.notifyDataSetChanged()
                dialog.dismiss()
            } else {
                errorDialog.show()
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
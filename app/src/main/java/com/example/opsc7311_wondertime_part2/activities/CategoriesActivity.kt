package com.example.opsc7311_wondertime_part2.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.adapters.categoryAdapter
import com.example.opsc7311_wondertime_part2.databinding.ActivityCategoriesBinding
import com.example.opsc7311_wondertime_part2.models.CategoriesRepository
import com.example.opsc7311_wondertime_part2.models.CategoriesRepository.calcTotalHours
import com.example.opsc7311_wondertime_part2.models.TimesheetRepository
import com.example.opsc7311_wondertime_part2.models.categoriesModel
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CategoriesActivity : AppCompatActivity() {

    val categoriesList = CategoriesRepository.getCategoryList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: categoryAdapter
    private lateinit var drawable : String
    private lateinit var rangeInput: EditText
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityCategoriesBinding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val user = FirebaseAuth.getInstance().currentUser!!
        val userId = user.uid

        binding.shimmerViewContainer.startShimmer()

        database = Firebase.database.reference.child("Categories").child(userId)
        drawable = "#6E3FF1"
        rangeInput  = findViewById(R.id.categoryRangeInput)
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
        Handler(Looper.getMainLooper()).postDelayed({
            recyclerView = findViewById(R.id.c_recycler)
            categoryAdapter = categoryAdapter(this, categoriesList)
            recyclerView.adapter = categoryAdapter
            recyclerView.layoutManager = LinearLayoutManager(this)

            binding.CategoryRangePicker.setOnClickListener{ showRangePicker() }

            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    categoriesList.clear()
                    if (dataSnapshot.exists()) {
                        for (studentsnapshot in dataSnapshot.children) {
                            val studentModel = studentsnapshot.getValue(categoriesModel::class.java)
                            categoriesList.add(studentModel!!)
                        }
                        categoryAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@CategoriesActivity, databaseError.toString(), Toast.LENGTH_SHORT).show()
                }
            })
            categoryAdapter.notifyDataSetChanged()

            binding.shimmerViewContainer.stopShimmer()
            binding.shimmerViewContainer.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }, 5000)

        binding.plusCat.setOnClickListener { showBottomDialog() }
        binding.DescendingFilter.setOnClickListener{OrderByDescending()}
        binding.AscendingFilter.setOnClickListener{OrderByAscending()}
        filterByColor()
    }

    @SuppressLint("SetTextI18n")
    private fun showRangePicker() {

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

            filterToDateRange(date1, date2)

        }

        materialDatePicker.show(supportFragmentManager, "tag")
    }

    private fun filterToDateRange(date1: String, date2: String){
        val errorDialog = Dialog(this)
        errorDialog.setContentView(R.layout.error_dialog)
        errorDialog.setCancelable(false)
        val errorMessageTextView = errorDialog.findViewById<TextView>(R.id.ErrorDescription)
        errorMessageTextView.text = getString(R.string.there_are_no_categories_entered)

        val dismissButton = errorDialog.findViewById<Button>(R.id.ErrorDone)
        dismissButton.setOnClickListener {
            errorDialog.dismiss()
        }

        if(categoriesList.isEmpty()){
            errorDialog.show()
        }
        else{
            rangeInput.setText(getString(R.string.to, date1, date2))
            val timesheetsList = TimesheetRepository.getTimesheetsList()
            Log.d("",timesheetsList.toString())
            val timesheetsFiltered = timesheetsList.filter {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val itemDate = dateFormat.parse(it.date)
                val startDate = dateFormat.parse(date1)
                val endDate = dateFormat.parse(date2)

                itemDate != null && !itemDate.before(startDate) && !itemDate.after(endDate)
            }


            for (category in categoriesList) {
                calcTotalHours(category.name, timesheetsFiltered)
            }

            categoryAdapter.notifyDataSetChanged()
            categoryAdapter = categoryAdapter(this, categoriesList)
            recyclerView.adapter = categoryAdapter
            recyclerView.layoutManager = LinearLayoutManager(this)
        }

    }

    fun filterByColor() {
        val colorInput = findViewById<Button>(R.id.ColorFilter)

        colorInput.setOnClickListener {
            MaterialColorPickerDialog
                .Builder(this)
                .setTitle("Pick Color")
                .setColorShape(ColorShape.SQAURE)
                .setColorSwatch(ColorSwatch._300)
                .setDefaultColor("#FFFFFF")
                .setColorListener { color, colorHex ->
                    Log.d("",color.toString() + colorHex)
                    filterCategoriesByColor(colorHex)
                }
                .show()
        }
    }

    private fun filterCategoriesByColor(selectedColorHex: String) {
        val errorDialog = Dialog(this)
        errorDialog.setContentView(R.layout.error_dialog)
        errorDialog.setCancelable(false)
        val errorMessageTextView = errorDialog.findViewById<TextView>(R.id.ErrorDescription)
        errorMessageTextView.text = getString(R.string.there_are_no_categories_entered)

        val dismissButton = errorDialog.findViewById<Button>(R.id.ErrorDone)
        dismissButton.setOnClickListener {
            errorDialog.dismiss()
        }

        if (categoriesList.isEmpty()) {
            errorDialog.show()
        } else {
            val filteredCategories = ArrayList(categoriesList.filter {
                it.color.equals(selectedColorHex, ignoreCase = true)
            })

            if (filteredCategories.isEmpty()) {
                errorMessageTextView.text =
                    getString(R.string.you_do_not_have_any_categories_with_that_color)
                errorDialog.show()
            } else {

                categoryAdapter = categoryAdapter(this, filteredCategories)
                recyclerView.adapter = categoryAdapter
                recyclerView.layoutManager = LinearLayoutManager(this)
                categoryAdapter.notifyDataSetChanged()
            }
        }
    }
    private fun OrderByDescending() {
        val errorDialog = Dialog(this)
        errorDialog.setContentView(R.layout.error_dialog)
        errorDialog.setCancelable(false)
        val errorMessageTextView = errorDialog.findViewById<TextView>(R.id.ErrorDescription)
        errorMessageTextView.text = getString(R.string.there_are_no_categories_entered)

        val dismissButton = errorDialog.findViewById<Button>(R.id.ErrorDone)
        dismissButton.setOnClickListener {
            errorDialog.dismiss()
        }

        if (categoriesList.isEmpty()) {
            errorDialog.show()
        } else {
            val orderedCategories = ArrayList(categoriesList.sortedByDescending {
                it.name
            })

            if (orderedCategories.isEmpty()) {
                errorMessageTextView.text =
                    getString(R.string.you_do_not_have_any_categories_with_that_color)
                errorDialog.show()
            } else {

                categoryAdapter = categoryAdapter(this, orderedCategories)
                recyclerView.adapter = categoryAdapter
                recyclerView.layoutManager = LinearLayoutManager(this)
                categoryAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun OrderByAscending() {
        val errorDialog = Dialog(this)
        errorDialog.setContentView(R.layout.error_dialog)
        errorDialog.setCancelable(false)
        val errorMessageTextView = errorDialog.findViewById<TextView>(R.id.ErrorDescription)
        errorMessageTextView.text = getString(R.string.there_are_no_categories_entered)

        val dismissButton = errorDialog.findViewById<Button>(R.id.ErrorDone)
        dismissButton.setOnClickListener {
            errorDialog.dismiss()
        }

        if (categoriesList.isEmpty()) {
            errorDialog.show()
        } else {
            val orderedCategories = ArrayList(categoriesList.sortedBy {
                it.name
            })

            if (orderedCategories.isEmpty()) {
                errorMessageTextView.text =
                    getString(R.string.you_do_not_have_any_categories_with_that_color)
                errorDialog.show()
            } else {

                categoryAdapter = categoryAdapter(this, orderedCategories)
                recyclerView.adapter = categoryAdapter
                recyclerView.layoutManager = LinearLayoutManager(this)
                categoryAdapter.notifyDataSetChanged()
            }
        }
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
                    drawable = colorHex
                    colorInput.background = ColorDrawable(Color.parseColor(drawable))
                }
                .show()

        }

        saveBtn.setOnClickListener{
            val categoryName = name.text.toString()
            val color = drawable

            if (categoryName.isNotEmpty()) {
                val user = FirebaseAuth.getInstance().currentUser!!
                val userId = user.uid
                val newCategory = categoriesModel(categoryName, 0, color)
                CategoriesRepository.addCategory(newCategory)

                val database = FirebaseDatabase.getInstance().getReference("Categories").child(userId)

                val newCategoryRef = database.child(newCategory.name)
                newCategoryRef.setValue(newCategory)

                database.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        categoriesList.clear()
                        if (dataSnapshot.exists()) {
                            for (studentsnapshot in dataSnapshot.children) {
                                val studentModel = studentsnapshot.getValue(categoriesModel::class.java)
                                categoriesList.add(studentModel!!)
                            }
                            categoryAdapter.notifyDataSetChanged()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@CategoriesActivity, databaseError.toString(), Toast.LENGTH_SHORT).show()
                    }
                })
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
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
    private fun handleProfileNavigation() {
        startActivity(Intent(this, ProfileActivity::class.java))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun handleGraphNavigation() {
        startActivity(Intent(this, StatisticsActivity::class.java))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
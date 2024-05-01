package com.example.opsc7311_wondertime_part2.activities

import android.app.Dialog
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.fragments.new_timesheet_Fragment
import com.example.opsc7311_wondertime_part2.models.CategoriesRepository
import com.example.opsc7311_wondertime_part2.models.HomeRepository
import com.example.opsc7311_wondertime_part2.models.TimesheetRepository
import com.example.opsc7311_wondertime_part2.models.homeModel
import com.example.opsc7311_wondertime_part2.models.timesheetsModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import nl.joery.timerangepicker.TimeRangePicker

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

        saveDailyGoal()
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
    private fun saveDailyGoal()
    {
        val TimepickerBtn = findViewById<TimeRangePicker>(R.id.picker)
        val saveDailyGoalBtn = findViewById<Button>(R.id.saveDailyGoal)
        val minGoal = findViewById<EditText>(R.id.MinimumGoalInput)
        val maxGoal = findViewById<EditText>(R.id.MaximumGoalInput)
        val minTimeDisplay = findViewById<TextView>(R.id.MinTimeDisplay)
        val maxTimeDisplay = findViewById<TextView>(R.id.MaxTimeDisplay)
        var goalDuration: Int = 0

        TimepickerBtn.setOnTimeChangeListener(object : TimeRangePicker.OnTimeChangeListener {
            var minGoalTime = 0
            var maxGoalTime = 0

            override fun onStartTimeChange(endTime: TimeRangePicker.Time) {
                val timeString = "${endTime.hour}:${endTime.minute}"
                maxGoal.setText(timeString)
                maxGoalTime = endTime.hour
            }

            override fun onEndTimeChange(startTime: TimeRangePicker.Time) {
                val timeString = "${startTime.hour}:${startTime.minute}"
                minGoal.setText(timeString)
                minGoalTime = startTime.hour
            }

            override fun onDurationChange(duration: TimeRangePicker.TimeDuration) {
                goalDuration = maxGoalTime - minGoalTime
            }
        })
        saveDailyGoalBtn.setOnClickListener()
        {
            val min = minGoal.text.toString()
            val max = maxGoal.text.toString()
            val newDailyGoal = homeModel(min, max)

            if(min.isEmpty() || max.isEmpty())
            {
                val errorDialog = Dialog(this)
                errorDialog.setContentView(R.layout.error_dialog)
                errorDialog.setCancelable(false)
                val errorMessageTextView = errorDialog.findViewById<TextView>(R.id.ErrorDescription)
                errorMessageTextView.text = "Please enter all fields"
                val dismissButton = errorDialog.findViewById<Button>(R.id.ErrorDone)
                dismissButton.setOnClickListener {
                    errorDialog.dismiss()
                }
                errorDialog.show()
            }
            else{
                HomeRepository.addDailyGoal(newDailyGoal)
                Toast.makeText(this, "Daily Goal Saved!", Toast.LENGTH_SHORT).show()
                minTimeDisplay.setText(min)
                maxTimeDisplay.setText(max)
                minGoal.setText("")
                maxGoal.setText("")
            }
        }
    }
}
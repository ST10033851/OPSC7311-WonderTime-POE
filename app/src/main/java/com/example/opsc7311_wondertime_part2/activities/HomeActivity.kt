package com.example.opsc7311_wondertime_part2.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.models.HomeRepository
import com.example.opsc7311_wondertime_part2.models.TimesheetRepository
import com.example.opsc7311_wondertime_part2.models.homeModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import nl.joery.timerangepicker.TimeRangePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private val homeModelList = HomeRepository.getDailyGoalList()
    private val timesheetsList = TimesheetRepository.getTimesheetsList()
    var minGoalTime = 0
    var maxGoalTime = 0
    private lateinit var minGoal: TextView
    private lateinit var maxGoal: TextView
    private lateinit var mincheckImage: ImageView
    private lateinit var maxcheckImage: ImageView
    private lateinit var TimepickerBtn: TimeRangePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        val saveDailyGoalBtn = findViewById<Button>(R.id.saveDailyGoal)
        val minTimeDisplay: TextView = findViewById(R.id.MinTimeDisplay)
        val maxTimeDisplay: TextView = findViewById(R.id.MaxTimeDisplay)
        TimepickerBtn = findViewById(R.id.picker)
        minGoal= findViewById(R.id.MinimumGoalInput)
        maxGoal= findViewById(R.id.MaximumGoalInput)

        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        var goalDuration: Int = 0

        val totalDurationToday = timesheetsList.filter {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val itemDate = dateFormat.parse(it.date)
            itemDate?.time == currentDate
        }.sumOf { it.duration }

        val dailyGoals = HomeRepository.getDailyGoalList()
        if (dailyGoals.isNotEmpty()) {
            val minimumGoal = dailyGoals.first().minimumGoal
            val maximumGoal = dailyGoals.first().maximumGoal

            minTimeDisplay.text = minimumGoal.toString() + "Hours"
            maxTimeDisplay.text = maximumGoal.toString() + "Hours"

            if (totalDurationToday >= minimumGoal) {
                mincheckImage.visibility = View.VISIBLE
            }

            if (totalDurationToday >= maximumGoal) {
                maxcheckImage.visibility = View.VISIBLE
            }
        }

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

        TimepickerBtn.setOnTimeChangeListener(object : TimeRangePicker.OnTimeChangeListener {

            override fun onStartTimeChange(endTime: TimeRangePicker.Time) {
                val timeString = "${endTime.hour}h:${endTime.minute}m"
                maxGoal.text = timeString
                maxGoalTime = endTime.hour
            }

            override fun onEndTimeChange(startTime: TimeRangePicker.Time) {
                val timeString = "${startTime.hour}h:${startTime.minute}m"
                minGoal.text = timeString
                minGoalTime = startTime.hour
            }

            override fun onDurationChange(duration: TimeRangePicker.TimeDuration) {
                goalDuration = maxGoalTime - minGoalTime
            }
        })

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

        saveDailyGoalBtn.setOnClickListener()
        {
            val min = minGoal.text.toString()
            val max = maxGoal.text.toString()

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
                val sharedPreferences = getSharedPreferences("DailyGoalPrefs", Context.MODE_PRIVATE)
                val lastSavedDate = sharedPreferences.getLong("lastSavedDate", 0)

                if (currentDate > lastSavedDate) {
                    showGoalConfirmationDialog()
                    Toast.makeText(this, "Daily Goal Saved!", Toast.LENGTH_SHORT).show()

                    sharedPreferences.edit().putLong("lastSavedDate", currentDate).apply()
                } else {
                    Toast.makeText(this@HomeActivity, "Daily Goal already saved for today!", Toast.LENGTH_SHORT).show()
                }


            }
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

    private fun showGoalConfirmationDialog()
    {
        val minGoal= findViewById<TextView>(R.id.MinimumGoalInput)
        val maxGoal= findViewById<TextView>(R.id.MaximumGoalInput)
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Save goals")
        alertDialog.setMessage("Note: You can set your minimum and maximum goals for today. " +
                "\nAfter saving, you won't be able to enter another goal until tomorrow.")
        alertDialog.setPositiveButton("Yes")
        {
            dialog, _ ->
                saveDailyGoal()
                minGoal.text = ""
                maxGoal.text = ""
                TimepickerBtn.isEnabled = false
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
        val minTimeDisplay: TextView = findViewById(R.id.MinTimeDisplay)
        val maxTimeDisplay: TextView = findViewById(R.id.MaxTimeDisplay)
        val min = minGoalTime
        val max = maxGoalTime

        val newDailyGoal = homeModel(min, max)
        HomeRepository.addDailyGoal(newDailyGoal)
        minTimeDisplay.text = minGoal.text
        maxTimeDisplay.text = maxGoal.text


    }
}
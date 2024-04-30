package com.example.opsc7311_wondertime_part2.activities

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.adapters.TimesheetAdapter
import com.example.opsc7311_wondertime_part2.models.CategoriesRepository
import com.example.opsc7311_wondertime_part2.models.TimesheetRepository
import com.example.opsc7311_wondertime_part2.models.timesheetsModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import nl.joery.timerangepicker.TimeRangePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimesheetsActivity : AppCompatActivity() {

    private val timesheetsList = TimesheetRepository.getTimesheetsList()
    private lateinit var timesheetAdapter: TimesheetAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var plusTimeSheetButton: FloatingActionButton
    private lateinit var imageView: ImageView
    private lateinit var imageInput : Uri
    private lateinit var category_name: String
    private lateinit var rangeInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timesheets)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        val timesheetRangePicker = findViewById<ImageView>(R.id.TimesheetRangePicker)
        rangeInput  = findViewById<EditText>(R.id.timesheetRangeInput)
        plusTimeSheetButton = findViewById(R.id.plusTimeSheet)
        category_name = intent.getStringExtra("categoryName").toString()

        val timesheetsFiltered = timesheetsList.filter { it.category.equals(category_name, ignoreCase = true) }

        val resId = R.drawable.test_image
        imageInput = Uri.parse("android.resource://${packageName}/$resId")

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

        recyclerView = findViewById(R.id.t_recycler)
        timesheetAdapter = TimesheetAdapter(this, timesheetsFiltered )
        recyclerView.adapter = timesheetAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        timesheetRangePicker.setOnClickListener{ showRangePicker() }

        plusTimeSheetButton.setOnClickListener { showBottomDialog() }


    }
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
        errorMessageTextView.text = getString(R.string.there_are_no_timesheets_entered)

        val dismissButton = errorDialog.findViewById<Button>(R.id.ErrorDone)
        dismissButton.setOnClickListener {
            errorDialog.dismiss()
        }

        if(timesheetsList.isEmpty()){
            errorDialog.show()
        }
        else{
            rangeInput.setText(getString(R.string.to, date1, date2))
            val timesheetsList = TimesheetRepository.getTimesheetsList()
            val timesheetsFiltered = timesheetsList.filter {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val itemDate = dateFormat.parse(it.date)
                val startDate = dateFormat.parse(date1)
                val endDate = dateFormat.parse(date2)

                itemDate != null && itemDate.after(startDate) && itemDate.before(endDate)
                        && it.category.equals(category_name, ignoreCase = true)
            }

            timesheetAdapter.notifyDataSetChanged()
            recyclerView = findViewById(R.id.t_recycler)
            timesheetAdapter = TimesheetAdapter(this, timesheetsFiltered )
            recyclerView.adapter = timesheetAdapter
            recyclerView.layoutManager = LinearLayoutManager(this)
        }

    }


    private fun showBottomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.fragment_new_timesheet_)

        val dateInput = dialog.findViewById<EditText>(R.id.DateInput)
        val descriptionInput = dialog.findViewById<EditText>(R.id.DescriptionInput)
        val startTimeInput = dialog.findViewById<EditText>(R.id.StartTimeInput)
        val endTimeInput = dialog.findViewById<EditText>(R.id.EndTimeInput)
        val CategoryInput = category_name

        val saveBtn = dialog.findViewById<TextView>(R.id.saveTimesheet)
        val uploadImageBtn = dialog.findViewById<ImageView>(R.id.UploadImageBtn)
        val DatePickerbtn = dialog.findViewById<ImageView>(R.id.datePickerBtn)
        val TimepickerBtn = dialog.findViewById<TimeRangePicker>(R.id.picker)
        val cancelButton = dialog.findViewById<ImageView>(R.id.cancelButton)

        var Timesheetduration: Int = 0
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.UK)
        val myCalender = Calendar.getInstance()



        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalender.set(Calendar.YEAR, year)
            myCalender.set(Calendar.MONTH,month)
            myCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            if (myCalender != null) {
                dateInput.setText(sdf.format(myCalender.time))
            }
        }

        TimepickerBtn.setOnTimeChangeListener(object : TimeRangePicker.OnTimeChangeListener {
            var StartTime = 0
            var EndTime = 0

            override fun onStartTimeChange(endTime: TimeRangePicker.Time) {
                val timeString = "${endTime.hour}:${endTime.minute}"
                endTimeInput.setText(timeString)
                EndTime = endTime.hour
            }

            override fun onEndTimeChange(startTime: TimeRangePicker.Time) {
                val timeString = "${startTime.hour}:${startTime.minute}"
                startTimeInput.setText(timeString)
                StartTime = startTime.hour
            }

            override fun onDurationChange(duration: TimeRangePicker.TimeDuration) {
                Timesheetduration = EndTime - StartTime
            }
        })


        DatePickerbtn.setOnClickListener{
            DatePickerDialog(this,datePicker,myCalender.get(Calendar.YEAR), myCalender.get(Calendar.MONTH),
                myCalender.get(Calendar.DAY_OF_MONTH)).show()
        }

        uploadImageBtn.setOnClickListener {
            imageView = dialog.findViewById(R.id.UploadImage)
            imagePickerLauncher.launch("image/*")

        }

        saveBtn.setOnClickListener{
            val date = dateInput.text.toString()
            val description = descriptionInput.text.toString()
            val startTime = startTimeInput.text.toString()
            val endTime = endTimeInput.text.toString()
            val category = CategoryInput
            val imageResource = imageInput
            val newTimesheet = timesheetsModel(date, startTime, endTime, description, category, imageResource, Timesheetduration)

            if(date.isEmpty() || description.isEmpty() || startTime.isEmpty() || endTime.isEmpty()){
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
                TimesheetRepository.addTimesheet(newTimesheet)
                timesheetAdapter.notifyDataSetChanged()

                CategoriesRepository.calcTotalHours(category_name, timesheetsList)

                updateAdapter()
                dialog.dismiss()
            }

        }


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

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->

        if (uri != null) {
            imageView.setImageURI(uri)
            imageInput = uri
        } else {
            val resId = R.drawable.test_image
            imageInput = Uri.parse("android.resource://${packageName}/$resId")

        }
    }

    private fun updateAdapter(){
        val timesheetsList = TimesheetRepository.getTimesheetsList()
        val timesheetsFiltered = timesheetsList.filter { it.category.equals(category_name, ignoreCase = true) }

        recyclerView = findViewById(R.id.t_recycler)
        timesheetAdapter = TimesheetAdapter(this, timesheetsFiltered )
        recyclerView.adapter = timesheetAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    private fun handleHomeNavigation(){
        startActivity(Intent(this, HomeActivity::class.java))
    }
    private fun handleCategoriesNavigation(){
        startActivity(Intent(this, CategoriesActivity::class.java))
    }
    private fun handleOtherNavigation(){
        Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show()
    }
}
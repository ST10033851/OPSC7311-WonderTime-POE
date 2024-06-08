package com.example.opsc7311_wondertime_part2.activities

import android.Manifest
import android.app.AlertDialog
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
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
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
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.adapters.TimesheetAdapter
import com.example.opsc7311_wondertime_part2.databinding.ActivityTimesheetsBinding
import com.example.opsc7311_wondertime_part2.models.TimesheetRepository
import com.example.opsc7311_wondertime_part2.models.timesheetsModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import nl.joery.timerangepicker.TimeRangePicker
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

//Droppers. (2021). TimeRangePicker [GitHub Repository]. Retrieved from https://github.com/Droppers/TimeRangePicker
class TimesheetsActivity : AppCompatActivity() {

    private val timesheetsList = TimesheetRepository.getTimesheetsList()

    private lateinit var timesheetAdapter: TimesheetAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var plusTimeSheetButton: FloatingActionButton
    private lateinit var imageView: ImageView
    private lateinit var imageInput: Uri
    private lateinit var photoUri: Uri
    private lateinit var categoryName: String
    private lateinit var rangeInput: EditText
    private lateinit var database: DatabaseReference
    private lateinit var storageRef: StorageReference

    private val CAMERA_PERMISSION_REQUEST_CODE = 1001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityTimesheetsBinding = ActivityTimesheetsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Firebase.database.reference.child("Timesheets")
        storageRef = FirebaseStorage.getInstance().getReference("Images")

        requestCameraPermission()

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNav.selectedItemId = 0

        val timesheetRangePicker: ImageView = findViewById(R.id.TimesheetRangePicker)
        rangeInput = findViewById(R.id.timesheetRangeInput)
        plusTimeSheetButton = findViewById(R.id.plusTimeSheet)

        categoryName = intent.getStringExtra("categoryName").toString()
        val timesheetsFiltered = timesheetsList.filter { it.category.equals(categoryName, ignoreCase = true) }

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
        recyclerView = findViewById(R.id.t_recycler)
        timesheetAdapter = TimesheetAdapter(this, timesheetsFiltered )
        recyclerView.adapter = timesheetAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                timesheetsList.clear()
                if (dataSnapshot.exists()) {
                    for (studentsnapshot in dataSnapshot.children) {
                        val studentModel = studentsnapshot.getValue(timesheetsModel::class.java)
                        timesheetsList.add(studentModel!!)
                    }
                    val timesheetsFiltered = timesheetsList.filter { it.category.equals(categoryName, ignoreCase = true) }
                    timesheetAdapter.submitList(timesheetsFiltered)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@TimesheetsActivity, databaseError.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        timesheetAdapter.notifyDataSetChanged()

            binding.shimmerTimesheet.stopShimmer()
            binding.shimmerTimesheet.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }, 5000)

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

                itemDate != null && !itemDate.before(startDate) && !itemDate.after(endDate)
                        && it.category.equals(categoryName, ignoreCase = true)
            }

            timesheetAdapter.notifyDataSetChanged()
            recyclerView = findViewById(R.id.t_recycler)
            timesheetAdapter = TimesheetAdapter(this, timesheetsFiltered )
            recyclerView.adapter = timesheetAdapter
            recyclerView.layoutManager = LinearLayoutManager(this)
        }

    }

    fun showBottomDialog(timesheet: timesheetsModel? = null) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.fragment_new_timesheet_)

        val dateInput = dialog.findViewById<EditText>(R.id.DateInput)
        val descriptionInput = dialog.findViewById<EditText>(R.id.DescriptionInput)
        val startTimeInput = dialog.findViewById<EditText>(R.id.StartTimeInput)
        val endTimeInput = dialog.findViewById<EditText>(R.id.EndTimeInput)
        val CategoryInput = categoryName

        timesheet?.let {
            dateInput.setText(it.date)
            descriptionInput.setText(it.description)
            startTimeInput.setText(it.startTime)
            endTimeInput.setText(it.endTime)
            imageInput = Uri.parse(it.imageUri)
        }

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
            myCalender.set(Calendar.MONTH, month)
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

        DatePickerbtn.setOnClickListener {
            DatePickerDialog(
                this, datePicker, myCalender.get(Calendar.YEAR), myCalender.get(Calendar.MONTH),
                myCalender.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        uploadImageBtn.setOnClickListener {
            imageView = dialog.findViewById(R.id.UploadImage)
            val options = arrayOf("Choose from Gallery", "Take Photo")
            AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> imagePickerLauncher.launch("image/*")
                        1 -> {
                            photoUri = createImageUri() ?: return@setItems
                            cameraLauncher.launch(photoUri)
                        }
                    }
                }
                .show()
        }

        saveBtn.setOnClickListener {
            val date = dateInput.text.toString()
            val description = descriptionInput.text.toString()
            val startTime = startTimeInput.text.toString()
            val endTime = endTimeInput.text.toString()
            val category = CategoryInput
            val imageUri = imageInput
            val user = FirebaseAuth.getInstance().currentUser
            val uid = user!!.uid

            if (date.isEmpty() || description.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
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
            } else {
                uploadImageToFirebase(imageUri) { downloadUrl ->
                    val newTimesheet = timesheetsModel(uid, date, startTime, endTime, description, category, downloadUrl, Timesheetduration)
                    val newTimesheetRef = database.push()
                    newTimesheetRef.setValue(newTimesheet)

                    database.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            timesheetsList.clear()
                            if (dataSnapshot.exists()) {
                                for (studentsnapshot in dataSnapshot.children) {
                                    val studentModel = studentsnapshot.getValue(timesheetsModel::class.java)
                                    timesheetsList.add(studentModel!!)
                                }
                                timesheetAdapter.notifyDataSetChanged()
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Toast.makeText(this@TimesheetsActivity, databaseError.toString(), Toast.LENGTH_SHORT).show()
                        }
                    })

                    TimesheetRepository.updateTotalHours(categoryName, Timesheetduration)
                    TimesheetRepository.updateDailyTotalHours(Timesheetduration)
                    updateAdapter()
                    dialog.dismiss()
                }
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


    private fun uploadImageToFirebase(uri: Uri, onSuccess: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

        val uploadTask = imagesRef.putFile(uri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            imagesRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                onSuccess(downloadUrl.toString())
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
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

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageView.setImageURI(photoUri)
            imageInput = photoUri
        }
        else {
            Toast.makeText(this, "Failed to take picture", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun createImageUri(): Uri? {
        val image = File(externalCacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            image
        )
    }

    private fun updateAdapter(){
        val timesheetsList = TimesheetRepository.getTimesheetsList()
        val timesheetsFiltered = timesheetsList.filter { it.category.equals(categoryName, ignoreCase = true) }

        recyclerView = findViewById(R.id.t_recycler)
        timesheetAdapter = TimesheetAdapter(this, timesheetsFiltered )
        recyclerView.adapter = timesheetAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        timesheetAdapter.notifyDataSetChanged()
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
    private fun handleProfileNavigation() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    private fun handleGraphNavigation() {
        startActivity(Intent(this, StatisticsActivity::class.java))
    }
}
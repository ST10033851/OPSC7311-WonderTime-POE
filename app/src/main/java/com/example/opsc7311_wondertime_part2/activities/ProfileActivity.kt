package com.example.opsc7311_wondertime_part2.activities
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.opsc7311_wondertime_part2.R
import com.google.android.material.imageview.ShapeableImageView
import android.net.Uri
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.opsc7311_wondertime_part2.models.timesheetsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
class ProfileActivity : AppCompatActivity()
{
    //Variables needed for profile picture changing and profile details
    private lateinit var profileImageView: ShapeableImageView
    private lateinit var selectImageTextView: ImageView
    private lateinit var  storageRef: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private var imageUri: Uri? = null
    private lateinit var emailAddress: EditText
    private lateinit var numberOfCategories: EditText
    private lateinit var numberOfTimeSheets: EditText
    private lateinit var numberOfGoals: EditText
    private lateinit var totalHoursWorked: EditText

    companion object
    {
        private const val CHANGE_IMAGE_REQUEST = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //calling my functions
        databaseSetup()
        viewsInitialisation()
        imageSelectionSetup()
    }
    //below are all my functions that handle the logic related to the user changing their profile picture
    private fun viewsInitialisation()
    {
        profileImageView = findViewById(R.id.profileImageView)
        selectImageTextView = findViewById(R.id.selectImageView)
    }
    //
    private fun databaseSetup()
    {
        storageRef = FirebaseStorage.getInstance().reference.child("ProfileImages")
        auth = FirebaseAuth.getInstance()
        databaseRef = Firebase.database.reference.child("Users").child(auth.currentUser?.uid!!)
    }
    //
    private fun imageSelectionSetup()
    {
        selectImageTextView.setOnClickListener{
            imageSelect()
        }
    }
    //
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            profileImageView.setImageURI(it)
            uploadImageToFirebase()
        }
    }
    //
    private fun imageSelect()
    {
        selectImageLauncher.launch("image/*")
    }
    //
    private fun uploadImageToFirebase() {
        imageUri?.let {
            val userId = auth.currentUser?.uid ?: return
            val fileRef = storageRef.child("$userId.jpg")
            fileRef.putFile(it)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        saveImageUriToDatabase(uri)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        }
    }
    //
    private fun saveImageUriToDatabase(uri: Uri)
    {
        databaseRef.child("profileImageUri").setValue(uri.toString())
            .addOnSuccessListener{
                Toast.makeText(this, "Profile image updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(this, "Failed to save image URI", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getProfileImage()
    {
        databaseRef.child("profileImageUri").get()
            .addOnSuccessListener{
                val imageUri = it.value as? String
                imageUri?.let { uri ->
                    Picasso.get().load(uri).into(profileImageView)
                }
            }
            .addOnFailureListener{
                Toast.makeText(this, "Failed to load profile image", Toast.LENGTH_SHORT).show()
            }
    }

    //the following function will calculate the items (eg. number of categories) and display it to the user
    private fun getAndDisplayUserData()
    {
        //lets get the profile image first
        getProfileImage()
        //now lets get the profile data
        //email retrieval
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val email = snapshot.child("email").getValue(String::class.java) ?:""

                // Count the number of categories
                var categoriesCount = 0
                snapshot.child("categories").children.forEach { _ ->
                    categoriesCount++
                }
                // Count the number of time sheets
                var timeSheetsCount = 0
                snapshot.child("timeSheets").children.forEach { _ ->
                    timeSheetsCount++
                }
                // Count the number of goals
                var goalsCount = 0
                snapshot.child("goals").children.forEach { _ ->
                    goalsCount++
                }

                var hoursWorked = 0;
                for(timesheetSnap in snapshot.child("timesheets").children)
                {
                    val timesheet = timesheetSnap.getValue(timesheetsModel::class.java)
                    timesheet?.let{
                        hoursWorked += it.duration
                    }
                }
                //now we populate our editText fields with retrieved data
                emailAddress.setText(email)
                numberOfCategories.setText(categoriesCount.toString())
                numberOfTimeSheets.setText(timeSheetsCount.toString())
                numberOfGoals.setText(goalsCount.toString())
                totalHoursWorked.setText(hoursWorked.toString())
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Database Error (E740)", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
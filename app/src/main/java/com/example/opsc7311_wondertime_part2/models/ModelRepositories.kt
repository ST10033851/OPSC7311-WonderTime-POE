package com.example.opsc7311_wondertime_part2.models
import android.util.Log
import android.widget.Toast
import com.example.opsc7311_wondertime_part2.activities.TimesheetsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Date

object TimesheetRepository {
    private val timesheetsList = ArrayList<timesheetsModel>()

    fun getTimesheetsList(): ArrayList<timesheetsModel> {
        return timesheetsList
    }

    fun addTimesheet(timesheet: timesheetsModel) {
        timesheetsList.add(timesheet)
    }

    fun updateTotalHours(categoryName: String, duration: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        userId?.let { uid ->
            val database = FirebaseDatabase.getInstance()
            val categoriesRef = database.getReference("Categories").child(uid)

            // Get the current total hours for the category
            categoriesRef.child(categoryName).child("totalHours").addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val currentTotalHours = dataSnapshot.getValue(Int::class.java) ?: 0
                        val newTotalHours = currentTotalHours + duration

                        // Update the total hours in the database
                        val categoryUpdates = mapOf<String, Any>(
                            "totalHours" to newTotalHours
                        )

                        categoriesRef.child(categoryName).updateChildren(categoryUpdates)
                            .addOnSuccessListener {
                                // Update successful
                            }
                            .addOnFailureListener { exception ->
                                // Handle update failure
                            }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle database error
                    }
                }
            )
        }
    }

    private val database = FirebaseDatabase.getInstance().getReference("Timesheets")

    fun getAllTimesheets(callback: (List<timesheetsModel>) -> Unit) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val timesheetsList = mutableListOf<timesheetsModel>()
                for (snapshot in dataSnapshot.children) {
                    val timesheet = snapshot.getValue(timesheetsModel::class.java)
                    timesheet?.let { timesheetsList.add(it) }
                }
                callback(timesheetsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                callback(emptyList()) // Or handle the error as needed
            }
        })
    }


}

object CategoriesRepository {
    private val categoriesList = ArrayList<categoriesModel>()

    fun getCategoryList(): ArrayList<categoriesModel> {
        return categoriesList
    }

    fun addCategory(category: categoriesModel) {
        categoriesList.add(category)
    }

    fun calcTotalHours(categoryName: String, timesheetsList: List<timesheetsModel>): Boolean {
        val category = categoriesList.find { it.name == categoryName }

        val totalDuration = timesheetsList
            .filter { it.category.equals(categoryName, ignoreCase = true) }
            .sumOf { it.duration }

        if (category != null) {
            category.totalHours = totalDuration
            return true
        }
        return false
    }



}
object HomeRepository {
    private val dailyGoalList = ArrayList<homeModel>()

    fun getDailyGoalList(): ArrayList<homeModel> {
        return dailyGoalList
    }

    fun addDailyGoal(dailyGoal: homeModel) {
        dailyGoalList.add(dailyGoal)
    }
}
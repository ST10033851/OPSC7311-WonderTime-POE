package com.example.opsc7311_wondertime_part2.models
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

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
    private val dailyGoalList = ArrayList<HomeModel>()

    fun getDailyGoalList() :ArrayList<HomeModel> {
        return dailyGoalList
    }

    fun addDailyGoal(dailyGoal: HomeModel) {
        dailyGoalList.add(dailyGoal)
    }
}
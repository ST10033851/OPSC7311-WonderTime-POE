package com.example.opsc7311_wondertime_part2.models
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimesheetRepository {
    private val timesheetsList = ArrayList<timesheetsModel>()

    fun getTimesheetsList(): ArrayList<timesheetsModel> {
        return timesheetsList
    }

    fun fetchTimesheetsFromDatabase(userId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Timesheets").child(userId)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                timesheetsList.clear()
                for (snapshot in dataSnapshot.children) {
                    val timesheet = snapshot.getValue(timesheetsModel::class.java)
                    if (timesheet != null) {
                        timesheetsList.add(timesheet)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    fun updateTotalHours(categoryName: String, duration: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        userId?.let { uid ->
            val database = FirebaseDatabase.getInstance()
            val categoriesRef = database.getReference("Categories").child(uid)

            categoriesRef.child(categoryName).child("totalHours").addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val currentTotalHours = dataSnapshot.getValue(Int::class.java) ?: 0
                        val newTotalHours = currentTotalHours + duration

                        val categoryUpdates = mapOf<String, Any>(
                            "totalHours" to newTotalHours
                        )

                        categoriesRef.child(categoryName).updateChildren(categoryUpdates)
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener { exception ->
                            }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                }
            )
        }
    }

    fun subtractTotalHours(categoryName: String, duration: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        userId?.let { uid ->
            val database = FirebaseDatabase.getInstance()
            val categoriesRef = database.getReference("Categories").child(uid)

            categoriesRef.child(categoryName).child("totalHours").addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val currentTotalHours = dataSnapshot.getValue(Int::class.java) ?: 0
                        val newTotalHours = (currentTotalHours - duration).coerceAtLeast(0)

                        val categoryUpdates = mapOf<String, Any>(
                            "totalHours" to newTotalHours
                        )

                        categoriesRef.child(categoryName).updateChildren(categoryUpdates)
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener { exception ->
                            }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                }
            )
        }
    }


    fun updateDailyTotalHours(duration:Int){
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        userId?.let { uid ->
            val database = FirebaseDatabase.getInstance()
            val goalsRef = database.getReference("DailyHours").child(uid)

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = sdf.format(Date())

            goalsRef.orderByChild("date").equalTo(today)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val currentTotalHours = snapshot.child("totalHours").getValue(Int::class.java) ?: 0
                            val newTotalHours = currentTotalHours + duration

                            snapshot.ref.child("totalHours").setValue(newTotalHours)
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener { exception ->
                                }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
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
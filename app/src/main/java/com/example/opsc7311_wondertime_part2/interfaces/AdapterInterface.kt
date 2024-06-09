package com.example.opsc7311_wondertime_part2.interfaces

import com.example.opsc7311_wondertime_part2.models.achievement_model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterInterface {
}

fun initializeAchievementsForUser(userId: String) {
    val achievementsRef = FirebaseDatabase.getInstance().getReference("achievements").child(userId)
    val defaultAchievements = listOf(
        achievement_model("First Login", "https://firebasestorage.googleapis.com/v0/b/opsc7311-wondertime-part2.appspot.com/o/achievements%2FAchievement1.png?alt=media&token=542ce881-ffc5-4d53-9c36-ab48e909ea87", true),
        achievement_model("Add your 1st Timesheet", "https://firebasestorage.googleapis.com/v0/b/opsc7311-wondertime-part2.appspot.com/o/achievements%2FAchievement6.png?alt=media&token=c1d9289e-ae0a-4e07-82c0-6a29124598e9", false),
        achievement_model("Set a Goal", "https://firebasestorage.googleapis.com/v0/b/opsc7311-wondertime-part2.appspot.com/o/achievements%2FAchievement2.png?alt=media&token=5f1698cc-31bd-45f1-9cf8-8b891130c570", false) ,
        achievement_model("Be among the first 10,000 users", "https://firebasestorage.googleapis.com/v0/b/opsc7311-wondertime-part2.appspot.com/o/achievements%2FAchievement5.png?alt=media&token=ecaf2450-dc58-4424-9d17-9a052b792997", true),
        achievement_model("Take a photo with your camera", "https://firebasestorage.googleapis.com/v0/b/opsc7311-wondertime-part2.appspot.com/o/achievements%2FAchievement3.png?alt=media&token=e16870d9-9a3a-41ac-957d-a32fd347b435", false),
        achievement_model("Add 100 Timesheets", "https://firebasestorage.googleapis.com/v0/b/opsc7311-wondertime-part2.appspot.com/o/achievements%2FAchievement7.png?alt=media&token=68dd4cb2-c3b1-42c2-b6bc-f6d7ebcda899", false)

    )

    for (achievement in defaultAchievements) {
        val achievementId = achievementsRef.push().key
        achievementsRef.child(achievementId!!).setValue(achievement)
    }
}
private fun initializeAchievementsForExistingUsers() {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val achievementsRef = FirebaseDatabase.getInstance().getReference("achievements").child(userId)

    achievementsRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (!snapshot.exists()) {
                val defaultAchievements = listOf(
                    achievement_model("First Login", "https://firebasestorage.googleapis.com/v0/b/opsc7311-wondertime-part2.appspot.com/o/achievements%2FAchievement1.png?alt=media&token=542ce881-ffc5-4d53-9c36-ab48e909ea87", true),
                    achievement_model("Add your 1st Timesheet", "https://firebasestorage.googleapis.com/v0/b/opsc7311-wondertime-part2.appspot.com/o/achievements%2FAchievement6.png?alt=media&token=c1d9289e-ae0a-4e07-82c0-6a29124598e9", false),
                    achievement_model("Set a Goal", "https://firebasestorage.googleapis.com/v0/b/opsc7311-wondertime-part2.appspot.com/o/achievements%2FAchievement2.png?alt=media&token=5f1698cc-31bd-45f1-9cf8-8b891130c570", false) ,
                    achievement_model("Be among the first 10,000 users", "https://firebasestorage.googleapis.com/v0/b/opsc7311-wondertime-part2.appspot.com/o/achievements%2FAchievement5.png?alt=media&token=ecaf2450-dc58-4424-9d17-9a052b792997", true),
                    achievement_model("Take a photo with your camera", "https://firebasestorage.googleapis.com/v0/b/opsc7311-wondertime-part2.appspot.com/o/achievements%2FAchievement3.png?alt=media&token=e16870d9-9a3a-41ac-957d-a32fd347b435", false),
                    achievement_model("Add 100 Timesheets", "https://firebasestorage.googleapis.com/v0/b/opsc7311-wondertime-part2.appspot.com/o/achievements%2FAchievement7.png?alt=media&token=68dd4cb2-c3b1-42c2-b6bc-f6d7ebcda899", false)
                )

                for (achievement in defaultAchievements) {
                    val achievementId = achievementsRef.push().key
                    achievementsRef.child(achievementId!!).setValue(achievement)
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
        }
    })
}
fun onAchievementsFeatureAccessed() {
    initializeAchievementsForExistingUsers()
}

 fun updateFirstTimesheetAchievement(userId: String) {
    val achievementsRef = FirebaseDatabase.getInstance().getReference("achievements").child(userId)

    achievementsRef.orderByChild("title").equalTo("Add your 1st Timesheet").addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()) {
                val achievementKey = dataSnapshot.children.first().key

                achievementsRef.child(achievementKey!!).child("completed").setValue(true).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                    } else {
                    }
                }
            } else {
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
        }
    })
}

fun updateFirstGoalAchievement(userId: String) {
    val achievementsRef = FirebaseDatabase.getInstance().getReference("achievements").child(userId)

    achievementsRef.orderByChild("title").equalTo("Set a Goal").addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()) {
                val achievementKey = dataSnapshot.children.first().key

                achievementsRef.child(achievementKey!!).child("completed").setValue(true).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                    } else {
                    }
                }
            } else {
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
        }
    })
}

fun updateFirstCameraAchievement(userId: String) {
    val achievementsRef = FirebaseDatabase.getInstance().getReference("achievements").child(userId)

    achievementsRef.orderByChild("title").equalTo("Take a photo with your camera").addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()) {
                val achievementKey = dataSnapshot.children.first().key

                achievementsRef.child(achievementKey!!).child("completed").setValue(true).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                    } else {
                    }
                }
            } else {
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
        }
    })
}






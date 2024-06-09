package com.example.opsc7311_wondertime_part2.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
import com.bumptech.glide.Glide
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.activities.TimesheetsActivity
import com.example.opsc7311_wondertime_part2.models.achievement_model
import com.example.opsc7311_wondertime_part2.models.categoriesModel
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AchievementAdapter(private val context: Context, private val achievements: List<achievement_model>, private val userId: String) :
    BaseAdapter() {

    override fun getCount(): Int {
        return achievements.size
    }

    override fun getItem(position: Int): achievement_model {
        return achievements[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.achievement_grid_item, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }
        val circularProgress = holder.progressBar
        val achievement = getItem(position)
        holder.title.text = achievement.title
        Glide.with(context)
            .load(achievement.imageUri)
            .into(holder.image)
        if (achievement.title == "Add 100 Timesheets") {
            val databaseRef = Firebase.database.reference
            val timesheetRef = databaseRef.child("Timesheets").child(userId)

            timesheetRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val timesheetCount = snapshot.childrenCount.toInt()

                    val maxProgress = 100.0
                    circularProgress.setMaxProgress(maxProgress)
                    circularProgress.setCurrentProgress(timesheetCount.toDouble())

                    if (timesheetCount >= 100) {
                        holder.caption.text = "Completed"
                        circularProgress.visibility = View.GONE
                    } else {
                        holder.caption.text = "Not Completed"
                        circularProgress.visibility = View.VISIBLE
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        } else {
            holder.caption.text = if (achievement.completed) "Completed" else "Not Completed"
            circularProgress.visibility = View.GONE
        }

        return view
    }

    inner class ViewHolder(view: View) {
        val progressBar: CircularProgressIndicator = view.findViewById(R.id.progressBar)
        val title: TextView = view.findViewById(R.id.AchievementHeading)
        val image: ImageView = view.findViewById(R.id.image)
        val caption: TextView = view.findViewById(R.id.AchievementCaption)
    }
}

package com.example.opsc7311_wondertime_part2.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.activities.TimesheetsActivity
import com.example.opsc7311_wondertime_part2.models.timesheetsModel

class TimesheetAdapter(val c: Context, var timesheetList: List<timesheetsModel>) : RecyclerView.Adapter<TimesheetAdapter.TimeViewHolder>(){
    inner class TimeViewHolder(val v: View): RecyclerView.ViewHolder(v){
        val date = v.findViewById<TextView>(R.id.date)
        val AssignedCategory = v.findViewById<TextView>(R.id.AssignedCategory)
        val Description = v.findViewById<TextView>(R.id.Description)
        val startTime = v.findViewById<TextView>(R.id.StartTime)
        val endTime = v.findViewById<TextView>(R.id.EndTime)
        val Image = v.findViewById<ImageView>(R.id.image)
        val DuplicateImage = v.findViewById<ImageView>(R.id.duplicateIcon)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val v  = inflator.inflate(R.layout.timesheet_row_layout,parent,false)
        return TimeViewHolder(v)
    }

    override fun getItemCount(): Int {
        return timesheetList.size
    }

    fun submitList(newList: List<timesheetsModel>) {
        timesheetList = newList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        val newTimeSheet = timesheetList[position]
        holder.date.text = newTimeSheet.date
        holder.AssignedCategory.text = newTimeSheet.category
        holder.Description.text = newTimeSheet.description
        holder.startTime.text = newTimeSheet.startTime
        holder.endTime.text = newTimeSheet.endTime
        Glide.with(holder.itemView)
            .load(newTimeSheet.imageUri)
            .placeholder(R.drawable.test_image)
            .error(R.drawable.test_image)
            .into(holder.Image)

        holder.DuplicateImage.setOnClickListener {
            (holder.itemView.context as TimesheetsActivity).showBottomDialog(newTimeSheet)
        }

    }
}
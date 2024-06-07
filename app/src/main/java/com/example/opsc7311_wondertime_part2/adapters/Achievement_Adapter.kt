package com.example.opsc7311_wondertime_part2.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.activities.TimesheetsActivity
import com.example.opsc7311_wondertime_part2.models.categoriesModel
import com.google.android.material.imageview.ShapeableImageView

class AchievementAdapter(val c: Context, private val achievementList: ArrayList<String>) : RecyclerView.Adapter<AchievementAdapter.AchivementViewHolder>(){
    inner class AchivementViewHolder(val v: View): RecyclerView.ViewHolder(v){
        //TODO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchivementViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val v  = inflator.inflate(R.layout.achievement_grid_item,parent,false)
        return AchivementViewHolder(v)
    }

    override fun getItemCount(): Int {
        return achievementList.size
    }

    override fun onBindViewHolder(holder: AchivementViewHolder, position: Int) {
        //TODO
    }
}
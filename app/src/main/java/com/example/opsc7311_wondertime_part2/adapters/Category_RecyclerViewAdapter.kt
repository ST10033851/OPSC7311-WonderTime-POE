package com.example.opsc7311_wondertime_part2.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.activities.TimesheetsActivity
import com.example.opsc7311_wondertime_part2.models.categoriesModel

class categoryAdapter(val c: Context, val categoryList: ArrayList<categoriesModel>) : RecyclerView.Adapter<categoryAdapter.CatViewHolder>(){
    inner class CatViewHolder(val v: View): RecyclerView.ViewHolder(v){
        val name = v.findViewById<TextView>(R.id.Catname)
        val TotalHours = v.findViewById<TextView>(R.id.HoursWorkedText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val v  = inflator.inflate(R.layout.category_row_layout,parent,false)
        return CatViewHolder(v)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: CatViewHolder, position: Int) {
        val newCat = categoryList[position]
        holder.name.text = newCat.name
        holder.TotalHours.text = newCat.totalHours.toString()

        holder.itemView.setOnClickListener {
            val selectedCategory = newCat.name
            val intent = Intent(c, TimesheetsActivity::class.java)
            intent.putExtra("categoryName", selectedCategory)
            c.startActivity(intent)

        }
    }
}
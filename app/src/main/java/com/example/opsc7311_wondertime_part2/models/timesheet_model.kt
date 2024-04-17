package com.example.opsc7311_wondertime_part2.models

data class timesheetsModel(
    val date : String,
    val startTime: String,
    val endTime: String ,
    val description: String,
    val category: String,
    val imageUri: Int
)
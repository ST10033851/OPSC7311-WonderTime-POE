package com.example.opsc7311_wondertime_part2.models

import android.net.Uri

data class timesheetsModel(
    val date : String,
    val startTime: String,
    val endTime: String ,
    val description: String,
    val category: String,
    val imageUri: Uri,
    val duration: Int
)
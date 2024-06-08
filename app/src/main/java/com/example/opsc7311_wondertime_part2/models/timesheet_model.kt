package com.example.opsc7311_wondertime_part2.models

import android.net.Uri

data class timesheetsModel(
    val id: String = "",
    val date : String = "",
    val startTime: String = "",
    val endTime: String ="",
    val description: String ="",
    val category: String = "",
    val imageUri: String = "",
    val duration: Int = 0
)
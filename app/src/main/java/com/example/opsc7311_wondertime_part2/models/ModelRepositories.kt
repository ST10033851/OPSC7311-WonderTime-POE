package com.example.opsc7311_wondertime_part2.models

object TimesheetRepository {
    private val timesheetsList = ArrayList<timesheetsModel>()

    fun getTimesheetsList(): ArrayList<timesheetsModel> {
        return timesheetsList
    }

    fun addTimesheet(timesheet: timesheetsModel) {
        timesheetsList.add(timesheet)
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
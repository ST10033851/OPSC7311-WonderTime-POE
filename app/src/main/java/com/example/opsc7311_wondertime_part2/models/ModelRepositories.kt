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

    fun calcTotalHours(categoryName: String, hoursWorked: Int): Boolean {
        val category = categoriesList.find { it.name == categoryName }
        if (category != null) {
            category.totalHours += hoursWorked
            return true
        }
        return false
    }
}
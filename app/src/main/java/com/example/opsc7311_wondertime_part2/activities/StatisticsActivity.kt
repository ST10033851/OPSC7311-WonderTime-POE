package com.example.opsc7311_wondertime_part2.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anychart.data.Set
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Bar
import com.anychart.core.cartesian.series.JumpLine
import com.anychart.data.Mapping
import com.anychart.enums.HoverMode
import com.anychart.enums.TooltipDisplayMode
import com.anychart.enums.TooltipPositionMode
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.databinding.ActivityStatisticsBinding
import com.example.opsc7311_wondertime_part2.models.HomeModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StatisticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatisticsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_statistics)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.graph

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    handleHomeNavigation()
                    true
                }

                R.id.categories -> {
                    handleCategoriesNavigation()
                    true

                }

                R.id.graph -> {
                    true
                }

                R.id.profile -> {
                    handleProfileNavigation()
                    true
                }

                else -> false
            }
        }

        populateChart()
    }


    private fun populateChart() {
        val anyChartView: AnyChartView = findViewById(R.id.any_chart_view)

        val vertical: Cartesian = AnyChart.vertical()

        vertical.animation(true)
            .title("Your Total Daily Hours")

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid
        val data: MutableList<DataEntry> = ArrayList()

        if (userId != null) {
            val database = FirebaseDatabase.getInstance().getReference("DailyHours").child(userId)

            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    for (snapshot in dataSnapshot.children) {
                        val dailyGoal = snapshot.getValue(HomeModel::class.java)
                        dailyGoal?.let {
                            val day = dailyGoal.date
                            val minGoal = dailyGoal.minimumGoal
                            val maxGoal = dailyGoal.maximumGoal
                            val actualHours = 10

                            data.add(CustomDataEntry(day,  actualHours, minGoal, maxGoal))
                        }
                    }
                    val set: Set = Set.instantiate()
                    set.data(data)
                    val barData: Mapping = set.mapAs("{ x: 'x', value: 'value' }")
                    val jumpLineData: Mapping = set.mapAs("{ x: 'x', value: 'jumpLine' }")
                    val MaxjumpLineData: Mapping = set.mapAs("{ x: 'x', value: 'maxGoal' }")

                    val bar: Bar = vertical.bar(barData)
                    bar.labels().format("{%Value} hrs")
                    bar.fill("#7B61FF")
                    bar.stroke("7B61FF")

                    val jumpLine: JumpLine = vertical.jumpLine(jumpLineData)
                    jumpLine.stroke("2 #60727B")
                    jumpLine.labels().enabled(false)

                    val MaxjumpLineL: JumpLine = vertical.jumpLine(MaxjumpLineData)
                    MaxjumpLineL.stroke("2 #60727B")
                    MaxjumpLineL.labels().enabled(false)

                    vertical.yScale().minimum(0.0)

                    vertical.labels(true)

                    vertical.tooltip()
                        .displayMode(TooltipDisplayMode.UNION)
                        .positionMode(TooltipPositionMode.POINT)
                        .unionFormat(
                            "function() {\n" +
                                    "      return 'Min: ' + this.points[1].value + ' hrs' +\n" +
                                    "       '\\n' + 'Max: ' + this.points[2].value + ' hrs' +\n" +
                                    "        '\\n' + 'Actual: ' + this.points[0].value + ' hrs';\n" +
                                    "    }"
                        )

                    vertical.interactivity().hoverMode(HoverMode.BY_X)

                    vertical.xAxis(true)
                    vertical.yAxis(true)
                    vertical.yAxis(0).labels().format("\${%Value} hrs")

                    anyChartView.setChart(vertical)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                }
            })
        } else {
            // Handle case when user is not logged in
        }


    }


    private class CustomDataEntry(x: String, value: Number, jumpLine: Number, maxGoal: Number) : ValueDataEntry(x, value) {
        init {
            setValue("jumpLine", jumpLine)
        }
    }

    private fun handleHomeNavigation(){
        startActivity(Intent(this, HomeActivity::class.java))
    }
    private fun handleOtherNavigation(){
        Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun handleProfileNavigation() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    private fun handleCategoriesNavigation() {
        startActivity(Intent(this, CategoriesActivity::class.java))
    }


    }

package com.example.opsc7311_wondertime_part2.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
import com.example.opsc7311_wondertime_part2.R
import com.example.opsc7311_wondertime_part2.activities.ui.theme.OPSC7311_WonderTime_POETheme
import com.example.opsc7311_wondertime_part2.databinding.ActivityStatisticsBinding
import com.example.opsc7311_wondertime_part2.interfaces.rememberMarker
import com.example.opsc7311_wondertime_part2.models.HomeModel
import com.example.opsc7311_wondertime_part2.models.TimesheetRepository.getTimesheetsList
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.edges.rememberFadingEdges
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollState
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.legend.legendItem
import com.patrykandpatrick.vico.compose.legend.verticalLegend
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.DefaultAlpha
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.entry.composed.ComposedChartEntryModelProducer
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.composed.plus
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.entriesOf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityStatisticsBinding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_statistics)

        val StatisticsRangePicker: ImageView = findViewById(R.id.StatisticsRangePicker)
        rangeInput = findViewById(R.id.StatisticsRangeInput)

        bottomNav= findViewById(R.id.bottomNavigationView)
        val user = FirebaseAuth.getInstance().currentUser!!
        val userId = user.uid
        database = Firebase.database.reference.child("DailyHours").child(userId)
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
        StatisticsRangePicker.setOnClickListener{ showRangePicker() }
        checkDataAndPopulateGraphs()

    }

    private fun checkDataAndPopulateGraphs() {
        val databaseReference = database

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    populateBarChart()
                    populateLineGraph()
                    fetchDataForCurrentMonth()

                    findViewById<TextView>(R.id.NoDataText).visibility = View.GONE
                    findViewById<ImageView>(R.id.NoDataImage).visibility = View.GONE
                } else {
                    findViewById<TextView>(R.id.NoDataText).visibility = View.VISIBLE
                    findViewById<ImageView>(R.id.NoDataImage).visibility = View.VISIBLE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }


    private fun showRangePicker() {
        if (getTimesheetsList().isEmpty()) {
            val errorDialog = Dialog(this)
            errorDialog.setContentView(R.layout.error_dialog)
            errorDialog.setCancelable(false)
            val errorMessageTextView = errorDialog.findViewById<TextView>(R.id.ErrorDescription)
            errorMessageTextView.text = getString(R.string.there_are_no_timesheets_entered)

            val dismissButton = errorDialog.findViewById<Button>(R.id.ErrorDone)
            dismissButton.setOnClickListener {
                errorDialog.dismiss()
            }

            errorDialog.show()
            return
        }

        val materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setSelection(androidx.core.util.Pair(
                MaterialDatePicker.thisMonthInUtcMilliseconds(),
                MaterialDatePicker.todayInUtcMilliseconds()
            ))
            .build()

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            val date1 = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                .format(Date(selection.first ?: 0))
            val date2 = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                .format(Date(selection.second ?: 0))

            populateBarChart(date1, date2)
            populateLineGraph(date1, date2)
            rangeInput.setText(getString(R.string.to, date1, date2))
        }

        materialDatePicker.show(supportFragmentManager, "tag")
    }



    private fun populateLineGraph(startDate: String? = null, endDate: String? = null) {
        val minGoalsList = ArrayList<Int>()
        val maxGoalsList = ArrayList<Int>()
        val cyanColor = Color(0xFF8A58EB)
        val redColor = Color(0xffffbb00)
        val composeView2 = findViewById<ComposeView>(R.id.compose_view2)
        var composedChartEntryModelProducer: ComposedChartEntryModelProducer? = null

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                minGoalsList.clear()
                maxGoalsList.clear()
                daysOfWeek.clear()
                if (dataSnapshot.exists()) {
                    for (graphsnapshot in dataSnapshot.children) {
                        val graphModel = graphsnapshot.getValue(HomeModel::class.java)
                        val minGoal = graphModel?.minimumGoal
                        val maxGoal = graphModel?.maximumGoal
                        val date = graphModel?.date

                        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("dd-MM", Locale.getDefault())

                        if (startDate != null && endDate != null) {
                            val startDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                            val endDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                            val parsedStartDate = startDateFormat.parse(startDate)
                            val parsedEndDate = endDateFormat.parse(endDate)
                            val parsedDate = inputFormat.parse(date!!)

                            if (parsedDate != null && (parsedDate.before(parsedStartDate) || parsedDate.after(parsedEndDate))) {
                                continue
                            }
                        }

                        val parsedDate = inputFormat.parse(date!!)
                        val formattedDate = outputFormat.format(parsedDate)
                        daysOfWeek.add(formattedDate)
                        minGoalsList.add(minGoal!!)
                        maxGoalsList.add(maxGoal!!)
                    }

                    composedChartEntryModelProducer = ComposedChartEntryModelProducer.build {
                        add(entriesOf(*maxGoalsList.mapIndexed { index, value -> index to value.toFloat() }.toTypedArray()))
                        add(entriesOf(*minGoalsList.mapIndexed { index, value -> index to value.toFloat() }.toTypedArray()))
                    }
                    composeView2.setContent {
                        val lineChart = lineChart(
                            lines = listOf(
                                LineChart.LineSpec(
                                    lineColor = cyanColor.toArgb(),
                                    point = ShapeComponent(shape = Shapes.pillShape, color = cyanColor.toArgb()),
                                    pointSizeDp = 8f,
                                    lineBackgroundShader = DynamicShaders.fromBrush(
                                        Brush.verticalGradient(
                                            listOf(
                                                cyanColor.copy(alpha = 0.2f),
                                                cyanColor.copy(alpha = 0f),
                                            ),
                                        ),
                                    ),
                                )
                            )
                        )
                        val lineChart2 = lineChart(
                            lines = listOf(
                                LineChart.LineSpec(
                                    lineColor = redColor.toArgb(),
                                    point = ShapeComponent(shape = Shapes.pillShape, color = redColor.toArgb()),
                                    pointSizeDp = 8f,
                                    lineBackgroundShader = DynamicShaders.fromBrush(
                                        Brush.verticalGradient(
                                            listOf(
                                                redColor.copy(alpha = 0.2f),
                                                redColor.copy(alpha = 0f),
                                            ),
                                        ),
                                    ),
                                )
                            )
                        )
                        OPSC7311_WonderTime_POETheme {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = Color.Transparent
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Chart(
                                        chart = remember(lineChart, lineChart2) { lineChart + lineChart2 },
                                        chartModelProducer = composedChartEntryModelProducer!!,
                                        startAxis = rememberStartAxis(),
                                        bottomAxis = rememberBottomAxis(
                                            titleComponent = textComponent(
                                                color = Color.Black,
                                                typeface = Typeface.MONOSPACE,
                                                padding = axisTitlePadding,
                                                margins = bottomAxisTitleMargins,
                                            ),

                                            valueFormatter = bottomAxisValueFormatter,
                                            title = "Date",
                                        ),
                                        legend = rememberLegend2(),
                                        marker = rememberMarker(),
                                        modifier = Modifier.height(300.dp),
                                        runInitialAnimation = true
                                    )
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun populateBarChart(startDate: String? = null, endDate: String? = null) {
        val actualHoursList = ArrayList<Int>()
        val composeView = findViewById<ComposeView>(R.id.compose_view)
        var composedChartEntryModelProducer: ComposedChartEntryModelProducer? = null

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                actualHoursList.clear()
                daysOfWeek.clear()
                if (dataSnapshot.exists()) {
                    for (graphsnapshot in dataSnapshot.children) {
                        val graphModel = graphsnapshot.getValue(HomeModel::class.java)
                        val actualHours = graphModel?.totalHours
                        val date = graphModel?.date

                        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("dd-MM", Locale.getDefault())

                        if (startDate != null && endDate != null) {
                            val startDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                            val endDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                            val parsedStartDate = startDateFormat.parse(startDate)
                            val parsedEndDate = endDateFormat.parse(endDate)
                            val parsedDate = inputFormat.parse(date!!)

                            if (parsedDate != null && (parsedDate.before(parsedStartDate) || parsedDate.after(parsedEndDate))) {
                                continue
                            }
                        }

                        val parsedDate = inputFormat.parse(date!!)
                        val formattedDate = outputFormat.format(parsedDate)
                        daysOfWeek.add(formattedDate)
                        actualHoursList.add(actualHours!!)
                    }

                    composedChartEntryModelProducer = ComposedChartEntryModelProducer.build {
                        add(entriesOf(*actualHoursList.mapIndexed { index, value -> index to value.toFloat() }.toTypedArray()))
                    }
                    composeView.setContent {

                        val purpleColor = Color(0xFF6200EE)

                        val columnChart = columnChart(
                            mergeMode = ColumnChart.MergeMode.Stack,
                            columns = listOf(
                                LineComponent(
                                    color = purpleColor.toArgb(),
                                    thicknessDp = 10f,
                                    shape = Shapes.cutCornerShape(25,25, 0,0)
                                )
                            )
                        )
                        OPSC7311_WonderTime_POETheme {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = Color.Transparent
                            ) {
                                Box(
                                    modifier = Modifier
                                ) {
                                    Chart(
                                        chart = remember(columnChart) { columnChart },
                                        chartModelProducer = composedChartEntryModelProducer!!,
                                        startAxis = rememberStartAxis(),
                                        bottomAxis = rememberBottomAxis(
                                            titleComponent = textComponent(
                                                color = Color.Black,
                                                padding = axisTitlePadding,
                                                margins = bottomAxisTitleMargins,
                                                typeface = Typeface.MONOSPACE,
                                            ),
                                            valueFormatter = bottomAxisValueFormatter,
                                            title = "Date",
                                        ),
                                        legend = rememberLegend(),
                                        marker = rememberMarker(),
                                        modifier = Modifier.height(300.dp),
                                        runInitialAnimation = true
                                    )
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun fetchDataForCurrentMonth() {
        val minGoalsList = ArrayList<Int>()
        val maxGoalsList = ArrayList<Int>()
        val actualHoursList = ArrayList<Int>()
        val daysOfWeek = ArrayList<String>()
        val currentDate = Calendar.getInstance()
        val startOfMonth = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                minGoalsList.clear()
                maxGoalsList.clear()
                actualHoursList.clear()
                daysOfWeek.clear()

                if (dataSnapshot.exists()) {
                    for (graphsnapshot in dataSnapshot.children) {
                        val graphModel = graphsnapshot.getValue(HomeModel::class.java)
                        val date = graphModel?.date

                        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val parsedDate = inputFormat.parse(date!!)

                        if (parsedDate.after(startOfMonth.time) && parsedDate.before(currentDate.time)) {
                            val minGoal = graphModel.minimumGoal
                            val maxGoal = graphModel.maximumGoal
                            val actualHours = graphModel.totalHours

                            minGoalsList.add(minGoal)
                            maxGoalsList.add(maxGoal)
                            actualHoursList.add(actualHours)
                            daysOfWeek.add(SimpleDateFormat("MM-dd", Locale.getDefault()).format(parsedDate))
                        }
                    }
                    calculateGoalAchievement(minGoalsList, maxGoalsList, actualHoursList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun calculateGoalAchievement(minGoalsList: List<Int>, maxGoalsList: List<Int>, actualHoursList: List<Int>) {
        var daysWithinGoals = 0

        for (i in actualHoursList.indices) {
            if (actualHoursList[i] in minGoalsList[i]..maxGoalsList[i]) {
                daysWithinGoals++
            }
        }

        val percentage = (daysWithinGoals.toDouble() / actualHoursList.size) * 100
        displayProgress(percentage)
    }

    private fun displayProgress(percentage: Double) {
        val circularProgress = findViewById<CircularProgressIndicator>(R.id.circular_progress)
        val maxProgress = 100.0
        val currentProgress = (percentage / 100 * maxProgress)
        circularProgress.setMaxProgress(maxProgress)
        circularProgress.setProgressTextAdapter(timeTextAdapter)
        circularProgress.setCurrentProgress(currentProgress)
    }


    private fun handleHomeNavigation(){
        startActivity(Intent(this, HomeActivity::class.java))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun handleProfileNavigation() {
        startActivity(Intent(this, ProfileActivity::class.java))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun handleCategoriesNavigation() {
        startActivity(Intent(this, CategoriesActivity::class.java))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    @Composable
    fun rememberLegend() = verticalLegend(
        items = chartColors.mapIndexed { index, chartColor ->
            legendItem(
                icon = ShapeComponent(shape = Shapes.pillShape, color = chartColor.toArgb()),
                label = textComponent(
                    color = currentChartStyle.axis.axisLabelColor,
                    textSize = legendItemLabelTextSize,
                    typeface = Typeface.MONOSPACE,
                ),
                labelText = labels[index],
            )
        },
        iconSize = legendItemIconSize,
        iconPadding = legendItemIconPaddingValue,
        spacing = legendItemSpacing,
        padding = legendPadding,
    )
    @Composable
    fun rememberLegend2() = verticalLegend(
        items = chartColors2.mapIndexed { index, chartColor ->
            legendItem(
                icon = ShapeComponent(shape = Shapes.pillShape, color = chartColor.toArgb()),
                label = textComponent(
                    color = currentChartStyle.axis.axisLabelColor,
                    textSize = legendItemLabelTextSize,
                    typeface = Typeface.MONOSPACE,
                ),
                labelText = labels2[index],
            )
        },
        iconSize = legendItemIconSize,
        iconPadding = legendItemIconPaddingValue,
        spacing = legendItemSpacing,
        padding = legendPadding,
    )

    private val timeTextAdapter =
        CircularProgressIndicator.ProgressTextAdapter { time -> String.format("%.0f%%", time) }

    private lateinit var database: DatabaseReference
    private lateinit var bottomNav: BottomNavigationView

    private val legendItemSpacing = 4.dp
    private val legendItemIconSize = 8.dp
    private val legendTopPaddingValue = 8.dp
    private val axisTitlePadding = dimensionsOf(8.dp, 2.dp)
    private val legendItemLabelTextSize = 12.sp
    private val bottomAxisTitleMargins = dimensionsOf(top = 4.dp)
    private val legendItemIconPaddingValue = 10.dp
    private val legendPadding = dimensionsOf(top = legendTopPaddingValue)

    private val chartColors = listOf(Color(0xFF6200EE))
    private val chartColors2 = listOf(Color(0xffffbb00),Color(0xFF8A58EB) )
    private var daysOfWeek = ArrayList<String>()
    private lateinit var rangeInput: EditText
    private val labels = listOf("Total Hours")
    private val labels2 = listOf("Minimum Hours", "Maximum Hours")
    private val bottomAxisValueFormatter =
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { x, _ -> daysOfWeek[x.toInt() % daysOfWeek.size] }

}


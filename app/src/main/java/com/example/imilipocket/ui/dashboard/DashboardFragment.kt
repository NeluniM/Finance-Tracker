package com.example.imilipocket.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.imilipocket.R
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.data.Transaction
import com.example.imilipocket.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DashboardViewModel
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            _binding = FragmentDashboardBinding.inflate(inflater, container, false)
            return binding.root
        } catch (e: Exception) {
            Log.e("DashboardFragment", "Error in onCreateView: ${e.message}")
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            setupViewModel()
            setupUI()
            observeViewModel()
            // Only add sample transactions if there are no existing transactions
            if (preferenceManager.getTransactions().isEmpty()) {
                addSampleTransactions()
            } else {
                viewModel.loadDashboardData()
            }
        } catch (e: Exception) {
            Log.e("DashboardFragment", "Error in onViewCreated: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupViewModel() {
        preferenceManager = PreferenceManager(requireContext())
        val factory = object : ViewModelProvider.NewInstanceFactory() {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(preferenceManager) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(DashboardViewModel::class.java)
    }

    private fun setupUI() {
        setupPieChart()
        setupLineCharts()
    }

    private fun observeViewModel() {
        setupObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {
        viewModel.totalBalance.observe(viewLifecycleOwner) { balance ->
            try {
                binding.tvTotalBalance.text = formatCurrency(balance ?: 0.0)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvTotalBalance.text = formatCurrency(0.0)
            }
        }

        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            try {
                binding.tvTotalIncome.text = formatCurrency(income ?: 0.0)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvTotalIncome.text = formatCurrency(0.0)
            }
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            try {
                binding.tvTotalExpense.text = formatCurrency(expense ?: 0.0)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvTotalExpense.text = formatCurrency(0.0)
            }
        }

        viewModel.categorySpending.observe(viewLifecycleOwner) { spending ->
            try {
                updatePieChart(spending ?: emptyMap())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            try {
                updateLineCharts(transactions ?: emptyList())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupPieChart() {
        try {
            binding.pieChart.apply {
                description.isEnabled = false
                legend.isEnabled = true
                setHoleColor(android.R.color.transparent)
                setTransparentCircleColor(android.R.color.transparent)
                setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                setEntryLabelTextSize(12f)
                setUsePercentValues(true)
                setDrawEntryLabels(true)
                setDrawHoleEnabled(true)
                setHoleRadius(50f)
                setTransparentCircleRadius(55f)
                setRotationEnabled(true)
                setHighlightPerTapEnabled(true)
                legend.textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
                legend.textSize = 12f
                animateY(1000)
                setNoDataText("No transactions yet")
                setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupLineCharts() {
        try {
            val commonSetup: com.github.mikephil.charting.charts.LineChart.() -> Unit = {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)
                xAxis.apply {
                    textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return try {
                                dateFormat.format(Date(value.toLong()))
                            } catch (e: Exception) {
                                ""
                            }
                        }
                    }
                }
                axisLeft.apply {
                    textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return formatCurrency(value.toDouble())
                        }
                    }
                }
                axisRight.isEnabled = false
                setNoDataText("No transactions recorded yet")
                setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            }

            binding.incomeChart.apply(commonSetup)
            binding.expenseChart.apply(commonSetup)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updatePieChart(spending: Map<String, Double>) {
        try {
            if (spending.isEmpty()) {
                binding.pieChart.setNoDataText("No expense data available")
                binding.pieChart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                binding.pieChart.invalidate()
                return
            }

            val entries = spending.map { (category, amount) ->
                PieEntry(amount.toFloat(), category)
            }

            val colors = listOf(
                R.color.chart_color_1,
                R.color.chart_color_2,
                R.color.chart_color_3,
                R.color.chart_color_4,
                R.color.chart_color_5
            ).map { ContextCompat.getColor(requireContext(), it) }

            val dataSet = PieDataSet(entries, "Expenses by Category").apply {
                this.colors = colors
                valueFormatter = PercentFormatter(binding.pieChart)
                valueTextSize = 12f
                valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                valueLinePart1Length = 0.4f
                valueLinePart2Length = 0.4f
                valueLineColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
            }

            binding.pieChart.apply {
                data = PieData(dataSet)
                setUsePercentValues(true)
                description.isEnabled = false
                legend.isEnabled = true
                legend.textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
                legend.orientation = Legend.LegendOrientation.VERTICAL
                legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                setEntryLabelTextSize(12f)
                animateY(1000)
                invalidate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.pieChart.setNoDataText("Unable to load spending data")
            binding.pieChart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            binding.pieChart.invalidate()
        }
    }

    private fun updateLineCharts(transactions: List<Transaction>) {
        try {
            // Process income entries
            val incomeEntries = transactions
                .filter { it.type == Transaction.Type.INCOME }
                .groupBy { it.date }
                .map { (date, txns) ->
                    Entry(
                        date.toFloat(),
                        txns.sumOf { it.amount }.toFloat()
                    )
                }
                .sortedBy { it.x }

            // Process expense entries
            val expenseEntries = transactions
                .filter { it.type == Transaction.Type.EXPENSE }
                .groupBy { it.date }
                .map { (date, txns) ->
                    Entry(
                        date.toFloat(),
                        txns.sumOf { it.amount }.toFloat()
                    )
                }
                .sortedBy { it.x }

            // Update Income Chart
            binding.incomeChart.apply {
                if (incomeEntries.isNotEmpty()) {
                    val dataSet = LineDataSet(incomeEntries, "Income").apply {
                        color = ContextCompat.getColor(requireContext(), R.color.green_500)
                        setDrawCircles(true)
                        setDrawValues(true)
                        lineWidth = 2f
                        circleRadius = 4f
                        valueTextSize = 12f
                        valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
                        mode = LineDataSet.Mode.LINEAR
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return formatCurrency(value.toDouble())
                            }
                        }
                    }
                    data = LineData(dataSet)
                    animateX(1000)
                } else {
                    data = null
                    setNoDataText("No income transactions recorded")
                }
                invalidate()
            }

            // Update Expense Chart
            binding.expenseChart.apply {
                if (expenseEntries.isNotEmpty()) {
                    val dataSet = LineDataSet(expenseEntries, "Expenses").apply {
                        color = ContextCompat.getColor(requireContext(), R.color.red_500)
                        setDrawCircles(true)
                        setDrawValues(true)
                        lineWidth = 2f
                        circleRadius = 4f
                        valueTextSize = 12f
                        valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
                        mode = LineDataSet.Mode.LINEAR
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return formatCurrency(value.toDouble())
                            }
                        }
                    }
                    data = LineData(dataSet)
                    animateX(1000)
                } else {
                    data = null
                    setNoDataText("No expense transactions recorded")
                }
                invalidate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.incomeChart.apply {
                data = null
                setNoDataText("Unable to load income data")
                setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                invalidate()
            }
            binding.expenseChart.apply {
                data = null
                setNoDataText("Unable to load expense data")
                setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                invalidate()
            }
        }
    }

    private fun formatCurrency(amount: Double): String {
        return try {
            val format = NumberFormat.getCurrencyInstance()
            val symbols = format.currency?.symbol?.let {
                format.currency = Currency.getInstance("LKR")
                format.maximumFractionDigits = 2
                format.minimumFractionDigits = 2
                format
            } ?: format
            symbols.format(amount).replace("LKR", "Rs")
        } catch (e: Exception) {
            e.printStackTrace()
            "Rs 0.00"
        }
    }

    private fun addSampleTransactions() {
        Log.d("DashboardFragment", "Adding sample transactions")
        
        // Sample income transactions
        val sampleTransactions = listOf(
            Transaction(
                title = "Monthly Salary",
                amount = 50000.0,
                category = "Salary",
                type = Transaction.Type.INCOME,
                date = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000) // 5 days ago
            ),
            Transaction(
                title = "Freelance Project",
                amount = 15000.0,
                category = "Freelance",
                type = Transaction.Type.INCOME,
                date = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000) // 2 days ago
            ),
            Transaction(
                title = "Grocery Shopping",
                amount = 2500.0,
                category = Transaction.CATEGORY_FOOD,
                type = Transaction.Type.EXPENSE,
                date = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000) // 3 days ago
            ),
            Transaction(
                title = "Utility Bills",
                amount = 3000.0,
                category = Transaction.CATEGORY_BILLS,
                type = Transaction.Type.EXPENSE,
                date = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000) // 1 day ago
            )
        )

        // Add all sample transactions
        sampleTransactions.forEach { transaction ->
            preferenceManager.addTransaction(transaction)
        }

        Log.d("DashboardFragment", "Added ${sampleTransactions.size} sample transactions")

        // Refresh the dashboard data
        viewModel.loadDashboardData()
    }
} 
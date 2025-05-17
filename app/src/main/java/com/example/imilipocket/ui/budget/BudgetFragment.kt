package com.example.imilipocket.ui.budget

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.imilipocket.R
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.databinding.FragmentBudgetBinding
import com.example.imilipocket.util.NotificationHelper
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var viewModel: BudgetViewModel
    private lateinit var notificationHelper: NotificationHelper

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, update notifications
            updateBudgetProgress()
        } else {
            Toast.makeText(requireContext(), 
                "Notification permission is required for budget alerts", 
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            _binding = FragmentBudgetBinding.inflate(inflater, container, false)
            preferenceManager = PreferenceManager(requireContext())
            viewModel = ViewModelProvider(
                this,
                BudgetViewModel.Factory(preferenceManager)
            )[BudgetViewModel::class.java]
            notificationHelper = NotificationHelper(requireContext())

            checkNotificationPermission()
            setupUI()
            setupClickListeners()
            observeViewModel()
            setupBarChart()

            return binding.root
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error initializing budget screen", Toast.LENGTH_SHORT).show()
            return binding.root
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            updateBudgetProgress()
            updateBarChart()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Toast.makeText(requireContext(), 
                        "Notification permission is needed for budget alerts", 
                        Toast.LENGTH_LONG).show()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun setupUI() {
        try {
            val currentBudget = preferenceManager.getMonthlyBudget()
            binding.etMonthlyBudget.setText(currentBudget.toString())
            updateBudgetProgress()
        } catch (e: Exception) {
            e.printStackTrace()
            binding.etMonthlyBudget.setText("0")
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveBudget.setOnClickListener {
            saveBudget()
        }
    }

    private fun observeViewModel() {
        viewModel.budget.observe(viewLifecycleOwner) { budget ->
            try {
                binding.etMonthlyBudget.setText(budget.toString())
                updateBudgetProgress()
                updateBarChart()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveBudget() {
        try {
            val budget = binding.etMonthlyBudget.text.toString().toDouble()
            if (budget < 0) {
                Toast.makeText(requireContext(), "Budget cannot be negative", Toast.LENGTH_SHORT).show()
                return
            }
            viewModel.updateBudget(budget)
            showBudgetNotification()
            Toast.makeText(requireContext(), "Budget updated", Toast.LENGTH_SHORT).show()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid budget amount", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error saving budget", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBudgetNotification() {
        try {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val monthlyBudget = preferenceManager.getMonthlyBudget()
                notificationHelper.showBudgetNotification(monthlyBudget)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateBudgetProgress() {
        try {
            val monthlyBudget = preferenceManager.getMonthlyBudget()
            val monthlyExpenses = viewModel.getMonthlyExpenses()
            val progress = if (monthlyBudget > 0) {
                (monthlyExpenses / monthlyBudget * 100).toInt()
            } else {
                0
            }
            binding.progressBudget.progress = progress
            binding.tvBudgetStatus.text = "$progress%"
            
            // Show budget alert notification
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    notificationHelper.showBudgetAlert(monthlyBudget, monthlyExpenses)
                    Toast.makeText(requireContext(), 
                        "Budget progress: $progress%", 
                        Toast.LENGTH_SHORT).show()
                }
            } else {
                // For older Android versions, just show the notification
                notificationHelper.showBudgetAlert(monthlyBudget, monthlyExpenses)
                Toast.makeText(requireContext(), 
                    "Budget progress: $progress%", 
                    Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.progressBudget.progress = 0
            binding.tvBudgetStatus.text = "0%"
        }
    }

    private fun setupBarChart() {
        try {
            binding.barChart.apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)
                axisLeft.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return formatCurrency(value.toDouble())
                    }
                }
                axisRight.isEnabled = false
                setNoDataText("No budget data available")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateBarChart() {
        try {
            val monthlyBudget = preferenceManager.getMonthlyBudget()
            val monthlyExpenses = viewModel.getMonthlyExpenses()
            val remaining = (monthlyBudget - monthlyExpenses).coerceAtLeast(0.0)

            val entries = listOf(
                BarEntry(0f, monthlyExpenses.toFloat()),
                BarEntry(1f, remaining.toFloat())
            )

            val dataSet = BarDataSet(entries, "Budget vs Expenses").apply {
                colors = listOf(
                    ContextCompat.getColor(requireContext(), R.color.red_500),
                    ContextCompat.getColor(requireContext(), R.color.green_500)
                )
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return formatCurrency(value.toDouble())
                    }
                }
                valueTextSize = 12f
                setDrawValues(true)
            }

            val barData = BarData(dataSet).apply {
                barWidth = 0.5f
            }

            binding.barChart.apply {
                data = barData
                setFitBars(true)
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when (value.toInt()) {
                            0 -> "Expenses"
                            1 -> "Remaining"
                            else -> ""
                        }
                    }
                }
                xAxis.granularity = 1f
                xAxis.labelCount = 2
                animateY(1000)
                invalidate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.barChart.apply {
                clear()
                setNoDataText("Error loading budget data")
                invalidate()
            }
        }
    }

    private fun formatCurrency(amount: Double): String {
        return try {
            val format = NumberFormat.getCurrencyInstance()
            format.currency = Currency.getInstance("LKR")
            format.format(amount).replace("LKR", "Rs.")
        } catch (e: Exception) {
            "Rs. %.2f".format(amount)
        }
    }
} 
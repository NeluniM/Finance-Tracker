package com.example.imilipocket.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.data.Transaction
import kotlinx.coroutines.launch

class DashboardViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {
    private val _totalBalance = MutableLiveData<Double>(0.0)
    val totalBalance: LiveData<Double> = _totalBalance

    private val _totalIncome = MutableLiveData<Double>(0.0)
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpense = MutableLiveData<Double>(0.0)
    val totalExpense: LiveData<Double> = _totalExpense

    private val _categorySpending = MutableLiveData<Map<String, Double>>(emptyMap())
    val categorySpending: LiveData<Map<String, Double>> = _categorySpending

    private val _transactions = MutableLiveData<List<Transaction>>(emptyList())
    val transactions: LiveData<List<Transaction>> = _transactions

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                val transactions = preferenceManager.getTransactions()
                if (transactions.isNotEmpty()) {
                    _transactions.value = transactions
                    calculateTotals(transactions)
                    calculateCategorySpending(transactions)
                } else {
                    _transactions.value = emptyList()
                    _totalBalance.value = 0.0
                    _totalIncome.value = 0.0
                    _totalExpense.value = 0.0
                    _categorySpending.value = emptyMap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _transactions.value = emptyList()
                _totalBalance.value = 0.0
                _totalIncome.value = 0.0
                _totalExpense.value = 0.0
                _categorySpending.value = emptyMap()
            }
        }
    }

    private fun calculateTotals(transactions: List<Transaction>) {
        try {
            val income = transactions
                .filter { it.type == Transaction.Type.INCOME }
                .sumOf { it.amount }

            val expense = transactions
                .filter { it.type == Transaction.Type.EXPENSE }
                .sumOf { it.amount }

            _totalIncome.value = income
            _totalExpense.value = expense
            _totalBalance.value = income - expense
        } catch (e: Exception) {
            e.printStackTrace()
            _totalIncome.value = 0.0
            _totalExpense.value = 0.0
            _totalBalance.value = 0.0
        }
    }

    private fun calculateCategorySpending(transactions: List<Transaction>) {
        try {
            if (transactions.isEmpty()) {
                _categorySpending.value = emptyMap()
                return
            }

            // Calculate expense categories
            val expenseByCategory = transactions
                .filter { it.type == Transaction.Type.EXPENSE }
                .groupBy { it.category }
                .mapValues { (_, transactions) -> 
                    transactions.sumOf { it.amount }
                }
                .filter { it.value > 0 }  // Only include categories with expenses

            if (expenseByCategory.isNotEmpty()) {
                _categorySpending.value = expenseByCategory
            } else {
                _categorySpending.value = emptyMap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _categorySpending.value = emptyMap()
        }
    }
} 
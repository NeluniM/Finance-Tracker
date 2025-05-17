package com.example.imilipocket.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import android.util.Log
import com.example.imilipocket.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()
    private val context: Context = context

    companion object {
        private const val PREF_NAME = "ImiliPocketPrefs"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
        private const val KEY_SELECTED_CURRENCY = "selected_currency"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val DEFAULT_CURRENCY = "$"
        private const val TAG = "PreferenceManager"
    }

    fun getTransactions(): List<Transaction> {
        return try {
            val json = sharedPreferences.getString(KEY_TRANSACTIONS, "[]")
            val type = object : TypeToken<List<Transaction>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun saveTransactions(transactions: List<Transaction>) {
        try {
            val json = gson.toJson(transactions)
            sharedPreferences.edit().putString(KEY_TRANSACTIONS, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addTransaction(transaction: Transaction) {
        try {
            val transactions = getTransactions().toMutableList()
            transactions.add(transaction)
            saveTransactions(transactions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateTransaction(transaction: Transaction) {
        try {
            val transactions = getTransactions().toMutableList()
            val index = transactions.indexOfFirst { it.id == transaction.id }
            if (index != -1) {
                transactions[index] = transaction
                saveTransactions(transactions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        try {
            val transactions = getTransactions().toMutableList()
            transactions.removeIf { it.id == transaction.id }
            saveTransactions(transactions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getMonthlyBudget(): Double {
        return try {
            sharedPreferences.getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }

    fun setMonthlyBudget(budget: Double) {
        try {
            sharedPreferences.edit().putFloat(KEY_MONTHLY_BUDGET, budget.toFloat()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSelectedCurrency(): String {
        return try {
            sharedPreferences.getString(KEY_SELECTED_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
        } catch (e: Exception) {
            e.printStackTrace()
            DEFAULT_CURRENCY
        }
    }

    fun setSelectedCurrency(currency: String) {
        try {
            sharedPreferences.edit().putString(KEY_SELECTED_CURRENCY, currency).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCategories(): List<String> {
        return try {
            context.resources.getStringArray(R.array.transaction_categories).toList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false)
    }

    fun getMonthlyExpenses(): Double {
        return try {
            val currentMonth = Date().month
            getTransactions()
                .filter { 
                    it.type == Transaction.Type.EXPENSE && 
                    Date(it.date).month == currentMonth 
                }
                .sumOf { it.amount }
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }

    fun getBackupFiles(): List<File> {
        try {
            val backupDir = File(context.getExternalFilesDir(null), "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
                return emptyList()
            }
            return backupDir.listFiles()?.filter { it.isFile && it.name.endsWith(".json") }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting backup files: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    fun createBackup(backupData: String): Boolean {
        try {
            // Use app-specific storage directory
            val backupDir = File(context.getExternalFilesDir(null), "backups").apply {
                if (!exists()) {
                    mkdirs()
                }
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "backup_$timestamp.json")

            try {
                backupFile.writeText(backupData)
                Log.d(TAG, "Backup created successfully at: ${backupFile.absolutePath}")
                return true
            } catch (e: IOException) {
                Log.e(TAG, "Error writing backup file: ${e.message}")
                e.printStackTrace()
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating backup: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    fun restoreFromBackup(backupFile: File): Boolean {
        try {
            val backupData = backupFile.readText()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val data = gson.fromJson<Map<String, Any>>(backupData, type)

            data["transactions"]?.let {
                val transactions = gson.fromJson<List<Transaction>>(
                    gson.toJson(it),
                    object : TypeToken<List<Transaction>>() {}.type
                )
                saveTransactions(transactions)
            }

            data["budget"]?.let {
                setMonthlyBudget((it as Number).toDouble())
            }

            data["currency"]?.let {
                setSelectedCurrency(it as String)
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring from backup: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
} 
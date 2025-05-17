package com.example.imilipocket.ui.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.data.Transaction

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

class EditTransactionViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {
    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    fun updateTransaction(transaction: Transaction) {
        try {
            preferenceManager.updateTransaction(transaction)
            _updateResult.value = Result.Success(Unit)
        } catch (e: Exception) {
            _updateResult.value = Result.Error(e)
        }
    }

    fun getCategories(): List<String> {
        return preferenceManager.getCategories()
    }
}

class EditTransactionViewModelFactory(
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditTransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditTransactionViewModel(preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 
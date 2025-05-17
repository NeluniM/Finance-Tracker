package com.example.imilipocket.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.imilipocket.data.PreferenceManager

class SettingsViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {
    private val _currency = MutableLiveData<String>()
    val currency: LiveData<String> = _currency

    private val _isDarkMode = MutableLiveData<Boolean>()
    val isDarkMode: LiveData<Boolean> = _isDarkMode

    fun initialize() {
        loadCurrency()
        loadDarkMode()
    }

    private fun loadCurrency() {
        _currency.value = preferenceManager.getSelectedCurrency()
    }

    private fun loadDarkMode() {
        _isDarkMode.value = preferenceManager.isDarkMode()
    }

    fun updateCurrency(newCurrency: String) {
        preferenceManager.setSelectedCurrency(newCurrency)
        _currency.value = newCurrency
    }

    fun updateDarkMode(enabled: Boolean) {
        preferenceManager.setDarkMode(enabled)
        _isDarkMode.value = enabled
    }

    fun setSelectedCurrency(currency: String) {
        val currencyCode = currency.substring(0, 3)
        preferenceManager.setSelectedCurrency(currencyCode)
    }

    fun loadSettings() {
        loadCurrency()
        loadDarkMode()
    }
} 
package com.example.imilipocket.ui.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.imilipocket.R
import com.example.imilipocket.databinding.FragmentSettingsBinding
import com.example.imilipocket.data.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import java.io.File

class SettingsFragment : Fragment() {
    companion object {
        private const val TAG = "SettingsFragment"
    }

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var viewModel: SettingsViewModel
    private lateinit var gson: Gson

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            showMessage("Permissions granted")
            // Retry the operation
            binding.apply {
                if (btnBackup.isPressed) {
                    createBackup()
                } else if (btnRestore.isPressed) {
                    restoreBackup()
                }
            }
        } else {
            showSettingsDialog()
        }
    }

    private val restoreFilePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context?.contentResolver?.openInputStream(it)?.use { inputStream ->
                    val tempFile = File.createTempFile("backup", ".json", requireContext().cacheDir)
                    tempFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    preferenceManager.restoreFromBackup(tempFile)
                    Toast.makeText(context, "Restore successful!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error restoring backup: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {
            _binding = FragmentSettingsBinding.inflate(inflater, container, false)
            return binding.root
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreateView: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            initializeComponents()
            setupUI()
            setupClickListeners()
            observeViewModel()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated: ${e.message}")
            e.printStackTrace()
            showError("Error initializing settings")
        }
    }

    private fun initializeComponents() {
        try {
            preferenceManager = PreferenceManager(requireContext())
            viewModel = ViewModelProvider(
                this,
                SettingsViewModelFactory(preferenceManager)
            )[SettingsViewModel::class.java]
            viewModel.initialize()
            gson = Gson()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing components: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private fun setupUI() {
        try {
            // Setup currency spinner
            val currencies = resources.getStringArray(R.array.currencies)
            val currencyAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                currencies
            )
            binding.spinnerCurrency.setAdapter(currencyAdapter)

            // Setup dark mode switch
            binding.switchDarkMode.isChecked = preferenceManager.isDarkMode()
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupUI: ${e.message}")
            e.printStackTrace()
            showError("Error setting up UI")
        }
    }

    private fun setupClickListeners() {
        try {
            binding.apply {
                btnSaveCurrency.setOnClickListener {
                    try {
                        val selectedCurrency = spinnerCurrency.text.toString()
                        if (selectedCurrency.isNotEmpty()) {
                            val currencyCode = selectedCurrency.substring(0, 3)
                            viewModel.updateCurrency(currencyCode)
                            showMessage("Currency saved")
                        } else {
                            showError("Please select a currency")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving currency: ${e.message}")
                        e.printStackTrace()
                        showError("Error saving currency")
                    }
                }

                switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
                    try {
                        val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                        AppCompatDelegate.setDefaultNightMode(mode)
                        preferenceManager.setDarkMode(isChecked)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error changing theme: ${e.message}")
                        e.printStackTrace()
                        showError("Error changing theme")
                    }
                }

                btnBackup.setOnClickListener {
                    checkPermissionsAndProceed(true)
                }

                btnRestore.setOnClickListener {
                    checkPermissionsAndProceed(false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupClickListeners: ${e.message}")
            e.printStackTrace()
            showError("Error setting up buttons")
        }
    }

    private fun checkPermissionsAndProceed(isBackup: Boolean) {
        // For Android 10 and above, we don't need external storage permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (isBackup) {
                createBackup()
            } else {
                restoreBackup()
            }
            return
        }

        // For Android 9 and below
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (permissions.all { permission ->
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            if (isBackup) {
                createBackup()
            } else {
                restoreBackup()
            }
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun handleBackupRestore() {
        // Implementation will be added based on the button that was clicked
    }

    private fun createBackup() {
        try {
            val transactions = preferenceManager.getTransactions()
            val budget = preferenceManager.getMonthlyBudget()
            val currency = preferenceManager.getSelectedCurrency()

            val backupData = gson.toJson(mapOf(
                "transactions" to transactions,
                "budget" to budget,
                "currency" to currency
            ))

            if (preferenceManager.createBackup(backupData)) {
                val backupDir = requireContext().getExternalFilesDir(null)?.absolutePath ?: ""
                showMessage("Backup created in $backupDir/backups")
            } else {
                showError("Failed to create backup. Please check app permissions.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in backup: ${e.message}")
            e.printStackTrace()
            showError("Error creating backup: ${e.message}")
        }
    }

    private fun restoreBackup() {
        restoreFilePicker.launch("application/json")
    }

    private fun showSettingsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permissions Required")
            .setMessage("Storage permissions are required for backup and restore. Please enable them in settings.")
            .setPositiveButton("Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { _, _ ->
                showMessage("Permission denied. Cannot proceed with backup/restore.")
            }
            .show()
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
            startActivity(this)
        }
    }

    private fun observeViewModel() {
        try {
            viewModel.currency.observe(viewLifecycleOwner) { currency ->
                try {
                    val currencies = binding.spinnerCurrency.adapter as ArrayAdapter<String>
                    val currencyIndex = currencies.getPosition(currency)
                    if (currencyIndex != -1) {
                        binding.spinnerCurrency.setText(currencies.getItem(currencyIndex), false)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating currency display: ${e.message}")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in observeViewModel: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showError(message: String) {
        try {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing error message: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showMessage(message: String) {
        try {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing message: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
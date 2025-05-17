package com.example.imilipocket.ui.transactions

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.imilipocket.R
import com.example.imilipocket.data.Transaction
import com.example.imilipocket.databinding.FragmentTransactionsBinding
import com.example.imilipocket.data.PreferenceManager
import kotlinx.coroutines.launch

class TransactionsFragment : Fragment() {
    companion object {
        private const val TAG = "TransactionsFragment"
    }

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var adapter: TransactionsAdapter
    private val viewModel: TransactionsViewModel by viewModels {
        try {
            preferenceManager = PreferenceManager(requireContext())
            TransactionsViewModelFactory(preferenceManager, requireContext())
        } catch (e: Exception) {
            Log.e(TAG, "Error creating ViewModel: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return try {
            Log.d(TAG, "Creating view...")
            _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
            binding.root
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreateView: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            Log.d(TAG, "Setting up view...")
            setupRecyclerView()
            setupClickListeners()
            observeViewModel()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated: ${e.message}")
            e.printStackTrace()
            showError("Error setting up the transactions view")
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            Log.d(TAG, "Fragment resumed, loading transactions...")
            if (isAdded && !isDetached) {
                viewModel.loadTransactions()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume: ${e.message}")
            e.printStackTrace()
            showError("Error loading transactions")
        }
    }

    private fun setupRecyclerView() {
        try {
            Log.d(TAG, "Setting up RecyclerView...")
            adapter = TransactionsAdapter(
                onEditClick = { transaction ->
                    try {
                        findNavController().navigate(
                            R.id.action_navigation_transactions_to_editTransactionFragment,
                            Bundle().apply {
                                putParcelable("transaction", transaction)
                            }
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error navigating to edit: ${e.message}")
                        e.printStackTrace()
                        showError("Error opening edit screen")
                    }
                },
                onDeleteClick = { transaction ->
                    showDeleteConfirmationDialog(transaction)
                }
            )
            binding.recyclerViewTransactions.adapter = adapter
            binding.recyclerViewTransactions.layoutManager = LinearLayoutManager(requireContext())
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView: ${e.message}")
            e.printStackTrace()
            showError("Error setting up the transaction list")
        }
    }

    private fun setupClickListeners() {
        try {
            Log.d(TAG, "Setting up click listeners...")
            binding.fabAddTransaction.setOnClickListener {
                try {
                    findNavController().navigate(R.id.action_navigation_transactions_to_addTransactionFragment)
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to add transaction: ${e.message}")
                    e.printStackTrace()
                    showError("Error opening add transaction screen")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners: ${e.message}")
            e.printStackTrace()
            showError("Error setting up buttons")
        }
    }

    private fun observeViewModel() {
        try {
            Log.d(TAG, "Setting up ViewModel observers...")
            viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
                try {
                    Log.d(TAG, "Received ${transactions?.size ?: 0} transactions")
                    adapter.submitList(transactions)
                    updateEmptyState(transactions?.isEmpty() == true)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating transaction list: ${e.message}")
                    e.printStackTrace()
                    showError("Error updating the transaction list")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error observing ViewModel: ${e.message}")
            e.printStackTrace()
            showError("Error loading transaction data")
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.textViewEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewTransactions.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        try {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete") { _, _ ->
                    try {
                        viewModel.deleteTransaction(transaction)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deleting transaction: ${e.message}")
                        e.printStackTrace()
                        showError("Error deleting the transaction")
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing delete dialog: ${e.message}")
            e.printStackTrace()
            showError("Error showing delete confirmation")
        }
    }

    private fun showError(message: String) {
        try {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing toast: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        try {
            Log.d(TAG, "Destroying view...")
            binding.recyclerViewTransactions.adapter = null
            _binding = null
            super.onDestroyView()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroyView: ${e.message}")
            e.printStackTrace()
        }
    }
} 
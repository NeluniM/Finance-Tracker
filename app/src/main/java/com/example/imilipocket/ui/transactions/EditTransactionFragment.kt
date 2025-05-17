package com.example.imilipocket.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.imilipocket.R
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.data.Transaction
import com.example.imilipocket.databinding.FragmentEditTransactionBinding

class EditTransactionFragment : Fragment() {
    private var _binding: FragmentEditTransactionBinding? = null
    private val binding get() = _binding!!
    private val args: EditTransactionFragmentArgs by navArgs()
    private val viewModel: EditTransactionViewModel by viewModels {
        EditTransactionViewModelFactory(PreferenceManager(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        val transaction = args.transaction
        binding.amountEditText.setText(transaction.amount.toString())
        binding.descriptionEditText.setText(transaction.title)

        binding.saveButton.setOnClickListener {
            updateTransaction()
        }
    }

    private fun updateTransaction() {
        val amount = binding.amountEditText.text.toString().toDoubleOrNull()
        val title = binding.descriptionEditText.text.toString()

        if (amount == null) {
            binding.amountEditText.error = "Please enter a valid amount"
            return
        }

        if (title.isBlank()) {
            binding.descriptionEditText.error = "Please enter a title"
            return
        }

        val updatedTransaction = args.transaction.copy(
            amount = amount,
            title = title,
            date = System.currentTimeMillis() // Update the modification date
        )

        viewModel.updateTransaction(updatedTransaction)
    }

    private fun observeViewModel() {
        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    Toast.makeText(requireContext(), "Transaction updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(), "Error updating transaction: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
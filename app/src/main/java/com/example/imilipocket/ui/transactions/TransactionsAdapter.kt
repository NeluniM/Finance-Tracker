package com.example.imilipocket.ui.transactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.imilipocket.R
import com.example.imilipocket.data.Transaction
import java.text.SimpleDateFormat
import java.util.*

class TransactionsAdapter(
    private val onEditClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionsAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        private val amountTextView: TextView = itemView.findViewById(R.id.textViewAmount)
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewDate)
        private val categoryTextView: TextView = itemView.findViewById(R.id.textViewCategory)
        private val editButton: ImageButton = itemView.findViewById(R.id.buttonEdit)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDelete)

        fun bind(transaction: Transaction) {
            titleTextView.text = transaction.title
            amountTextView.text = String.format("%.2f", transaction.amount)
            amountTextView.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (transaction.type == Transaction.Type.INCOME) R.color.income_green else R.color.expense_red
                )
            )
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            dateTextView.text = dateFormat.format(Date(transaction.date))
            categoryTextView.text = transaction.category

            editButton.setOnClickListener { onEditClick(transaction) }
            deleteButton.setOnClickListener { onDeleteClick(transaction) }
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
} 
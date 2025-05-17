package com.example.imilipocket.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: String,
    val type: Type,
    val date: Long = System.currentTimeMillis()
) : Parcelable {
    enum class Type {
        INCOME, EXPENSE
    }

    companion object {
        const val CATEGORY_FOOD = "Food"
        const val CATEGORY_TRANSPORT = "Transport"
        const val CATEGORY_BILLS = "Bills"
        const val CATEGORY_ENTERTAINMENT = "Entertainment"
        const val CATEGORY_OTHER = "Other"

        val DEFAULT_CATEGORIES = listOf(
            CATEGORY_FOOD,
            CATEGORY_TRANSPORT,
            CATEGORY_BILLS,
            CATEGORY_ENTERTAINMENT,
            CATEGORY_OTHER
        )
    }
} 
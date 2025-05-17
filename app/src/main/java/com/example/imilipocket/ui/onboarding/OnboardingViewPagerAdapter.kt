package com.example.imilipocket.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.imilipocket.R
import com.example.imilipocket.databinding.ItemOnboardingBinding

class OnboardingViewPagerAdapter : RecyclerView.Adapter<OnboardingViewPagerAdapter.OnboardingViewHolder>() {

    private val onboardingItems = listOf(
        OnboardingItem(
            "Track Every Penny",
            "Effortlessly monitor your daily expenses and income with our intuitive expense tracker",
            R.drawable.ic_onboarding_track
        ),
        OnboardingItem(
            "Set & Achieve Goals",
            "Create custom budgets, set savings targets, and watch your financial dreams come true",
            R.drawable.ic_onboarding_goals
        ),
        OnboardingItem(
            "Smart Insights",
            "Get personalized financial insights and tips to help you make better money decisions",
            R.drawable.ic_onboarding_insights
        )
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(onboardingItems[position])
    }

    override fun getItemCount() = onboardingItems.size

    class OnboardingViewHolder(private val binding: ItemOnboardingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OnboardingItem) {
            binding.apply {
                textTitle.text = item.title
                textDescription.text = item.description
                imageOnboarding.setImageResource(item.imageResId)
            }
        }
    }

    data class OnboardingItem(
        val title: String,
        val description: String,
        val imageResId: Int
    )
} 
package com.ssac.place.activity.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainFragmentStateAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> HomeFragment.newInstance("param1", "param2")
            1 -> SearchFragment.newInstance()
            2 -> LikeFragment.newInstance()
            else -> MyFragment.newInstance()
        }
    }
}
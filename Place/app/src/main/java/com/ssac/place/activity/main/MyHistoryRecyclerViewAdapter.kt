package com.ssac.place.activity.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssac.place.R

class MyHistoryRecyclerViewAdapter(
    private val context: Context,
    private val historyList: List<String>,
    private val onClickListener: View.OnClickListener
): RecyclerView.Adapter<MyHistoryRecyclerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHistoryRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_history_recycler_view, parent, false)
        return MyHistoryRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyHistoryRecyclerViewHolder, position: Int) {
        holder.setHistory(historyList[position])
    }

    override fun getItemCount(): Int {
        return historyList.count()
    }
}

class MyHistoryRecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val imageView: ImageView = view.findViewById(R.id.imageView)
    private val nameTextView: TextView = view.findViewById(R.id.nameTextView)
    private val explainTextView: TextView = view.findViewById(R.id.explainTextView)
    private val dateTextView: TextView = view.findViewById(R.id.dateTextView)

    fun setHistory(history: String) {

    }
}
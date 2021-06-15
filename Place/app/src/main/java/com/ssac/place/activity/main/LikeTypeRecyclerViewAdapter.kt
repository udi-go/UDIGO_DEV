package com.ssac.place.activity.main

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ssac.place.R

class LikeTypeRecyclerViewAdapter(
    private val context: Context,
    private val typeList: List<String>,
    private val onClickListener: (View)->Unit
): RecyclerView.Adapter<LikeTypeRecyclerViewHolder>() {
    private var selectedPosition: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeTypeRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_like_type_recycler_view, parent, false)
        return LikeTypeRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: LikeTypeRecyclerViewHolder, position: Int) {
        holder.setType(typeList[position])
        holder.setSelected(context, selectedPosition==position)
        holder.itemView.setOnClickListener {
            val type = it.tag as String
            selectType(type)
            onClickListener(it)
        }
    }

    override fun getItemCount(): Int {
        return typeList.count()
    }

    private fun selectType(type: String) {
        val oldPosition = selectedPosition
        typeList.indexOfFirst { it == type }.let { newPosition ->
            if (oldPosition != newPosition) {
                selectedPosition = newPosition
                notifyItemChanged(oldPosition)
                notifyItemChanged(newPosition)
            }
        }
    }
}

class LikeTypeRecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val backgroundLayout: ConstraintLayout = view.findViewById(R.id.backgroundLayout)
    private val nameTextView: TextView = view.findViewById(R.id.nameTextView)

    fun setType(type: String) {
        itemView.tag = type
        nameTextView.text = type
    }

    fun setSelected(context: Context, selected: Boolean) {
        if (selected) {
            nameTextView.setTextColor(context.getColor(R.color.white))
            nameTextView.setTypeface(nameTextView.typeface, Typeface.BOLD)
            backgroundLayout.background = AppCompatResources.getDrawable(context, R.drawable.drawable_gradation_40_button_background)
        } else {
            nameTextView.setTextColor(context.getColor(R.color.black))
            nameTextView.setTypeface(nameTextView.typeface, Typeface.NORMAL)
            backgroundLayout.background = AppCompatResources.getDrawable(context, R.drawable.drawable_gray_border_40_button_background)
        }
    }
}
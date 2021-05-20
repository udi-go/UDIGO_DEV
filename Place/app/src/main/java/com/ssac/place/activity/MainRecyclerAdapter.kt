package com.ssac.place.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssac.place.R
import com.ssac.place.models.KakaoDocument

class MainRecyclerAdapter(
        private val context: Context,
        private val documentList: List<KakaoDocument>,
        private val onClickListener: View.OnClickListener
): RecyclerView.Adapter<MainRecyclerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_main_recycler_view, parent, false)
        return MainRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainRecyclerViewHolder, position: Int) {
        holder.setDocument(documentList[position])
        holder.itemView.tag = position
        holder.itemView.setOnClickListener(onClickListener)
    }

    override fun getItemCount(): Int {
        return documentList.count()
    }
}

class MainRecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val nameTextView: TextView = view.findViewById(R.id.nameTextView)
    private val addressTextView: TextView = view.findViewById(R.id.addressTextView)

    fun setDocument(document: KakaoDocument) {
        nameTextView.text = document.place_name
        addressTextView.text = document.address()
    }
}
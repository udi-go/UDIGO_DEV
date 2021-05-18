package com.ssac.place

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssac.place.models.TravelRecommend

class SearchDetailRecyclerAdapter(
    private val context: Context,
    private val recommendList: List<TravelRecommend>,
    private val onClickListener: View.OnClickListener
): RecyclerView.Adapter<SearchDetailRecyclerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchDetailRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_detail_recycler_view_holder, parent, false)
        return SearchDetailRecyclerViewHolder(view)
    }
    override fun onBindViewHolder(holder: SearchDetailRecyclerViewHolder, position: Int) {
        holder.setRecommend(context, recommendList[position])
        holder.itemView.setOnClickListener(onClickListener)
    }

    override fun getItemCount(): Int {
        return recommendList.count()
    }
}

class SearchDetailRecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val imageView: ImageView = view.findViewById(R.id.imageView)
    private val titleTextView: TextView = view.findViewById(R.id.titleTextView)
    private val addressTextView: TextView = view.findViewById(R.id.addressTextView)

    fun setRecommend(context: Context, recommend: TravelRecommend) {
        Glide.with(context).load(recommend.firstimage2).into(imageView)
        titleTextView.text = recommend.title
        addressTextView.text = recommend.addr1
        itemView.tag = recommend.contentid
    }
}
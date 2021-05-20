package com.ssac.place.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssac.place.R
import com.ssac.place.models.TravelRecommend

class RecommendRecyclerAdapter(
    private val context: Context,
    private val recommendList: List<TravelRecommend>,
    private val onClickListener: View.OnClickListener
): RecyclerView.Adapter<RecommendRecyclerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recommend_recycler_view_holder, parent, false)
        return RecommendRecyclerViewHolder(view)
    }
    override fun onBindViewHolder(holder: RecommendRecyclerViewHolder, position: Int) {
        holder.setRecommend(context, recommendList[position])
        holder.itemView.setOnClickListener(onClickListener)
    }

    override fun getItemCount(): Int {
        return recommendList.count()
    }
}

class RecommendRecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
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

class RecommendTypeRecyclerAdapter(
        private val recommendList: List<TravelRecommend>,
        private val onClickListener: View.OnClickListener
): RecyclerView.Adapter<RecommendTypeRecyclerViewHolder>() {
    private val typeList: List<String> = recommendList.mapNotNull { it.contentTypeId }.toSet().toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendTypeRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recommend_type_recycler_view_holder, parent, false)
        return RecommendTypeRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendTypeRecyclerViewHolder, position: Int) {
        holder.setName(typeNameMap[typeList[position]])
        holder.itemView.tag = typeList[position]
        holder.itemView.setOnClickListener(onClickListener)
    }

    override fun getItemCount(): Int = typeList.count()

    companion object {
        private val typeNameMap = mapOf(
                "12" to "관광지",
                "14" to "문화시설",
                "15" to "축제/공연/행사",
                "25" to "여행코스",
                "28" to "레포츠",
                "32" to "숙박",
                "38" to "쇼핑",
                "39" to "음식"
        )
    }
}

class RecommendTypeRecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val nameTextView: TextView = view.findViewById(R.id.nameTextView)

    fun setName(name: String?) {
        nameTextView.text = name
    }
}
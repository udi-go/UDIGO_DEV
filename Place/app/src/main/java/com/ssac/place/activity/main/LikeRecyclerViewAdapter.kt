package com.ssac.place.activity.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssac.place.R
import com.ssac.place.models.MyLike

class LikeRecyclerViewAdapter(
    private val context: Context,
    private val likeList: List<MyLike>,
    private val onClickListener: View.OnClickListener
): RecyclerView.Adapter<LikeRecyclerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_like_recycler_view, parent, false)
        return LikeRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: LikeRecyclerViewHolder, position: Int) {
        holder.setLike(context, likeList[position])
        holder.itemView.setOnClickListener(onClickListener)
    }

    override fun getItemCount(): Int {
        return likeList.count()
    }
}

class LikeRecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val imageView: ImageView = view.findViewById(R.id.imageView)
    private val nameTextView: TextView = view.findViewById(R.id.nameTextView)
    private val addressTextView: TextView = view.findViewById(R.id.addressTextView)

    fun setLike(context: Context, like: MyLike) {
        Glide.with(context).load(like.image).circleCrop().placeholder(R.drawable.drawable_round_image_place_holder).into(imageView)
        nameTextView.text = like.title
        addressTextView.text = like.address
        itemView.tag = like.place_id
    }
}
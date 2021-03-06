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
import com.ssac.place.models.MyReview

class MyReviewRecyclerViewAdapter(
    private val context: Context,
    private val reviewList: List<MyReview>,
    private val onClickListener: View.OnClickListener,
    private val onEditClickListener: View.OnClickListener
): RecyclerView.Adapter<MyReviewRecyclerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyReviewRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_review_recycler_view, parent, false)
        return MyReviewRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyReviewRecyclerViewHolder, position: Int) {
        holder.setReview(context, reviewList[position])
        holder.setEditButton(position, onEditClickListener)
        holder.itemView.tag = position
        holder.itemView.setOnClickListener(onClickListener)
    }

    override fun getItemCount(): Int {
        return reviewList.count()
    }
}

class MyReviewRecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val editTextView: TextView = view.findViewById(R.id.editTextView)
    private val imageView: ImageView = view.findViewById(R.id.imageView)
    private val nameTextView: TextView = view.findViewById(R.id.nameTextView)
    private val ratingImageView1: ImageView = view.findViewById(R.id.ratingImageView1)
    private val ratingImageView2: ImageView = view.findViewById(R.id.ratingImageView2)
    private val ratingImageView3: ImageView = view.findViewById(R.id.ratingImageView3)
    private val ratingImageView4: ImageView = view.findViewById(R.id.ratingImageView4)
    private val ratingImageView5: ImageView = view.findViewById(R.id.ratingImageView5)
    private val dateTextView: TextView = view.findViewById(R.id.dateTextView)
    private val contentsTextView: TextView = view.findViewById(R.id.contentsTextView)

    fun setReview(context: Context, review: MyReview) {
        Glide.with(context).load(review.image).circleCrop().placeholder(R.drawable.drawable_round_image_place_holder).into(imageView)
        nameTextView.text = review.place_title
        if (review.grade > 1) {
            ratingImageView2.setImageResource(R.drawable.ic_star_on)
        } else {
            ratingImageView2.setImageResource(R.drawable.ic_star_off)
        }
        if (review.grade > 2) {
            ratingImageView3.setImageResource(R.drawable.ic_star_on)
        } else {
            ratingImageView3.setImageResource(R.drawable.ic_star_off)
        }
        if (review.grade > 3) {
            ratingImageView4.setImageResource(R.drawable.ic_star_on)
        } else {
            ratingImageView4.setImageResource(R.drawable.ic_star_off)
        }
        if (review.grade > 4) {
            ratingImageView5.setImageResource(R.drawable.ic_star_on)
        } else {
            ratingImageView5.setImageResource(R.drawable.ic_star_off)
        }
        dateTextView.text = review.displayDate()
        contentsTextView.text = review.text
    }

    fun setEditButton(position: Int, onClickListener: View.OnClickListener) {
        editTextView.tag = position
        editTextView.setOnClickListener(onClickListener)
    }
}
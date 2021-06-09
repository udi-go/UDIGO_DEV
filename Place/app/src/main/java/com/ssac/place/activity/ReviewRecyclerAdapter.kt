package com.ssac.place.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssac.place.R
import com.ssac.place.models.MyReview

class ReviewRecyclerAdapter(
    private val context: Context,
    private val reviewList: List<MyReview>,
    private val onClickListener: View.OnClickListener?
): RecyclerView.Adapter<ReviewRecyclerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review_recycler_view, parent, false)
        return ReviewRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewRecyclerViewHolder, position: Int) {
        holder.setReview(reviewList[position])
    }

    override fun getItemCount(): Int {
        return reviewList.count()
    }
}

class ReviewRecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val userNameTextView: TextView = view.findViewById(R.id.userNameTextView)
    private val contentsTextView: TextView = view.findViewById(R.id.contentsTextView)

    fun setReview(review: MyReview) {
        userNameTextView.text = review.user_nickname
        contentsTextView.text = review.text
    }
}
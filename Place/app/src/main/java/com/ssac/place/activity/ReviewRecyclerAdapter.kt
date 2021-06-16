package com.ssac.place.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssac.place.R
import com.ssac.place.models.PlaceReview
import com.ssac.place.repository.LocalRepository

class ReviewRecyclerAdapter(
        private val context: Context,
        private val reviewList: List<PlaceReview>,
        private val onEditClickListener: View.OnClickListener?
): RecyclerView.Adapter<ReviewRecyclerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review_recycler_view, parent, false)
        return ReviewRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewRecyclerViewHolder, position: Int) {
        holder.setReview(reviewList[position], position, onEditClickListener)
    }

    override fun getItemCount(): Int {
        return reviewList.count()
    }
}

class ReviewRecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val editTextView: TextView = view.findViewById(R.id.editTextView)
    private val userNameTextView: TextView = view.findViewById(R.id.userNameTextView)
    private val ratingImageView1: ImageView = view.findViewById(R.id.ratingImageView1)
    private val ratingImageView2: ImageView = view.findViewById(R.id.ratingImageView2)
    private val ratingImageView3: ImageView = view.findViewById(R.id.ratingImageView3)
    private val ratingImageView4: ImageView = view.findViewById(R.id.ratingImageView4)
    private val ratingImageView5: ImageView = view.findViewById(R.id.ratingImageView5)
    private val contentsTextView: TextView = view.findViewById(R.id.contentsTextView)

    fun setReview(review: PlaceReview, position: Int, onEditClickListener: View.OnClickListener?) {
        if (LocalRepository.instance.isMyReview(review.review_id)) {
            editTextView.visibility = View.VISIBLE
            editTextView.tag = position
            editTextView.setOnClickListener(onEditClickListener)
        } else {
            editTextView.visibility = View.GONE
            editTextView.tag = -1
            editTextView.setOnClickListener(null)
        }
        userNameTextView.text = review.user_nickname
        if (review.grade.toInt() > 1) {
            ratingImageView2.setImageResource(R.drawable.ic_star_on)
        } else {
            ratingImageView2.setImageResource(R.drawable.ic_star_off)
        }
        if (review.grade.toInt() > 2) {
            ratingImageView3.setImageResource(R.drawable.ic_star_on)
        } else {
            ratingImageView3.setImageResource(R.drawable.ic_star_off)
        }
        if (review.grade.toInt() > 3) {
            ratingImageView4.setImageResource(R.drawable.ic_star_on)
        } else {
            ratingImageView4.setImageResource(R.drawable.ic_star_off)
        }
        if (review.grade.toInt() > 4) {
            ratingImageView5.setImageResource(R.drawable.ic_star_on)
        } else {
            ratingImageView5.setImageResource(R.drawable.ic_star_off)
        }
        contentsTextView.text = review.text
    }
}
package com.ssac.place.activity

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssac.place.R
import com.ssac.place.models.TravelRecommend

class RecommendRecyclerAdapter(
    private val context: Context,
    private val recommendList: List<TravelRecommend>,
    private val onClickListener: View.OnClickListener): RecyclerView.Adapter<RecommendRecyclerViewHolder>() {
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
    private val likeButton: ImageButton = view.findViewById(R.id.likeButton)
    private val imageView: ImageView = view.findViewById(R.id.imageView)
    private val nameTextView: TextView = view.findViewById(R.id.nameTextView)
    private val addressTextView: TextView = view.findViewById(R.id.addressTextView)

    fun setRecommend(context: Context, recommend: TravelRecommend) {
        Glide.with(context).load(recommend.firstimage2).circleCrop().placeholder(R.drawable.drawable_round_image_place_holder).into(imageView)
        nameTextView.text = recommend.title
        addressTextView.text = recommend.addr1
        itemView.tag = recommend.contentid
    }
}

class RecommendTypeRecyclerAdapter(
        private val context: Context,
        private val recommendList: List<TravelRecommend>,
        private val onClickListener: (View)->Unit
): RecyclerView.Adapter<RecommendTypeRecyclerViewHolder>() {
    private val typeList: List<String> = recommendList.mapNotNull { it.contentTypeId }.toSet().toMutableList().apply { add(0, TYPE_ALL) }
    private var selectedPosition: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendTypeRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recommend_type_recycler_view_holder, parent, false)
        return RecommendTypeRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendTypeRecyclerViewHolder, position: Int) {
        holder.setType(typeNameMap[typeList[position]])
        holder.setSelected(context, position == selectedPosition)
        holder.itemView.tag = typeList[position]
        holder.itemView.setOnClickListener{
            val typeId = it.tag as String
            selectType(typeId)
            onClickListener(it)
        }
    }

    override fun getItemCount(): Int = typeList.count()

    private fun selectType(typeId: String) {
        val oldPosition = selectedPosition
        typeList.indexOfFirst { it == typeId }.let { newPosition ->
            if (oldPosition != newPosition) {
                selectedPosition = newPosition
                notifyItemChanged(oldPosition)
                notifyItemChanged(newPosition)
            }
        }
    }

    companion object {
        const val TYPE_ALL = "all"
        private val typeNameMap = mapOf(
                TYPE_ALL to "전체",
                "12" to "관광지",
                "14" to "문화시설",
                "15" to "공연",
                "25" to "여행코스",
                "28" to "레포츠",
                "32" to "숙박",
                "38" to "쇼핑",
                "39" to "식당"
        )
    }
}

class RecommendTypeRecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val backgroundLayout: ConstraintLayout = view.findViewById(R.id.backgroundLayout)
    private val nameTextView: TextView = view.findViewById(R.id.nameTextView)

    fun setType(type: String?) {
        nameTextView.text = type
    }

    fun setSelected(context: Context, selected: Boolean) {
        if (selected) {
            nameTextView.setTextColor(context.getColor(R.color.white))
            nameTextView.setTypeface(null, Typeface.BOLD)
            backgroundLayout.background = AppCompatResources.getDrawable(context, R.drawable.drawable_gradation_40_button_background)
        } else {
            nameTextView.setTextColor(context.getColor(R.color.black))
            nameTextView.setTypeface(null, Typeface.NORMAL)
            backgroundLayout.background = AppCompatResources.getDrawable(context, R.drawable.drawable_gray_border_40_button_background)
        }
    }
}
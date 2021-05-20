package com.ssac.place.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssac.place.R
import com.ssac.place.TravelApis
import com.ssac.place.TravelDetailResponse
import com.ssac.place.TravelSearchResponse
import com.ssac.place.models.TravelDetail
import com.ssac.place.models.TravelRecommend
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TravelDetailActivity : AppCompatActivity() {
    private val contentId: String by lazy { intent.getStringExtra("contentId") as String }
    private val latitude: String by lazy { intent.getStringExtra("latitude") as String }
    private val longitude: String by lazy { intent.getStringExtra("longitude") as String }

    private val imageView: ImageView by lazy { findViewById(R.id.imageView) }
    private val titleTextView: TextView by lazy { findViewById(R.id.titleTextView) }
    private val addressTextView: TextView by lazy { findViewById(R.id.addressTextView) }
    private val homepageTextView: TextView by lazy { findViewById(R.id.homepageTextView) }
    private val telTextView: TextView by lazy { findViewById(R.id.telTextView) }
    private val overviewTextView: TextView by lazy { findViewById(R.id.overviewTextView) }
    private val reviewRecyclerView: RecyclerView by lazy { findViewById(R.id.reviewRecyclerView) }
    private val recommendationTextView: TextView by lazy { findViewById(R.id.recommendationTextView) }
    private val recommendationRecyclerView: RecyclerView by lazy { findViewById(R.id.recommendationRecyclerView) }

    private var recommendList: List<TravelRecommend> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_detail)

        requestTravelDetail()
        requestRecommendation()
    }

    private fun requestTravelDetail() {
        TravelApis.getInstance().detail(contentId).enqueue(object : Callback<TravelDetailResponse> {
            override fun onResponse(call: Call<TravelDetailResponse>, response: Response<TravelDetailResponse>) {
                if (response.isSuccessful) {
                    response.body()?.body?.items?.firstOrNull()?.let {
                        setTravelDetail(it)
                    }
                }
            }

            override fun onFailure(call: Call<TravelDetailResponse>, t: Throwable) {
                Log.d("AAA", t.localizedMessage)
            }
        })
    }

    private fun requestRecommendation() {
        TravelApis.getInstance().search(longitude.toDouble(), latitude.toDouble()).enqueue(object : Callback<TravelSearchResponse> {
            override fun onResponse(
                    call: Call<TravelSearchResponse>,
                    response: Response<TravelSearchResponse>
            ) {
                if (response.isSuccessful) {
                    val items = response.body()?.body?.items
                    if (items == null) {

                    } else {
                        setRecommendList(items)
                    }
                }
            }

            override fun onFailure(call: Call<TravelSearchResponse>, t: Throwable) {
                Log.d("AAA", t.localizedMessage)
            }
        })
    }

    private fun setTravelDetail(detail: TravelDetail) {
        Glide.with(this).load(detail.firstimage).into(imageView)
        titleTextView.text = detail.title
        addressTextView.text = detail.address()
        detail.homepage?.let {
            homepageTextView.text = HtmlCompat.fromHtml(it, FROM_HTML_MODE_LEGACY)
            homepageTextView.movementMethod = LinkMovementMethod.getInstance()
        }
        telTextView.text = detail.tel
        detail.overview?.let {
            overviewTextView.text = HtmlCompat.fromHtml(it, FROM_HTML_MODE_COMPACT)
        }
        recommendationTextView.text = "${detail.title} 근처 가볼만한 곳"
    }

    private fun setRecommendList(list: List<TravelRecommend>) {
        recommendList = list
        recommendationRecyclerView.adapter = RecommendRecyclerAdapter(this, recommendList) {
            moveToTravelDetail(it.tag as? String)
        }
    }

    private fun moveToTravelDetail(contentId: String?) {
        contentId?.let {
            val recommend = recommendList.first { it.contentid == contentId }
            val intent = Intent(this, TravelDetailActivity::class.java)
            intent.putExtra("contentId", it)
            intent.putExtra("latitude", recommend.mapy)
            intent.putExtra("longitude", recommend.mapx)
            startActivity(intent)
        }
    }
}
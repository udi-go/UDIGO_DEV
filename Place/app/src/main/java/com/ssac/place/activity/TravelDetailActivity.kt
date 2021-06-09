package com.ssac.place.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kakao.sdk.auth.AuthApiClient
import com.ssac.place.R
import com.ssac.place.TravelApis
import com.ssac.place.TravelDetailResponse
import com.ssac.place.TravelSearchResponse
import com.ssac.place.models.MyReview
import com.ssac.place.models.TravelDetail
import com.ssac.place.models.TravelRecommend
import com.ssac.place.networks.FetchReviewListResponse
import com.ssac.place.networks.MyApis
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
    private val createReviewButton: Button by lazy { findViewById(R.id.createReviewButton) }
    private val reviewRecyclerView: RecyclerView by lazy { findViewById(R.id.reviewRecyclerView) }
    private val noReviewTextView: TextView by lazy { findViewById(R.id.noReviewTextView) }
    private val recommendationTextView: TextView by lazy { findViewById(R.id.recommendationTextView) }
    private val recommendationRecyclerView: RecyclerView by lazy { findViewById(R.id.recommendationRecyclerView) }

    private var travelDetail: TravelDetail? = null
    private var recommendList: List<TravelRecommend> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_detail)

        requestTravelDetail()
        requestReviewList()
        requestRecommendation()
    }

    private fun requestTravelDetail() {
        TravelApis.getInstance().detail(contentId).enqueue(object : Callback<TravelDetailResponse> {
            override fun onResponse(call: Call<TravelDetailResponse>, response: Response<TravelDetailResponse>) {
                if (response.isSuccessful) {
                    response.body()?.body?.items?.firstOrNull()?.let {
                        setTravelDetail(it)
                        createReviewButton.isEnabled = true
                    }
                }
            }

            override fun onFailure(call: Call<TravelDetailResponse>, t: Throwable) {
                Log.d("AAA", t.localizedMessage)
            }
        })
    }

    private fun requestReviewList() {
        MyApis.getInstance().fetchReviewList(contentId.toInt(), "tour").enqueue(object : Callback<FetchReviewListResponse> {
            override fun onResponse(call: Call<FetchReviewListResponse>, response: Response<FetchReviewListResponse>) {
                if (response.isSuccessful) {
                    setReviewList(response.body()?.reviews)
                }
            }

            override fun onFailure(call: Call<FetchReviewListResponse>, t: Throwable) {
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
        travelDetail = detail
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

    private fun setReviewList(list: List<MyReview>?) {
        if (list.isNullOrEmpty()) {
            noReviewTextView.visibility = View.VISIBLE
            reviewRecyclerView.visibility = View.GONE
        } else {
            noReviewTextView.visibility = View.GONE
            reviewRecyclerView.visibility = View.VISIBLE
            reviewRecyclerView.adapter = ReviewRecyclerAdapter(this, list, null)
        }
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

    private fun moveToCreateReview() {
        travelDetail?.let {
            val intent = Intent(this, CreateReviewActivity::class.java)
            intent.putExtra("placeType", "tour")
            intent.putExtra("placeName", titleTextView.text.toString())
            intent.putExtra("travelDetail", it)
            startActivity(intent)
        }
    }

    private fun moveToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun onCreateReview(view: View) {
        if (AuthApiClient.instance.hasToken()) {
            moveToCreateReview()
        } else {
            moveToLogin()
        }
    }
}
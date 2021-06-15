package com.ssac.place.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import com.ssac.place.models.PlaceReview
import com.ssac.place.models.TravelDetail
import com.ssac.place.models.TravelRecommend
import com.ssac.place.networks.FetchPlaceReviewListResponse
import com.ssac.place.networks.MyApis
import com.ssac.place.repository.LocalRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TravelDetailActivity : AppCompatActivity() {
    private val contentId: String by lazy { intent.getStringExtra("contentId") as String }
    private val latitude: String by lazy { intent.getStringExtra("latitude") as String }
    private val longitude: String by lazy { intent.getStringExtra("longitude") as String }

    private val imageView: ImageView by lazy { findViewById(R.id.imageView) }
    private val titleTextView: TextView by lazy { findViewById(R.id.titleTextView) }
    private val likeButton: ImageButton by lazy { findViewById(R.id.likeButton) }
    private val addressTextView: TextView by lazy { findViewById(R.id.addressTextView) }
    private val homepageTextView: TextView by lazy { findViewById(R.id.homepageTextView) }
    private val telTextView: TextView by lazy { findViewById(R.id.telTextView) }
    private val overviewTextView: TextView by lazy { findViewById(R.id.overviewTextView) }
    private val createReviewButton: ImageButton by lazy { findViewById(R.id.createReviewButton) }
    private val reviewRecyclerView: RecyclerView by lazy { findViewById(R.id.reviewRecyclerView) }
    private val noReviewTextView: TextView by lazy { findViewById(R.id.noReviewTextView) }
    private val recommendationTextView: TextView by lazy { findViewById(R.id.recommendationTextView) }
    private val recommendationTypeRecyclerView: RecyclerView by lazy { findViewById(R.id.typeRecyclerView) }
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
        MyApis.getInstance().fetchPlaceReviewList(contentId.toInt(), "tour").enqueue(object : Callback<FetchPlaceReviewListResponse> {
            override fun onResponse(call: Call<FetchPlaceReviewListResponse>, response: Response<FetchPlaceReviewListResponse>) {
                if (response.isSuccessful) {
                    setReviewList(response.body()?.reviews)
                }
            }

            override fun onFailure(call: Call<FetchPlaceReviewListResponse>, t: Throwable) {
                Log.d("AAA", t.localizedMessage)
                setReviewList(null)
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

    private fun createLike(token: String) {
        likeButton.isEnabled = false
        travelDetail?.contentid?.toIntOrNull()?.let {
            MyApis.getInstance().createTourLike(token, it).enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {

                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    likeButton.isEnabled = true
                }
            })
        }
    }

    private fun setTravelDetail(detail: TravelDetail) {
        travelDetail = detail
        Glide.with(this).load(detail.firstimage).into(imageView)
        titleTextView.text = detail.title
        addressTextView.text = detail.address()
        homepageTextView.text = HtmlCompat.fromHtml(detail.homepage(), FROM_HTML_MODE_LEGACY)
        homepageTextView.movementMethod = LinkMovementMethod.getInstance()
        telTextView.text = detail.tel()
        detail.overview?.let {
            overviewTextView.text = HtmlCompat.fromHtml(it, FROM_HTML_MODE_COMPACT)
        }
    }

    private fun setReviewList(list: List<PlaceReview>?) {
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
        recommendationTypeRecyclerView.adapter = RecommendTypeRecyclerAdapter(this, recommendList) {
            val typeId = it.tag as String
            changeRecommendType(typeId)
        }
        recommendationRecyclerView.adapter = RecommendRecyclerAdapter(this, recommendList) {
            moveToTravelDetail(it.tag as? String)
        }
    }

    private fun changeRecommendType(type: String) {
        val list = if (type == RecommendTypeRecyclerAdapter.TYPE_ALL) {
            recommendList
        } else {
            recommendList.filter { it.contentTypeId == type }.toList()
        }
        recommendationRecyclerView.adapter = RecommendRecyclerAdapter(this, list) {
            moveToTravelDetail(it.tag as? String)
        }
    }

    private fun showLoginAlert() {
        AlertDialog.Builder(this, R.style.LoginAlertDialog)
                .setTitle("로그인이 필요한 기능입니다.")
                .setMessage("로그인하시겠습니까?")
                .setPositiveButton("로그인") { dialog, i ->
                    moveToLogin()
                }
                .setNegativeButton("취소") { dialog, i ->

                }
                .show()
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

    fun onBack(view: View) {
        finish()
    }

    fun onLike(view: View) {
        val token = LocalRepository.instance.getMyToken(this)
        if (token!=null) {
            createLike(token)
        } else {
            showLoginAlert()
        }
    }

    fun onCreateReview(view: View) {
        if (LocalRepository.instance.getMyToken(this)!=null) {
            moveToCreateReview()
        } else {
            showLoginAlert()
        }
    }
}
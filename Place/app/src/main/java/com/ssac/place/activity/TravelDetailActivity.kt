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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.TokenManagerProvider
import com.ssac.place.R
import com.ssac.place.TravelApis
import com.ssac.place.TravelDetailResponse
import com.ssac.place.TravelSearchResponse
import com.ssac.place.models.PlaceReview
import com.ssac.place.models.TravelDetail
import com.ssac.place.models.TravelRecommend
import com.ssac.place.networks.CreateTourLikeResponse
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

    private val shadowLayout: ConstraintLayout by lazy { findViewById(R.id.shadowLayout) }
    private val scrollView: NestedScrollView by lazy { findViewById(R.id.scrollView) }
    private val imageView: ImageView by lazy { findViewById(R.id.imageView) }
    private val titleTextView: TextView by lazy { findViewById(R.id.titleTextView) }
    private val likeButton: ImageButton by lazy { findViewById(R.id.likeButton) }
    private val addressTextView: TextView by lazy { findViewById(R.id.addressTextView) }
    private val homepageTextView: TextView by lazy { findViewById(R.id.homepageTextView) }
    private val telTextView: TextView by lazy { findViewById(R.id.telTextView) }
    private val overviewTextView: TextView by lazy { findViewById(R.id.overviewTextView) }
    private val gradeTextView: TextView by lazy { findViewById(R.id.gradeTextView) }
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

        initLayout()
        requestTravelDetail()
        requestReviewList()
        requestRecommendation()
        refreshLikeButton()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode== RESULT_OK) {
            if (requestCode==LOGIN_REQUEST_CODE) {
                reviewRecyclerView.adapter?.notifyDataSetChanged()
                recommendationRecyclerView.adapter?.notifyDataSetChanged()
            } else if (requestCode== CREATE_REVIEW_REQUEST_CODE) {
                requestReviewList()
            }
        }
    }

    private fun initLayout() {
        scrollView.setOnScrollChangeListener{ v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY==0) {
                shadowLayout.visibility = View.GONE
            } else {
                shadowLayout.visibility = View.VISIBLE
            }
        }
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
                    gradeTextView.text = response.body()?.grade
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
                    val items = response.body()?.body?.items?.filter { it.contentid != contentId }
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

    private fun createLike(type: String, token: String) {
        likeButton.isEnabled = false
        travelDetail?.let { travelDetail ->
            MyApis.getInstance().createTourLike(
                    type +" " + token,
                    "tour",
                    travelDetail.contentid.toInt(),
                    travelDetail.addr1 ?: "",
                    travelDetail.addr2 ?: "",
                    travelDetail.areacode ?: "",
                    travelDetail.cat1 ?: "",
                    travelDetail.cat2 ?: "",
                    travelDetail.cat3 ?: "",
                    travelDetail.contentTypeId ?: "",
                    travelDetail.createdtime,
                    travelDetail.firstimage ?: "",
                    travelDetail.firstimage2 ?: "",
                    travelDetail.mapx ?: "",
                    travelDetail.mapy ?: "",
                    travelDetail.modifiedtime,
                    travelDetail.sigungucode ?: "",
                    travelDetail.tel ?: "",
                    travelDetail.title,
                    travelDetail.overview ?: "",
                    travelDetail.zipcode ?: "",
                    travelDetail.homepage ?: "",).enqueue(object : Callback<CreateTourLikeResponse> {
                override fun onResponse(call: Call<CreateTourLikeResponse>, response: Response<CreateTourLikeResponse>) {
                    likeButton.isEnabled = true
                    if (response.isSuccessful) {
                        response.body()?.message?.let {
                            if (it) {
                                LocalRepository.instance.addLikeTour(contentId)
                            } else {
                                LocalRepository.instance.removeLikeTour(contentId)
                            }
                            LocalRepository.instance.setNeedUpdateLikeList()
                        }
                    }
                    refreshLikeButton()
                }

                override fun onFailure(call: Call<CreateTourLikeResponse>, t: Throwable) {
                    likeButton.isEnabled = true
                    Log.d("AAA", t.localizedMessage)
                }
            })
        }
    }

    private fun setTravelDetail(detail: TravelDetail) {
        travelDetail = detail
        Glide.with(this).load(detail.firstimage).placeholder(R.drawable.ic_no_image2).into(imageView)
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
            reviewRecyclerView.adapter = ReviewRecyclerAdapter(this, list) {
                val position = it.tag as Int
                val review = list[position]
                moveToCreateReviewForEdit(review.review_id, review.grade, review.text)
            }
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

    private fun refreshLikeButton() {
        if (LocalRepository.instance.isLikedTour(contentId)) {
            likeButton.setImageResource(R.drawable.ic_place_like_on)
        } else {
            likeButton.setImageResource(R.drawable.ic_place_like_off)
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
            startActivityForResult(intent, CREATE_REVIEW_REQUEST_CODE)
        }
    }

    private fun moveToCreateReviewForEdit(reviewId: String, rating: String, contents: String) {
        val intent = Intent(this, CreateReviewActivity::class.java)
        intent.putExtra("placeType", "tour")
        intent.putExtra("placeName", titleTextView.text.toString())
        intent.putExtra("reviewId", reviewId)
        intent.putExtra("grade", rating.toInt())
        intent.putExtra("contents", contents)
        startActivityForResult(intent, CREATE_REVIEW_REQUEST_CODE)
    }

    private fun moveToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, LOGIN_REQUEST_CODE)
    }

    fun onBack(view: View) {
        finish()
    }

    fun onLike(view: View) {
        val type = LocalRepository.instance.getMySocialType(this)
        val token = TokenManagerProvider.instance.manager.getToken()?.accessToken
        if (LocalRepository.instance.loggedIn(this) && !type.isNullOrEmpty() && !token.isNullOrEmpty()) {
            createLike(type, token)
        } else {
            showLoginAlert()
        }
    }

    fun onCreateReview(view: View) {
        if (LocalRepository.instance.loggedIn(this)) {
            moveToCreateReview()
        } else {
            showLoginAlert()
        }
    }

    companion object {
        private const val LOGIN_REQUEST_CODE = 6021
        private const val CREATE_REVIEW_REQUEST_CODE = 6022
    }
}
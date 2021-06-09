package com.ssac.place.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssac.place.R
import com.ssac.place.TravelApis
import com.ssac.place.TravelSearchResponse
import com.ssac.place.extensions.isInstalled
import com.ssac.place.models.KakaoDocument
import com.ssac.place.models.MyReview
import com.ssac.place.models.TravelRecommend
import com.ssac.place.networks.FetchReviewListResponse
import com.ssac.place.networks.MyApis
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchDetailActivity : AppCompatActivity() {
    private val document: KakaoDocument by lazy { intent.getSerializableExtra("document") as KakaoDocument }
    private val mapViewContainer: ViewGroup by lazy { findViewById(R.id.mapView) }
    private val titleTextView: TextView by lazy { findViewById(R.id.titleTextView) }
    private val addressTextView: TextView by lazy { findViewById(R.id.addressTextView) }
    private val reviewRecyclerView: RecyclerView by lazy { findViewById(R.id.reviewRecyclerView) }
    private val noReviewTextView: TextView by lazy { findViewById(R.id.noReviewTextView) }
    private val typeRecyclerView: RecyclerView by lazy { findViewById(R.id.typeRecyclerView) }
    private val recommendRecyclerView: RecyclerView by lazy { findViewById(R.id.recommendRecyclerView) }

    private var mapView: MapView? = null

    private var recommendList: List<TravelRecommend> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_detail)
        searchWithTravel()
        fetchReviewList()

        initDocument()
    }

    override fun onResume() {
        super.onResume()
        mapView = MapView(this).apply {
            mapViewContainer.addView(this)
            addPOIItem(document.toPOIItem(0))
            setMapCenterPoint(MapPoint.mapPointWithGeoCoord(document.y.toDouble(), document.x.toDouble()), false)
        }
    }

    override fun onPause() {
        mapViewContainer.removeAllViews()
        mapView = null
        super.onPause()
    }

    private fun initDocument() {
        titleTextView.text= document.place_name
        addressTextView.text = document.address()
    }

    private fun fetchReviewList() {
        MyApis.getInstance().fetchReviewList(document.id.toInt(),"kakao").enqueue(object : Callback<FetchReviewListResponse> {
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

    private fun searchWithTravel() {
        TravelApis.getInstance()
            .search(document.x.toDouble(), document.y.toDouble()).enqueue(object : Callback<TravelSearchResponse> {
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

    private fun setReviewList(list: List<MyReview>?) {
        if (list.isNullOrEmpty()) {
            reviewRecyclerView.visibility = View.GONE
            noReviewTextView.visibility = View.VISIBLE
        } else {
            reviewRecyclerView.visibility = View.VISIBLE
            noReviewTextView.visibility = View.GONE
            reviewRecyclerView.adapter = ReviewRecyclerAdapter(this, list, null)
        }
    }

    private fun setRecommendList(list: List<TravelRecommend>) {
        recommendList = list

        typeRecyclerView.adapter = RecommendTypeRecyclerAdapter(list) {
            val type = it.tag as String
            changeRecommendType(type)
        }
        recommendRecyclerView.adapter = RecommendRecyclerAdapter(this, list) {
            moveToTravelDetail(it.tag as? String)
        }
    }

    private fun changeRecommendType(type: String) {
        val list = recommendList.filter { it.contentTypeId == type }.toList()
        recommendRecyclerView.adapter = RecommendRecyclerAdapter(this, list) {
            moveToTravelDetail(it.tag as? String)
        }
    }

    fun onOpenPage(view: View) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(document.place_url))
        startActivity(intent)
    }

    fun routeTo(view: View) {
        if (isInstalled("net.daum.android.map")) {
            val url = "kakaomap://look?p=${document.y},${document.x}"
            URLUtil.isValidUrl(url)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("kakaomap://place?id=${document.id}"))
            startActivity(intent)
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

    fun onCreateReview(view: View) {
        val intent = Intent(this, CreateReviewActivity::class.java)
        intent.putExtra("placeType", "kakao")
        intent.putExtra("placeName", titleTextView.text.toString())
        intent.putExtra("kakaoDocument", document)
        startActivity(intent)
    }
}
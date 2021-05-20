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
import com.ssac.place.models.TravelRecommend
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
    private val typeRecyclerView: RecyclerView by lazy { findViewById(R.id.typeRecyclerView) }
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }

    private var mapView: MapView? = null

    private var recommendList: List<TravelRecommend> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_detail)
        searchWithTravel()

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

    private fun setRecommendList(list: List<TravelRecommend>) {
        recommendList = list

        typeRecyclerView.adapter = RecommendTypeRecyclerAdapter(list) {
            val type = it.tag as String
            changeRecommendType(type)
        }
        recyclerView.adapter = RecommendRecyclerAdapter(this, list) {
            moveToTravelDetail(it.tag as? String)
        }
    }

    private fun changeRecommendType(type: String) {
        val list = recommendList.filter { it.contentTypeId == type }.toList()
        recyclerView.adapter = RecommendRecyclerAdapter(this, list) {
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
}
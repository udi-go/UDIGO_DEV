package com.ssac.place.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.ssac.place.KakaoApis
import com.ssac.place.KakaoSearchResponse
import com.ssac.place.R
import com.ssac.place.models.KakaoDocument
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchResultActivity : AppCompatActivity(), MapView.MapViewEventListener, MapView.POIItemEventListener {
    private val result: String by lazy { intent.getStringExtra("result") as String }

    private val mapViewContainer: ViewGroup by lazy { findViewById(R.id.mapView) }
    private val recyclerView: RecyclerView by lazy  {
        findViewById<RecyclerView>(R.id.recommendRecyclerView).apply {
            val helper = PagerSnapHelper()
            helper.attachToRecyclerView(this)
            addOnScrollListener(recyclerViewScrollListener)
        }
    }
    private val recyclerViewScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val position = (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                documentList.getOrNull(position)?.let {
                    moveToMapCenterPoint(it.y.toDouble(), it.x.toDouble(), true)
                }
                mapView?.poiItems?.forEach {
                    if (it.tag == position) {
                        mapView?.selectPOIItem(it, true)
                    }
                }
            }
        }
    }

    private var mapView: MapView? = null
    private var lastCenterPoint: MapPoint? = null
    private var lastZoomLevel: Int = 3

    private var documentList: List<KakaoDocument> = listOf()
    private var selectedDocument: KakaoDocument? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)
    }

    override fun onResume() {
        super.onResume()

        mapView = MapView(this).apply {
            setMapViewEventListener(this@SearchResultActivity)
            setPOIItemEventListener(this@SearchResultActivity)
            mapViewContainer.addView(this)
        }
    }

    override fun onPause() {
        mapViewContainer.removeAllViews()
        mapView = null
        super.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                moveToCurrentLocation()
            } else {
                searchWithKakao()
            }
        }
    }

    override fun onPOIItemSelected(mapView: MapView?, item: MapPOIItem?) {
        item?.let { it ->
            val position = it.tag
            val document = documentList[position]
            selectedDocument = document
            recyclerView.scrollToPosition(position)
        }
    }

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
        return
    }

    override fun onMapViewInitialized(mapView: MapView?) {
        if (lastCenterPoint==null) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                moveToCurrentLocation()
            } else {
                requestPermission()
            }
        } else {
            mapView?.setMapCenterPoint(lastCenterPoint, false)
            resetDocumentList(documentList)
        }
    }

    override fun onMapViewCenterPointMoved(mapView: MapView?, mapPoint: MapPoint?) {
        lastCenterPoint = mapPoint
    }

    override fun onMapViewZoomLevelChanged(mapView: MapView?, zoomLevel: Int) {
        lastZoomLevel = zoomLevel
    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
        return
    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {
        return
    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {
        return
    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {
        return
    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {
        return
    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {
        return
    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
        return
    }

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?, p2: MapPOIItem.CalloutBalloonButtonType?) {
        return
    }

    private fun requestPermission() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE)
    }

    @SuppressLint("MissingPermission")
    private fun moveToCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location==null) {
                moveToMapCenterPoint()
                searchWithKakao()
            } else {
                moveToMapCenterPoint(location.latitude, location.longitude)
                searchWithKakao(location.latitude, location.longitude)
            }
        }
    }

    private fun moveToMapCenterPoint(latitude: Double = 37.51778532586552, longitude: Double = 126.8864141623943, animated: Boolean = false) {
        val point = MapPoint.mapPointWithGeoCoord(latitude, longitude)
        lastCenterPoint = point
        mapView?.setMapCenterPointAndZoomLevel(point, lastZoomLevel, animated)
    }


    private fun searchWithKakao(latitude: Double = 37.51778532586552, longitude: Double = 126.8864141623943) {
        KakaoApis.getInstance()
            .search(result, longitude.toString(), latitude.toString()).enqueue(object : Callback<KakaoSearchResponse> {
            override fun onResponse(
                call: Call<KakaoSearchResponse>,
                response: Response<KakaoSearchResponse>
            ) {
                if (response.isSuccessful) {
                    val documentList = response.body()?.documents
                    if (documentList.isNullOrEmpty()) {

                    } else {
                        resetDocumentList(documentList)
                    }
                }
            }

            override fun onFailure(call: Call<KakaoSearchResponse>, t: Throwable) {

            }
        })
    }

    private fun resetDocumentList(list: List<KakaoDocument>) {
        mapView?.removeAllPOIItems()
        documentList = list
        val poiItemList = mutableListOf<MapPOIItem>()
        for ((i, document) in documentList.iterator().withIndex()) {
            poiItemList.add(document.toPOIItem(i))
        }
        mapView?.addPOIItems(poiItemList.toTypedArray())
        mapView?.poiItems?.firstOrNull()?.let {
            mapView?.selectPOIItem(it, false)
            moveToMapCenterPoint(it.mapPoint.mapPointGeoCoord.latitude, it.mapPoint.mapPointGeoCoord.longitude)
        }
        recyclerView.adapter = SearchResultRecyclerAdapter(this, documentList) {
            val position = it.tag as Int
            selectedDocument = documentList[position]
            selectedDocument?.let {
                moveToSearchDetail(it)
            }
        }
    }

    private fun moveToSearchDetail(document: KakaoDocument) {
        val intent = Intent(this, SearchDetailActivity::class.java)
        intent.putExtra("document", document)
        startActivity(intent)
    }

    fun onBack(view: View) {
        finish()
    }

    companion object {
        private const val LOCATION_PERMISSION_CODE = 3021
    }
}
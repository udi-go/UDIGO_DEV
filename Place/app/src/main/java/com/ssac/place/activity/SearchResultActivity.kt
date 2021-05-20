package com.ssac.place.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
    private val infoLayout: ConstraintLayout by lazy { findViewById(R.id.infoLayout) }
    private val nameTextView: TextView by lazy { findViewById(R.id.nameTextView) }
    private val addressTextView: TextView by lazy { findViewById(R.id.addressTextView) }

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
            val document = documentList[it.tag]
            selectedDocument = document
            infoLayout.visibility = View.VISIBLE
            nameTextView.text = document.place_name
            addressTextView.text = document.address()
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

    private fun moveToMapCenterPoint(latitude: Double = 37.51778532586552, longitude: Double = 126.8864141623943) {
        val point = MapPoint.mapPointWithGeoCoord(latitude, longitude)
        lastCenterPoint = point
        mapView?.setMapCenterPointAndZoomLevel(point, lastZoomLevel, false)
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
        documentList = list
        val poiItemList = mutableListOf<MapPOIItem>()
        for ((i, document) in documentList.iterator().withIndex()) {
            poiItemList.add(document.toPOIItem(i))
        }
        mapView?.addPOIItems(poiItemList.toTypedArray())
    }

    fun moveToDetail(view: View) {
        selectedDocument?.let {
            val intent = Intent(this, SearchDetailActivity::class.java)
            intent.putExtra("document", it)
            startActivity(intent)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_CODE = 3021
    }
}
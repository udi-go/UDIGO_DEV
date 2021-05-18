package com.ssac.place

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.location.LocationServices
import com.ssac.place.models.KakaoDocument
import net.daum.android.map.MapViewEventListener
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchResultActivity : AppCompatActivity(), MapViewEventListener, MapView.POIItemEventListener {
    private val mapViewContainer: ViewGroup by lazy { findViewById(R.id.mapView) }
    private val infoLayout: ConstraintLayout by lazy { findViewById(R.id.infoLayout) }
    private val nameTextView: TextView by lazy { findViewById(R.id.nameTextView) }
    private val addressTextView: TextView by lazy { findViewById(R.id.addressTextView) }

    private var mapView: MapView? = null

    private var documentList: List<KakaoDocument> = listOf()
    private var selectedDocument: KakaoDocument? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)

    }

    override fun onResume() {
        super.onResume()

        mapView = MapView(this).apply {
            mapViewEventListener = this@SearchResultActivity
            setPOIItemEventListener(this@SearchResultActivity)
            mapViewContainer.addView(this)
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            moveToCurrentLocation()
        } else {
            requestPermission()
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

    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        p1?.let { item ->
            val document = documentList[item.tag]
            selectedDocument = document
            infoLayout.visibility = View.VISIBLE
            nameTextView.text = document.place_name
            addressTextView.text = document.address()
        }
    }

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
        return
    }

    override fun onLoadMapView() {
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
        mapView?.setZoomLevel(3, false)
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
        mapView?.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), false)
    }

    private fun searchWithKakao(latitude: Double = 37.51778532586552, longitude: Double = 126.8864141623943) {
        KakaoApis.getInstance().search("공원", longitude.toString(), latitude.toString()).enqueue(object : Callback<KakaoSearchResponse> {
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
        for ((i, document) in documentList.iterator().withIndex())
            poiItemList.add(document.toPOIItem(i))
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
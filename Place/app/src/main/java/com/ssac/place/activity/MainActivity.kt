package com.ssac.place.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.kakao.sdk.user.UserApiClient
import com.ssac.place.KakaoApis
import com.ssac.place.KakaoSearchResponse
import com.ssac.place.R
import com.ssac.place.custom.SelectPhotoDialog
import com.ssac.place.extensions.getRealPath
import com.ssac.place.extensions.loadBitmap
import com.ssac.place.models.KakaoDocument
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity(), MapView.MapViewEventListener, MapView.POIItemEventListener {
    private val loginButton: Button by lazy { findViewById(R.id.loginButton) }
    private val myPageButton: Button by lazy { findViewById(R.id.myPageButton) }
    private val photoButton: Button by lazy { findViewById(R.id.photoButton) }
    private val imageView: ImageView by lazy { findViewById(R.id.imageView) }
    private val removeButton: Button by lazy { findViewById(R.id.removeButton) }
    private val classifyButton: Button by lazy { findViewById(R.id.classifyButton) }
    private val classifyTextView: TextView by lazy { findViewById(R.id.classifyTextView) }

    private val resultLayout: ConstraintLayout by lazy { findViewById(R.id.resultLayout) }
    private val mapViewContainer: ViewGroup by lazy { findViewById(R.id.mapView) }
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }

    private var photoUri: Uri? = null

    private var mapView: MapView? = null
    private var lastCenterPoint: MapPoint = MapPoint.mapPointWithGeoCoord(37.51778532586552, 126.8864141623943)
    private var lastZoomLevel: Int = 4

    private var documentList: List<KakaoDocument> = listOf()
    private var selectedDocument: KakaoDocument? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkLoginState()
    }

    override fun onResume() {
        super.onResume()
        initMapView()
    }

    override fun onPause() {
        removeMapView()
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==RESULT_OK) {
            if (requestCode == LOGIN_REQUEST || requestCode == MY_PAGE_REQUEST) {
                checkLoginState()
            } else if (requestCode == TAKE_PHOTO_REQUEST) {
                setSelectedPhoto()
            } else if (requestCode == SELECT_PHOTO_REQUEST) {
                photoUri = data?.data
                setSelectedPhoto()
            } else if (requestCode == LOCATION_PERMISSION_REQUEST) {
                initLastCenterPointAndMove()
            }
        }
    }

    // MapView
    override fun onPOIItemSelected(mapView: MapView?, item: MapPOIItem?) {
        item?.let { it ->
            val document = documentList[it.tag]
            selectedDocument = document
        }
    }

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
        return
    }

    override fun onMapViewInitialized(mapView: MapView?) {
        if (documentList.isNullOrEmpty()) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                initLastCenterPointAndMove()
            } else {
                requestPermission()
            }
        } else {
            resetDocumentList(documentList)
        }
    }

    override fun onMapViewCenterPointMoved(mapView: MapView?, mapPoint: MapPoint?) {
        mapPoint?.let { lastCenterPoint = it }
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

    private fun checkLoginState() {
        UserApiClient.instance.me { user, error ->
            if (user == null) {
                loginButton.visibility = View.VISIBLE
                myPageButton.visibility = View.GONE
            } else {
                loginButton.visibility = View.GONE
                myPageButton.visibility = View.VISIBLE
            }
        }
    }

    private fun initMapView() {
        mapView = MapView(this).apply {
            setMapViewEventListener(this@MainActivity)
            setPOIItemEventListener(this@MainActivity)
            mapViewContainer.addView(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLastCenterPointAndMove() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnCompleteListener { location ->
            location.result?.let {
                lastCenterPoint = MapPoint.mapPointWithGeoCoord(it.latitude, it.longitude)
            }
            mapView?.setMapCenterPointAndZoomLevel(lastCenterPoint, lastZoomLevel, false)
        }
    }

    private fun removeMapView() {
        mapViewContainer.removeAllViews()
        mapView = null
    }

    private fun removeSelectedPhoto() {
        photoUri = null
        imageView.setImageBitmap(null)
        imageView.visibility = View.GONE
        removeButton.visibility = View.GONE
        photoButton.visibility = View.VISIBLE
    }

    private fun setSelectedPhoto() {
        photoUri?.loadBitmap(this)?.let {
            imageView.setImageBitmap(it)
            imageView.visibility = View.VISIBLE
            removeButton.visibility = View.VISIBLE
            photoButton.visibility = View.GONE
        }
    }

    private fun showClassifyResult(result: String) {
        removeButton.visibility = View.GONE
        classifyButton.visibility = View.GONE
        classifyTextView.visibility = View.VISIBLE
        classifyTextView.text = result
        resultLayout.visibility = View.VISIBLE
        searchWithKakao(result)
    }

    private fun getPhotoFile(): File {
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(UUID.randomUUID().toString(), ".jpg", storageDir)
    }

    private fun classify(path: String) {
        showClassifyResult("공원")
//        val intent = Intent(this@MainActivity, SearchResultActivity::class.java)
//        intent.putExtra("result", "공원")
//        startActivity(intent)
//        val requestFile = File(path).asRequestBody("multipart/form-data".toMediaType())
//        val requestBody = MultipartBody.Part.createFormData("image", "image.jpg", requestFile)
//        MyApis.getInstance().classify(requestBody).enqueue(object : Callback<MyClassifyResponse> {
//            override fun onResponse(call: Call<MyClassifyResponse>, response: Response<MyClassifyResponse>) {
//                if (response.isSuccessful) {
//                    val name = response.body()?.name
//                    if (!name.isNullOrEmpty()) {
//                        showClassifyResult(name)
//                        val intent = Intent(this@MainActivity, SearchResultActivity::class.java)
//                        intent.putExtra("result", name)
//                        startActivity(intent)
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<MyClassifyResponse>, t: Throwable) {
//                Log.d("AAA", t.localizedMessage)
//            }
//        })
    }

    private fun openCameraIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            val file = getPhotoFile()
            photoUri = Uri.fromFile(file)
            val photoUri: Uri = FileProvider.getUriForFile(
                this@MainActivity,
                "com.ssac.android.fileprovider",
                file
            )
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(this, TAKE_PHOTO_REQUEST)
        }
    }

    private fun openAlbumIntent() {
        Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            startActivityForResult(Intent.createChooser(this, "사진"), SELECT_PHOTO_REQUEST)
        }
    }

    private fun requestPermission() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
    }

    private fun searchWithKakao(result: String) {
        KakaoApis.getInstance()
                .search(result,
                        lastCenterPoint.mapPointGeoCoord.longitude.toString(),
                        lastCenterPoint.mapPointGeoCoord.latitude.toString()
                ).enqueue(object : Callback<KakaoSearchResponse> {
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
        recyclerView.adapter = MainRecyclerAdapter(this, documentList) {
            val position = it.tag as Int
            val selected = documentList[position]
            moveToDetail(selected)
        }
    }

    private fun moveToDetail(document: KakaoDocument) {
        val intent = Intent(this, SearchDetailActivity::class.java)
        intent.putExtra("document", document)
        startActivity(intent)
    }

    fun onLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, LOGIN_REQUEST)
    }

    fun onMyPage(view: View) {
        val intent = Intent(this, MyPageActivity::class.java)
        startActivityForResult(intent, MY_PAGE_REQUEST)
    }

    fun selectPhoto(view: View) {
        SelectPhotoDialog(this)
            .setOnCameraButton {
                openCameraIntent()
            }
            .setOnAlbumButton {
                openAlbumIntent()
            }
            .show()
    }

    fun removePhoto(view: View) {
        removeSelectedPhoto()
    }

    fun classify(view: View) {
        photoUri?.getRealPath(this)?.let {
            classify(it)
        }
    }

    companion object {
        private const val LOGIN_REQUEST = 2081
        private const val MY_PAGE_REQUEST = 2082

        private const val TAKE_PHOTO_REQUEST = 2091
        private const val SELECT_PHOTO_REQUEST = 2092

        private const val LOCATION_PERMISSION_REQUEST = 2101
    }
}
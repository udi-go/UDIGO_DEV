package com.ssac.place.activity.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.kakao.sdk.user.UserApiClient
import com.ssac.place.R
import com.ssac.place.activity.LoginActivity
import com.ssac.place.activity.MyPageActivity
import com.ssac.place.activity.SearchDetailActivity
import com.ssac.place.activity.SearchResultActivity
import com.ssac.place.custom.SelectPhotoDialog
import com.ssac.place.extensions.asClassifyResult
import com.ssac.place.extensions.loadBitmap
import com.ssac.place.models.KakaoDocument
import com.ssac.place.networks.MyApis
import com.ssac.place.networks.MyClassifyResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {
    private val loginButton: Button by lazy { findViewById(R.id.loginButton) }
    private val myPageButton: Button by lazy { findViewById(R.id.myPageButton) }
    private val imageView: ImageView by lazy { findViewById(R.id.imageView) }
    private val selectImageTextView: TextView by lazy { findViewById(R.id.selectImageTextView) }
    private val removeButton: ImageButton by lazy { findViewById(R.id.removeButton) }
    private val classifyButton: Button by lazy { findViewById(R.id.classifyButton) }
    private val networkProgressBar: ProgressBar by lazy { findViewById(R.id.networkProgressBar) }
    private val classifyTextView: TextView by lazy { findViewById(R.id.classifyTextView) }
    private val searchButton: Button by lazy { findViewById(R.id.searchButton) }
    private val resetButton: Button by lazy { findViewById(R.id.resetButton) }

    private var photoUri: Uri? = null
    private var searchResult: String? = null
    private var searchResultSentence: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkLoginState()
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
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode== TAKE_PHOTO_REQUEST) {
                photoUri = null
            }
        }
    }

    override fun onBackPressed() {
        if (photoUri == null) {
            super.onBackPressed()
        } else {
            resetAllUI()
        }
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

    private fun removeSelectedPhoto() {
        photoUri = null
        imageView.setImageBitmap(null)
        removeButton.visibility = View.GONE
        selectImageTextView.visibility = View.VISIBLE
        classifyButton.text = "사진 선택"
        resetButton.visibility = View.GONE
    }

    private fun setSelectedPhoto() {
        photoUri?.loadBitmap(this)?.let {
            imageView.setImageBitmap(it)
            removeButton.visibility = View.VISIBLE
            selectImageTextView.visibility = View.GONE
            classifyButton.text = "장소 검색"
            resetButton.visibility = View.VISIBLE
        }
    }

    private fun showClassifyResult() {
        val result = searchResult
        val sentence = searchResultSentence ?: ""
        networkProgressBar.visibility = View.GONE
        if (result.isNullOrEmpty()) {
            removeButton.visibility = View.VISIBLE
            classifyButton.visibility = View.VISIBLE
            classifyTextView.visibility = View.GONE
            searchButton.visibility = View.GONE
            resetButton.visibility = View.VISIBLE
        } else {
            removeButton.visibility = View.GONE
            classifyButton.visibility = View.GONE
            classifyTextView.visibility = View.VISIBLE
            classifyTextView.text = result.asClassifyResult(sentence, getColor(R.color.green_dark))
            searchButton.text = "내 주변 ${result} 찾기"
            searchButton.visibility = View.VISIBLE
            resetButton.visibility = View.VISIBLE
        }
    }

    private fun getPhotoFile(): File {
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(UUID.randomUUID().toString(), ".jpg", storageDir)
    }

    private fun classify(uri: Uri) {
//        searchResult = "공원"
//        searchResultSentence = "멋진 공원이네요!"
//        showClassifyResult()
//        return
        removeButton.visibility = View.GONE
        classifyButton.visibility = View.GONE
        resetButton.visibility = View.GONE
        networkProgressBar.visibility = View.VISIBLE
        contentResolver.openInputStream(uri)?.readBytes()?.toRequestBody("multipart/form-data".toMediaType())?.let { body ->
            val requestBody = MultipartBody.Part.createFormData("image", "image.jpg", body)
            MyApis.getInstance().classify(requestBody).enqueue(object : Callback<MyClassifyResponse> {
                override fun onResponse(call: Call<MyClassifyResponse>, response: Response<MyClassifyResponse>) {
                    networkProgressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        searchResult = response.body()?.name
                        searchResultSentence = response.body()?.sentence
                        showClassifyResult()
                    } else {
                        searchResult = null
                        searchResultSentence = null
                        showClassifyResult()
                    }
                }

                override fun onFailure(call: Call<MyClassifyResponse>, t: Throwable) {
                    Log.d("AAA", t.localizedMessage)
                    searchResult = null
                    searchResultSentence = null
                    showClassifyResult()
                }
            })
        }
    }

    private fun showSelectAlert() {
        SelectPhotoDialog(this)
                .setOnCameraButton {
                    openCameraIntent()
                }
                .setOnAlbumButton {
                    openAlbumIntent()
                }
                .show()
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
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            startActivityForResult(Intent.createChooser(this, "사진"), SELECT_PHOTO_REQUEST)
        }
    }

    private fun resetAllUI() {
        searchResult = null
        searchResultSentence = null
        showClassifyResult()
        removeSelectedPhoto()
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

    fun removePhoto(view: View) {
        removeSelectedPhoto()
    }

    fun classify(view: View) {
        val uri = photoUri
        if (uri == null) {
            showSelectAlert()
        } else {
            classify(uri)
        }
    }

    fun onSearch(view: View) {
        val result = searchResult
        if (result.isNullOrEmpty()) { return }
        val intent = Intent(this@MainActivity, SearchResultActivity::class.java)
        intent.putExtra("result", result)
        startActivity(intent)
    }

    fun onClearAll(view: View) {
        resetAllUI()
    }

    companion object {
        private const val LOGIN_REQUEST = 2081
        private const val MY_PAGE_REQUEST = 2082

        private const val TAKE_PHOTO_REQUEST = 2091
        private const val SELECT_PHOTO_REQUEST = 2092
    }
}
package com.ssac.place

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import com.ssac.place.networks.MyApis
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private val loginButton: Button by lazy { findViewById(R.id.loginButton) }
    private val myPageButton: Button by lazy { findViewById(R.id.myPageButton) }
    private val cameraButton: Button by lazy { findViewById(R.id.cameraButton) }
    private val photoButton: Button by lazy { findViewById(R.id.photoButton) }
    private val imageView: ImageView by lazy { findViewById(R.id.imageView) }
    private val removeButton: Button by lazy { findViewById(R.id.removeButton) }

    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        KakaoSdk.init(this, "4b5e6b1dbeb88b42d491d8a2ad61a44d")
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
        imageView.visibility = View.GONE
        removeButton.visibility = View.GONE
        cameraButton.visibility = View.VISIBLE
        photoButton.visibility = View.VISIBLE
    }

    private fun setSelectedPhoto() {
        loadFromUri(photoUri)?.let {
            imageView.setImageBitmap(it)
            imageView.visibility = View.VISIBLE
            removeButton.visibility = View.VISIBLE
            cameraButton.visibility = View.GONE
            photoButton.visibility = View.GONE
        }
    }

    private fun loadFromUri(photoUri: Uri?): Bitmap? {
        photoUri?.let {
            return try {
                if (Build.VERSION.SDK_INT > 27) {
                    val source: ImageDecoder.Source = ImageDecoder.createSource(this.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(this.contentResolver, it)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
        return null
    }

    private fun getPhotoFile(): File {
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(UUID.randomUUID().toString(), ".jpg", storageDir)
    }

    private fun classify(path: String) {
        val requestFile = File(path).asRequestBody("multipart/form-data".toMediaType())
        val requestBody = MultipartBody.Part.createFormData("image", "image.jpg", requestFile)
        MyApis.getInstance().classify(requestBody).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                val intent = Intent(this@MainActivity, SearchResultActivity::class.java)
                startActivity(intent)
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {

            }
        })
    }

    fun onLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, LOGIN_REQUEST)
    }

    fun onMyPage(view: View) {
        val intent = Intent(this, MyPageActivity::class.java)
        startActivityForResult(intent, MY_PAGE_REQUEST)
    }

    fun takePhoto(view: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            val file = getPhotoFile()
            photoUri = Uri.fromFile(file)
            val photoUri: Uri = FileProvider.getUriForFile(
                this@MainActivity,
                "com.ssac.android.fileprovider",
                file
            )
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        startActivityForResult(intent, TAKE_PHOTO_REQUEST)
    }

    fun selectPhoto(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(Intent.createChooser(intent, "사진"), SELECT_PHOTO_REQUEST)
    }

    fun removePhoto(view: View) {
        removeSelectedPhoto()
    }

    fun classify(view: View) {
        photoUri?.path?.let {
            classify(it)
        }
    }

    companion object {
        private const val LOGIN_REQUEST = 2081
        private const val MY_PAGE_REQUEST = 2082

        private const val TAKE_PHOTO_REQUEST = 2091
        private const val SELECT_PHOTO_REQUEST = 2092
    }
}
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
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
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

class MainActivity : FragmentActivity() {
    private val viewPager: ViewPager2 by lazy { findViewById(R.id.viewPager) }
    private val homeButton: ImageButton by lazy { findViewById(R.id.homeButton) }
    private val searchButton: ImageButton by lazy { findViewById(R.id.searchButton) }
    private val likeButton: ImageButton by lazy { findViewById(R.id.likeButton) }
    private val myButton: ImageButton by lazy { findViewById(R.id.myButton) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager.adapter = MainFragmentStateAdapter(this)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                homeButton.isSelected = position == 0
                searchButton.isSelected = position == 1
                likeButton.isSelected = position == 2
                myButton.isSelected = position == 3
            }
        })
    }

    fun onHome(view: View) {
        viewPager.currentItem = 0
    }

    fun onSearch(view: View) {
        viewPager.currentItem = 1
    }

    fun onLike(view: View) {
        viewPager.currentItem = 2
    }

    fun onMy(view: View) {
        viewPager.currentItem = 3
    }
}
package com.ssac.place.activity.main

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.kakao.sdk.auth.TokenManagerProvider
import com.ssac.place.R
import com.ssac.place.networks.FetchMyLikeListResponse
import com.ssac.place.networks.FetchMyReviewListResponse
import com.ssac.place.networks.MyApis
import com.ssac.place.repository.LocalRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        fetchUserInformation()
    }

    private fun fetchUserInformation() {
        val type = LocalRepository.instance.getMySocialType(this)
        val token = TokenManagerProvider.instance.manager.getToken()?.accessToken
        if (LocalRepository.instance.loggedIn(this) && !type.isNullOrEmpty() && !token.isNullOrEmpty()) {
            MyApis.getInstance().fetchMyLikeList(type + " " + token).enqueue(object : Callback<FetchMyLikeListResponse> {
                override fun onResponse(call: Call<FetchMyLikeListResponse>, response: Response<FetchMyLikeListResponse>) {
                    response.body()?.all?.map { LocalRepository.instance.addLikeTour(it.place_id) }
                }

                override fun onFailure(call: Call<FetchMyLikeListResponse>, t: Throwable) {

                }
            })
            MyApis.getInstance().fetchMyReviewList(type + " " + token).enqueue(object : Callback<FetchMyReviewListResponse> {
                override fun onResponse(call: Call<FetchMyReviewListResponse>, response: Response<FetchMyReviewListResponse>) {
                    response.body()?.reviews?.map { LocalRepository.instance.addMyReview(it.review_id) }
                }

                override fun onFailure(call: Call<FetchMyReviewListResponse>, t: Throwable) {

                }
            })
        }
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
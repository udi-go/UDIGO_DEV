package com.ssac.place.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.common.KakaoSdk
import com.ssac.place.R
import com.ssac.place.models.KakaoDocument
import com.ssac.place.models.TravelDetail
import com.ssac.place.networks.MyApis
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Field

class CreateReviewActivity : AppCompatActivity() {
    private val nameTextView: TextView by lazy { findViewById(R.id.nameTextView) }
    private val doneButton: Button by lazy { findViewById(R.id.doneButton) }
    private val ratingBar: RatingBar by lazy { findViewById(R.id.ratingBar) }
    private val reviewEditText: EditText by lazy { findViewById(R.id.reviewEditText) }

    private val placeType: String by lazy { intent.getStringExtra("placeType") as String }
    private val placeName: String by lazy { intent.getStringExtra("placeName") as String }
    private val kakaoDocument: KakaoDocument? by lazy { intent.getSerializableExtra("kakaoDocument") as? KakaoDocument }
    private val travelDetail: TravelDetail? by lazy { intent.getSerializableExtra("travelDetail") as? TravelDetail }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_review)

        KakaoSdk.init(this, "4b5e6b1dbeb88b42d491d8a2ad61a44d")
        initLayout()
    }

    private fun initLayout() {
        nameTextView.text = placeName
        ratingBar.rating = 5f
    }

    private fun createReview(token: String, rating: Int, contents: String) {
        val document = kakaoDocument
        val travelDetail = travelDetail
        if (placeType=="kakao" && document!=null) {
            MyApis.getInstance().createKakaoReview(
                    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoyfQ.NiBmZlBggpMOfgR5JTvl6no_T5ttjXZz_oeXcziFDDA",
                    placeType,
                    document.id.toInt(),
                    document.place_name,
                    document.place_url,
                    document.category_name,
                    document.address_name,
                    document.road_address_name,
                    document.phone,
                    document.category_group_code,
                    document.category_group_name,
                    document.x,
                    document.y,
                    rating,
                    contents
            ).enqueue(
                    object : Callback<Unit> {
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                            setResult(RESULT_OK)
                            finish()
                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            Toast.makeText(this@CreateReviewActivity, "네트워크를 확인하세요", Toast.LENGTH_SHORT).show()
                        }
                    })
        } else if (placeType=="tour" && travelDetail!=null){
            MyApis.getInstance().createTourReview("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoyfQ.NiBmZlBggpMOfgR5JTvl6no_T5ttjXZz_oeXcziFDDA",
                    placeType,
                    travelDetail.contentid.toInt(),
                    travelDetail.addr1 ?: "",
                    travelDetail.addr2 ?: "",
                    travelDetail.areacode ?: "",
                    travelDetail.cat1 ?: "",
                    travelDetail.cat2 ?: "",
                    travelDetail.cat3 ?: "",
                    travelDetail.contentTypeId ?: "",
                    travelDetail.createdtime,
                    travelDetail.firstimage ?: "",
                    travelDetail.firstimage2 ?: "",
                    travelDetail.mapx ?: "",
                    travelDetail.mapy ?: "",
                    travelDetail.modifiedtime,
                    travelDetail.sigungucode ?: "",
                    travelDetail.tel ?: "",
                    travelDetail.title,
                    travelDetail.overview ?: "",
                    travelDetail.zipcode ?: "",
                    travelDetail.homepage ?: "",
                    rating,
                    contents
            ).enqueue(object: Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    setResult(RESULT_OK)
                    finish()
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Toast.makeText(this@CreateReviewActivity, "네트워크를 확인하세요", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun onDone(view: View) {
        val rate = ratingBar.rating.toInt()
        val review = reviewEditText.text.toString().trim()
        if (review.isEmpty()) {
            Toast.makeText(this, "내용을 입력하세요", Toast.LENGTH_SHORT).show()
        } else {
            val token = AuthApiClient.instance.tokenManagerProvider.manager.getToken()
            if (token == null) {
                Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            } else {
                createReview(token.accessToken, rate, review)
            }
        }
    }
}
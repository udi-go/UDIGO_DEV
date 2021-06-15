package com.ssac.place.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.ssac.place.R
import com.ssac.place.models.KakaoDocument
import com.ssac.place.models.TravelDetail
import com.ssac.place.networks.MyApis
import com.ssac.place.repository.LocalRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateReviewActivity : AppCompatActivity() {
    private val nameTextView: TextView by lazy { findViewById(R.id.nameTextView) }
    private val ratingButton1: ImageButton by lazy { findViewById(R.id.ratingButton1) }
    private val ratingButton2: ImageButton by lazy { findViewById(R.id.ratingButton2) }
    private val ratingButton3: ImageButton by lazy { findViewById(R.id.ratingButton3) }
    private val ratingButton4: ImageButton by lazy { findViewById(R.id.ratingButton4) }
    private val ratingButton5: ImageButton by lazy { findViewById(R.id.ratingButton5) }
    private val reviewEditText: EditText by lazy { findViewById(R.id.reviewEditText) }

    private val placeType: String by lazy { intent.getStringExtra("placeType") as String }
    private val placeName: String by lazy { intent.getStringExtra("placeName") as String }
    private val kakaoDocument: KakaoDocument? by lazy { intent.getSerializableExtra("kakaoDocument") as? KakaoDocument }
    private val travelDetail: TravelDetail? by lazy { intent.getSerializableExtra("travelDetail") as? TravelDetail }

    private var rate: Int = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_review)

        initLayout()
    }

    private fun initLayout() {
        nameTextView.text = placeName
    }

    private fun setRating(rating: Int) {
        rate = rating
        if (rate > 1) {
            ratingButton2.setImageResource(R.drawable.ic_star_on)
        } else {
            ratingButton2.setImageResource(R.drawable.ic_star_off)
        }
        if (rate > 2) {
            ratingButton3.setImageResource(R.drawable.ic_star_on)
        } else {
            ratingButton3.setImageResource(R.drawable.ic_star_off)
        }
        if (rate > 3) {
            ratingButton4.setImageResource(R.drawable.ic_star_on)
        } else {
            ratingButton4.setImageResource(R.drawable.ic_star_off)
        }
        if (rate > 4) {
            ratingButton5.setImageResource(R.drawable.ic_star_on)
        } else {
            ratingButton5.setImageResource(R.drawable.ic_star_off)
        }
    }

    private fun createReview(token: String, rating: Int, contents: String) {
        val document = kakaoDocument
        val travelDetail = travelDetail
        if (placeType=="kakao" && document!=null) {
            MyApis.getInstance().createKakaoReview(
                    token,
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
            MyApis.getInstance().createTourReview(
                    token,
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

    fun onBack(view: View) {
        finish()
    }

    fun onRate(view: View) {
        when(view.id) {
            R.id.ratingButton1 -> setRating(1)
            R.id.ratingButton2 -> setRating(2)
            R.id.ratingButton3 -> setRating(3)
            R.id.ratingButton4 -> setRating(4)
            R.id.ratingButton5 -> setRating(5)
        }
    }

    fun onDone(view: View) {
        val review = reviewEditText.text.toString().trim()
        if (review.isEmpty()) {
            Toast.makeText(this, "내용을 입력하세요", Toast.LENGTH_SHORT).show()
        } else {
            val token = LocalRepository.instance.getMyToken(this)
            if (token == null) {
                Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            } else {
                createReview(token, rate, review)
            }
        }
    }
}
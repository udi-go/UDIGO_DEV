package com.ssac.place

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import com.bumptech.glide.Glide
import com.ssac.place.models.TravelDetail
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TravelDetailActivity : AppCompatActivity() {
    private val contentId: String by lazy { intent.getStringExtra("contentId") as String }
    private val imageView: ImageView by lazy { findViewById(R.id.imageView) }
    private val titleTextView: TextView by lazy { findViewById(R.id.titleTextView) }
    private val homepageTextView: TextView by lazy { findViewById(R.id.homepageTextView) }
    private val overviewTextView: TextView by lazy { findViewById(R.id.overviewTextView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_detail)

        requestTravelDetail()
    }

    private fun requestTravelDetail() {
        TravelApis.getInstance().detail(contentId).enqueue(object : Callback<TravelDetailResponse> {
            override fun onResponse(call: Call<TravelDetailResponse>, response: Response<TravelDetailResponse>) {
                if (response.isSuccessful) {
                    response.body()?.body?.items?.firstOrNull()?.let {
                        setTravelDetail(it)
                    }
                }
            }

            override fun onFailure(call: Call<TravelDetailResponse>, t: Throwable) {
                Log.d("AAA", t.localizedMessage)
            }
        })
    }

    private fun setTravelDetail(detail: TravelDetail) {
        Glide.with(this).load(detail.firstimage).into(imageView)
        titleTextView.text = detail.title
        detail.homepage?.let {
            homepageTextView.text = HtmlCompat.fromHtml(it, FROM_HTML_MODE_LEGACY)
            homepageTextView.movementMethod = LinkMovementMethod.getInstance()
        }
        detail.overview?.let {
            overviewTextView.text = HtmlCompat.fromHtml(it, FROM_HTML_MODE_COMPACT)
        }
    }
}
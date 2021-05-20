package com.ssac.place.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.kakao.sdk.user.UserApiClient
import com.ssac.place.R

class MyPageActivity : AppCompatActivity() {
    private val idTextView: TextView by lazy { findViewById(R.id.idTextView) }
    private val profileImageView: ImageView by lazy { findViewById(R.id.profileImageView) }
    private val nickNameTextView: TextView by lazy { findViewById(R.id.nickNameTextView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        UserApiClient.instance.me { user, error ->
            if (user == null) {
                Toast.makeText(this, "로그인 정보가 없습니다", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                idTextView.text = user.id.toString()
                Glide.with(this).load(user.kakaoAccount?.profile?.thumbnailImageUrl).into(profileImageView)
                nickNameTextView.text = user.kakaoAccount?.profile?.nickname
            }
        }
    }

    fun onLogout(view: View) {
        UserApiClient.instance.logout {
            setResult(RESULT_OK)
            finish()
        }
    }
}
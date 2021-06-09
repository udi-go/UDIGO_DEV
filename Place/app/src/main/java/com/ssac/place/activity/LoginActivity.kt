package com.ssac.place.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.ssac.place.R
import com.ssac.place.networks.LoginResponse
import com.ssac.place.networks.MyApis
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Toast.makeText(this, "로그인에 실패했습니다", Toast.LENGTH_SHORT).show()
        } else if (token != null) {
            UserApiClient.instance.me { user, error ->
                if (user != null) {
                    requestLogin(token.accessToken, user.id.toString())
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    private fun requestLogin(token: String, userId: String) {
        MyApis.getInstance().loginWithKaKao(token, userId).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d("AAA", it.access_token)
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                logout()
            }
        })
    }

    private fun logout() {
        UserApiClient.instance.logout {
            Toast.makeText(this, "로그인에 실패했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    fun kakaoLogin(view: View) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }
}
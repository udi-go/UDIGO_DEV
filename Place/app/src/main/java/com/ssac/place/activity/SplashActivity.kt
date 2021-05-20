package com.ssac.place.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.kakao.sdk.common.KakaoSdk
import com.ssac.place.R
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        KakaoSdk.init(this, "4b5e6b1dbeb88b42d491d8a2ad61a44d")

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            moveToMainActivity()
        }
    }

    private fun moveToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
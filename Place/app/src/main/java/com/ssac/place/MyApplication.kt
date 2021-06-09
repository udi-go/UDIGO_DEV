package com.ssac.place

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, "4b5e6b1dbeb88b42d491d8a2ad61a44d")
    }
}
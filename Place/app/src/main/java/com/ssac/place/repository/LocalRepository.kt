package com.ssac.place.repository

import android.content.Context

class LocalRepository {
    fun setMyId(context: Context, id: Int) {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putInt(MY_ID, id).apply()
    }

    fun getMyId(context: Context): Int {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).getInt(MY_ID, -1)
    }

    fun setMyNickname(context: Context, nickname: String?) {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putString(MY_NICKNAME, nickname).apply()
    }

    fun getMyNickname(context: Context): String? {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).getString(MY_NICKNAME,  null)
    }

    fun setMySocialType(context: Context, socialType: String?) {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putString(MY_SOCIAL_TYPE, socialType).apply()
    }

    fun getMySocialType(context: Context): String? {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).getString(MY_SOCIAL_TYPE,  null)
    }

    fun loggedIn(context: Context): Boolean {
        val id = getMyId(context)
        return id > -1
    }

    fun logout(context: Context) {
        setMyId(context, -1)
        setMyNickname(context, null)
        setMySocialType(context, null)
        setNeedUpdateReviewList()
        setNeedUpdateLikeList()
        likeTourPlaceIdList.clear()
    }

    fun setNeedUpdateLikeList() {
        needUpdateLikeList = true
    }

    fun needUpdateLikeList(): Boolean {
        return needUpdateLikeList
    }

    fun setNeedUpdateReviewList() {
        needUpdateReviewList = true
    }

    fun needUpdateReviewList(): Boolean {
        return needUpdateReviewList
    }

    fun addLikeTour(placeId: String) {
        likeTourPlaceIdList.add(placeId)
    }

    fun removeLikeTour(placeId: String) {
        likeTourPlaceIdList.remove(placeId)
    }

    fun isLikedTour(placeId: String): Boolean {
        return likeTourPlaceIdList.contains(placeId)
    }

    companion object {
        private const val PREFERENCE_NAME = "place_preference"
        private const val MY_ID = "my_id"
        private const val MY_NICKNAME = "my_nickname"
        private const val MY_SOCIAL_TYPE = "my_social_type"

        val instance = LocalRepository()

        private var needUpdateLikeList: Boolean = false
        private var needUpdateReviewList: Boolean = false
        private var likeTourPlaceIdList: MutableSet<String> = mutableSetOf()
    }
}
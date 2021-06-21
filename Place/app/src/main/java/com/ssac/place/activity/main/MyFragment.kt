package com.ssac.place.activity.main

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.kakao.sdk.auth.TokenManagerProvider
import com.kakao.sdk.user.UserApiClient
import com.ssac.place.MyApplication
import com.ssac.place.R
import com.ssac.place.activity.CreateReviewActivity
import com.ssac.place.activity.LoginActivity
import com.ssac.place.activity.SearchDetailActivity
import com.ssac.place.activity.TravelDetailActivity
import com.ssac.place.models.KakaoDocument
import com.ssac.place.models.MyReview
import com.ssac.place.networks.FetchMyReviewListResponse
import com.ssac.place.networks.MyApis
import com.ssac.place.repository.LocalRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyFragment : Fragment() {

    companion object {
        fun newInstance() = MyFragment()
    }

    private lateinit var viewModel: MyFragmentViewModel

    private lateinit var loginButton: Button
    private lateinit var userLayout: NestedScrollView
    private lateinit var userNameTextView: TextView
    private lateinit var logoutButton : Button
    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var noReviewTextView: TextView
    private lateinit var shadowLayout: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.my_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MyFragmentViewModel::class.java)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginButton = view.findViewById(R.id.loginButton)
        userLayout = view.findViewById(R.id.userLayout)
        userNameTextView = view.findViewById(R.id.userNameTextView)
        logoutButton = view.findViewById(R.id.logoutButton)
        reviewRecyclerView = view.findViewById(R.id.reviewRecyclerView)
        noReviewTextView = view.findViewById(R.id.noReviewTextView)
        shadowLayout = view.findViewById(R.id.shadowLayout)
        userLayout.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY==0) {
                shadowLayout.visibility = View.GONE
            } else {
                shadowLayout.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val type = LocalRepository.instance.getMySocialType(requireContext())
        val token = TokenManagerProvider.instance.manager.getToken()?.accessToken
        if (LocalRepository.instance.loggedIn(requireContext()) && !type.isNullOrEmpty() && !token.isNullOrEmpty()) {
            loginButton.visibility = View.GONE
            userLayout.visibility = View.VISIBLE
            userNameTextView.text = LocalRepository.instance.getMyNickname(requireContext()) + " 님"
            logoutButton.setOnClickListener {
                logout()
            }
            showMyReviewList(type, token)
        } else {
            viewModel.myReviewList = null
            loginButton.visibility = View.VISIBLE
            userLayout.visibility = View.GONE
            userNameTextView.text = null
            shadowLayout.visibility = View.GONE
            loginButton.setOnClickListener {
                moveToLogin()
            }
        }
    }

    private fun showMyReviewList(type: String, token: String) {
        val reviewList = viewModel.myReviewList
        if (reviewList==null || LocalRepository.instance.needUpdateReviewList()) {
            fetchMyReviewList(type, token)
        }
    }

    private fun refreshReviewRecyclerView(reviewList: List<MyReview>) {
        if (reviewList.isEmpty()) {
            noReviewTextView.visibility = View.VISIBLE
            reviewRecyclerView.visibility = View.GONE
        } else {
            noReviewTextView.visibility = View.GONE
            reviewRecyclerView.visibility = View.VISIBLE
            reviewRecyclerView.adapter = MyReviewRecyclerViewAdapter(requireContext(), reviewList, {
                val position = it.tag as Int
                val reviewList = viewModel.myReviewList
                if (!reviewList.isNullOrEmpty()) {
                    val review = reviewList[position]
                    if (review.type == "tour") {
                        moveToTravelDetail(review.place_id, review.mapy, review.mapx)
                    } else if (review.type == "kakao") {
                         moveToSearchDetail(review.getKakaoDocument())
                    }
                }
            }, {
                val position = it.tag as Int
                viewModel.myReviewList?.getOrNull(position)?.let {
                    moveToCreateReview(it)
                }
            })
        }
    }

    private fun fetchMyReviewList(type: String, token: String) {
        MyApis.getInstance().fetchMyReviewList(type+ " " + token).enqueue(object : Callback<FetchMyReviewListResponse> {
            override fun onResponse(call: Call<FetchMyReviewListResponse>, response: Response<FetchMyReviewListResponse>) {
                if (response.isSuccessful) {
                    response.body()?.reviews?.let {
                        viewModel.myReviewList = it
                        refreshReviewRecyclerView(it)
                    }
                }
            }

            override fun onFailure(call: Call<FetchMyReviewListResponse>, t: Throwable) {
                Log.d("AAA", t.localizedMessage)
            }
        })
    }

    private fun moveToTravelDetail(placeId: String, latitude: String, longitude: String) {
        val intent = Intent(requireContext(), TravelDetailActivity::class.java)
        intent.putExtra("contentId", placeId)
        intent.putExtra("latitude", latitude)
        intent.putExtra("longitude", longitude)
        startActivity(intent)
    }

    private fun moveToCreateReview(review: MyReview) {
        val intent = Intent(requireContext(), CreateReviewActivity::class.java)
        intent.putExtra("placeType", review.type)
        intent.putExtra("placeName", review.place_title)
        intent.putExtra("reviewId", review.review_id)
        intent.putExtra("contents", review.text)
        intent.putExtra("grade", review.grade)
        startActivity(intent)
    }

    private fun moveToSearchDetail(document: KakaoDocument) {
        val intent = Intent(requireContext(), SearchDetailActivity::class.java)
        intent.putExtra("document", document)
        startActivity(intent)
    }

    private fun moveToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    private fun logout() {
        UserApiClient.instance.logout {
            LocalRepository.instance.logout(requireContext())
            onResume()
        }
    }
}
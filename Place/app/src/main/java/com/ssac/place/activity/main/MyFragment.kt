package com.ssac.place.activity.main

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.ssac.place.MyApplication
import com.ssac.place.R
import com.ssac.place.activity.LoginActivity
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
    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var historyRecyclerView: RecyclerView

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
        reviewRecyclerView = view.findViewById(R.id.reviewRecyclerView)
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView)
    }

    override fun onResume() {
        super.onResume()

        val token = LocalRepository.instance.getMyToken(requireContext())
        if (token.isNullOrEmpty()) {
            loginButton.visibility = View.VISIBLE
            userLayout.visibility = View.GONE
            loginButton.setOnClickListener {
                moveToLogin()
            }
        } else {
            loginButton.visibility = View.GONE
            userLayout.visibility = View.VISIBLE
            showMyReviewList(token)
        }
    }

    private fun showMyReviewList(token: String) {
        val reviewList = viewModel.myReviewList
        if (reviewList.isNullOrEmpty()) {
            fetchMyReviewList(token)
        } else {
            refreshReviewRecyclerView(reviewList)
        }
    }

    private fun refreshReviewRecyclerView(reviewList: List<MyReview>) {
        reviewRecyclerView.adapter = MyReviewRecyclerViewAdapter(requireContext(), reviewList) {

        }
    }

    private fun fetchMyReviewList(token: String) {
        MyApis.getInstance().fetchMyReviewList(token).enqueue(object : Callback<FetchMyReviewListResponse> {
            override fun onResponse(call: Call<FetchMyReviewListResponse>, response: Response<FetchMyReviewListResponse>) {
                if (response.isSuccessful) {
                    response.body()?.reviews?.let {
                        viewModel.myReviewList = it
                        refreshReviewRecyclerView(it)
                    }
                }
            }

            override fun onFailure(call: Call<FetchMyReviewListResponse>, t: Throwable) {

            }
        })
    }

    private fun moveToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

}
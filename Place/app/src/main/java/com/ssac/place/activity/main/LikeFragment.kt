package com.ssac.place.activity.main

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.ssac.place.MyApplication
import com.ssac.place.R
import com.ssac.place.activity.LoginActivity
import com.ssac.place.models.MyLike
import com.ssac.place.networks.FetchMyLikeListResponse
import com.ssac.place.networks.MyApis
import com.ssac.place.repository.LocalRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LikeFragment : Fragment() {

    companion object {
        fun newInstance() = LikeFragment()
    }

    private lateinit var viewModel: LikeFragmentViewModel

    private lateinit var loginButton: Button
    private lateinit var typeRecyclerView: RecyclerView
    private lateinit var likeRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.like_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LikeFragmentViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginButton = view.findViewById(R.id.loginButton)
        typeRecyclerView = view.findViewById(R.id.typeRecyclerView)
        likeRecyclerView = view.findViewById(R.id.likeRecyclerView)
    }

    override fun onResume() {
        super.onResume()

        val token = LocalRepository.instance.getMyToken(requireContext())
        if (token==null) {
            loginButton.visibility = View.VISIBLE
            typeRecyclerView.visibility = View.GONE
            likeRecyclerView.visibility = View.GONE
            loginButton.setOnClickListener {
                moveToLogin()
            }
        } else {
            loginButton.visibility = View.GONE
            typeRecyclerView.visibility = View.VISIBLE
            likeRecyclerView.visibility = View.VISIBLE
            showMyLikeList(token)
        }
    }

    private fun showMyLikeList(token: String) {
        val likeList = viewModel.myLikeList
        if (likeList.isNullOrEmpty()) {
            fetchMyLikeList(token)
        } else {
            refreshLikeRecyclerView(likeList)
        }
    }

    private fun fetchMyLikeList(token: String) {
        MyApis.getInstance().fetchMyLikeList(token).enqueue(object : Callback<FetchMyLikeListResponse> {
            override fun onResponse(call: Call<FetchMyLikeListResponse>, response: Response<FetchMyLikeListResponse>) {
                if (response.isSuccessful) {
                    response.body()?.likes?.let {
                        viewModel.myLikeList = it
                        refreshLikeRecyclerView(it)
                    }
                }
            }

            override fun onFailure(call: Call<FetchMyLikeListResponse>, t: Throwable) {

            }
        })
    }

    private fun refreshLikeRecyclerView(list: List<MyLike>) {
        typeRecyclerView.adapter = LikeTypeRecyclerViewAdapter(requireContext(), listOf("관광지", "식당", "레포츠", "레포츠")) {
            val type = it.tag as String
        }
        likeRecyclerView.adapter = LikeRecyclerViewAdapter(requireContext(), listOf("1", "2", "3", "4", "5")) {

        }
    }

    private fun moveToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }
}
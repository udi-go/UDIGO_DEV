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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.kakao.sdk.auth.TokenManagerProvider
import com.ssac.place.R
import com.ssac.place.activity.LoginActivity
import com.ssac.place.activity.TravelDetailActivity
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
    private lateinit var noLikeTextView: TextView
    private lateinit var shadowLayout: ConstraintLayout

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
        noLikeTextView = view.findViewById(R.id.noLikeTextView)
        shadowLayout = view.findViewById(R.id.shadowLayout)
        likeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(-1)) {
                    shadowLayout.visibility = View.GONE
                } else {
                    shadowLayout.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        val type = LocalRepository.instance.getMySocialType(requireContext())
        val token = TokenManagerProvider.instance.manager.getToken()?.accessToken
        if (LocalRepository.instance.loggedIn(requireContext()) && !type.isNullOrEmpty() && !token.isNullOrEmpty()) {
            loginButton.visibility = View.GONE
            typeRecyclerView.visibility = View.VISIBLE
            likeRecyclerView.visibility = View.VISIBLE
            showMyLikeList(type, token)
        } else {
            loginButton.visibility = View.VISIBLE
            typeRecyclerView.visibility = View.GONE
            likeRecyclerView.visibility = View.GONE
            loginButton.setOnClickListener {
                moveToLogin()
            }
        }
    }

    private fun showMyLikeList(type: String, token: String) {
        val likeList = viewModel.myLikeList
        if (likeList==null || LocalRepository.instance.needUpdateLikeList()) {
            fetchMyLikeList(type, token)
        }
    }

    private fun fetchMyLikeList(type: String, token: String) {
        MyApis.getInstance().fetchMyLikeList(type + " " +token).enqueue(object : Callback<FetchMyLikeListResponse> {
            override fun onResponse(call: Call<FetchMyLikeListResponse>, response: Response<FetchMyLikeListResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        viewModel.myLikeList = body.all
                        viewModel.a12 = body.a12
                        viewModel.a14 = body.a14
                        viewModel.a15 = body.a15
                        viewModel.a28 = body.a28
                        viewModel.a32 = body.a32
                        viewModel.a38 = body.a38
                        viewModel.a39 = body.a39
                        refreshLikeRecyclerView(body.all)
                        body.all.map { LocalRepository.instance.addLikeTour(it.place_id) }
                    }
                }
            }

            override fun onFailure(call: Call<FetchMyLikeListResponse>, t: Throwable) {

            }
        })
    }

    private fun refreshLikeRecyclerView(list: List<MyLike>) {
        if (list.isEmpty()) {
            noLikeTextView.visibility = View.VISIBLE
            typeRecyclerView.visibility = View.GONE
            likeRecyclerView.visibility = View.GONE
        } else {
            noLikeTextView.visibility = View.GONE
            typeRecyclerView.visibility = View.VISIBLE
            likeRecyclerView.visibility = View.VISIBLE
            typeRecyclerView.adapter = LikeTypeRecyclerViewAdapter(requireContext(), listOf("전체", "관광지", "문화시설", "공연", "레포츠", "숙박", "쇼핑", "식당")) {
                val type = it.tag as String
                changeLikeType(type)
            }
            likeRecyclerView.adapter = LikeRecyclerViewAdapter(requireContext(), list) {
                val placeId = it.tag as String
                moveToTravelDetail(placeId)
            }
        }
    }

    private fun changeLikeType(type: String) {
        when(type) {
            "관광지" -> viewModel.a12
            "문화시설" -> viewModel.a14
            "공연" -> viewModel.a15
            "레포츠" -> viewModel.a28
            "숙박" -> viewModel.a32
            "쇼핑" -> viewModel.a38
            "식당" -> viewModel.a39
            else -> viewModel.myLikeList
        }?.let {
            likeRecyclerView.adapter = LikeRecyclerViewAdapter(requireContext(), it) {
                val placeId = it.tag as String
                moveToTravelDetail(placeId)
            }
        }
    }

    private fun moveToTravelDetail(placeId: String?) {
        viewModel.myLikeList?.firstOrNull{ it.place_id == placeId }?.let {
            val intent = Intent(requireContext(), TravelDetailActivity::class.java)
            intent.putExtra("contentId", it.place_id)
            intent.putExtra("latitude", it.mapy)
            intent.putExtra("longitude", it.mapx)
            startActivity(intent)
        }
    }

    private fun moveToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }
}
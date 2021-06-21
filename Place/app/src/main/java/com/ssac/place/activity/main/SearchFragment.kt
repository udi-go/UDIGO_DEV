package com.ssac.place.activity.main

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.DrawableCompat
import com.ssac.place.R
import com.ssac.place.activity.SearchResultActivity
import com.ssac.place.custom.SelectPhotoDialog
import com.ssac.place.extensions.asClassifyResult
import com.ssac.place.extensions.loadBitmap
import com.ssac.place.networks.MyApis
import com.ssac.place.networks.MyClassifyResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class SearchFragment : Fragment() {

    companion object {
        private const val TAKE_PHOTO_REQUEST = 2091
        private const val SELECT_PHOTO_REQUEST = 2092

        fun newInstance() = SearchFragment()
    }

    private lateinit var viewModel: SearchFragmentViewModel

    private lateinit var textView1: TextView
    private lateinit var textView2: TextView
    private lateinit var emptyImageLayout: ConstraintLayout
    private lateinit var selectImageButton: ImageButton
    private lateinit var selectedImageLayout: ConstraintLayout
    private lateinit var selectedImageView: ImageView
    private lateinit var cancelButton: Button
    private lateinit var searchButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.search_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SearchFragmentViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textView1 = view.findViewById(R.id.textView1)
        textView2 = view.findViewById(R.id.textView2)
        emptyImageLayout = view.findViewById(R.id.emptyImageLayout)
        selectImageButton = view.findViewById(R.id.selectImageButton)
        selectedImageLayout = view.findViewById(R.id.selectedImageLayout)
        selectedImageView = view.findViewById(R.id.selectedImageView)
        cancelButton = view.findViewById(R.id.cancelButton)
        searchButton = view.findViewById(R.id.searchButton)

        selectImageButton.setOnClickListener {
            showSelectAlert()
        }
        cancelButton.setOnClickListener {
            showSelectAlert()
        }
        searchButton.setOnClickListener {
            viewModel.photoUri?.let { classify(it) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==RESULT_OK) {
            if (requestCode == TAKE_PHOTO_REQUEST) {
                setSelectedPhoto()
            } else if (requestCode == SELECT_PHOTO_REQUEST) {
                viewModel.photoUri = data?.data
                setSelectedPhoto()
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode== TAKE_PHOTO_REQUEST) {
                viewModel.photoUri = null
            }
        }
    }

    private fun showSelectAlert() {
        AlertDialog.Builder(requireContext(), R.style.SelectAlertDialog).setItems(arrayOf("사진 찍기", "앨범에서 사진선택", "취소")) { dialog, i ->
            when (i) {
                0 -> openCameraIntent()
                1 -> openAlbumIntent()
                else -> dialog.dismiss()
            }
        }.setTitle("사진 선택").show()
    }

    private fun getPhotoFile(): File {
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(UUID.randomUUID().toString(), ".jpg", storageDir)
    }

    private fun openCameraIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            val file = getPhotoFile()
            viewModel.photoUri = Uri.fromFile(file)
            val photoUri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "com.ssac.android.fileprovider",
                file
            )
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(this, TAKE_PHOTO_REQUEST)
        }
    }

    private fun openAlbumIntent() {
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            startActivityForResult(Intent.createChooser(this, "사진"), SELECT_PHOTO_REQUEST)
        }
    }

    private fun setSelectedPhoto() {
        viewModel.photoUri?.loadBitmap(requireContext())?.let {
            textView1.text = getString(R.string.search_fragment_text1_select_image)
            textView2.text = getString(R.string.search_fragment_text2_select_image)
            selectedImageView.setImageBitmap(it)
            emptyImageLayout.visibility = View.GONE
            selectedImageLayout.visibility = View.VISIBLE

            cancelButton.text = getString(R.string.search_fragment_cancel_button_select_image)
            cancelButton.setOnClickListener {
                showSelectAlert()
            }
            searchButton.text = getString(R.string.search_fragment_search_button_select_image)
            searchButton.setOnClickListener {
                viewModel.photoUri?.let { classify(it) }
            }
        }
    }

    private fun removeSelectedPhoto() {
        viewModel.photoUri = null
        textView1.text = getString(R.string.search_fragment_text1_empty_image)
        textView2.text = getString(R.string.search_fragment_text2_empty_image)
        selectedImageView.setImageBitmap(null)
        emptyImageLayout.visibility = View.VISIBLE
        selectedImageLayout.visibility = View.GONE
        cancelButton.setOnClickListener {
            removeSelectedPhoto()
        }
    }

    private fun classify(uri: Uri) {
//        viewModel.searchResult = "공원"
//        viewModel.searchResultSentence = "멋진 공원이네요!"
//        showClassifyResult()
//        return
        requireContext().contentResolver.openInputStream(uri)?.readBytes()?.toRequestBody("multipart/form-data".toMediaType())?.let { body ->
            val requestBody = MultipartBody.Part.createFormData("image", "image.jpg", body)
            MyApis.getInstance().classify(requestBody).enqueue(object :
                Callback<MyClassifyResponse> {
                override fun onResponse(call: Call<MyClassifyResponse>, response: Response<MyClassifyResponse>) {
                    if (response.isSuccessful) {
                        viewModel.searchResult = response.body()?.name
                        viewModel.searchResultSentence = response.body()?.sentence
                        showClassifyResult()
                    } else {
                        viewModel.searchResult = null
                        viewModel.searchResultSentence = null
                        showClassifyResult()
                    }
                }

                override fun onFailure(call: Call<MyClassifyResponse>, t: Throwable) {
                    Log.d("AAA", t.localizedMessage)
                    viewModel.searchResult = null
                    viewModel.searchResultSentence = null
                    showClassifyResult()
                }
            })
        }
    }

    private fun showClassifyResult() {
        val result = viewModel.searchResult
        val sentence = viewModel.searchResultSentence ?: ""
        if (result.isNullOrEmpty()) {
            cancelButton.setOnClickListener {
                removeSelectedPhoto()
            }
            searchButton.setOnClickListener {
                viewModel.photoUri?.let { classify(it) }
            }
        } else {
            textView1.text = result.asClassifyResult(sentence, requireContext().getColor(R.color.classified_highlight))
            textView2.text = ""
            cancelButton.text = getString(R.string.search_fragment_cancel_button_search_image)
            cancelButton.setOnClickListener {
                showSelectAlert()
            }
            searchButton.text = getString(R.string.search_fragment_search_button_search_image)
            searchButton.setOnClickListener {
                moveToSearchResultActivity()
            }
        }
    }

    private fun moveToSearchResultActivity() {
        val result = viewModel.searchResult
        if (result.isNullOrEmpty()) { return }
        val intent = Intent(requireContext(), SearchResultActivity::class.java)
        intent.putExtra("result", result)
        startActivity(intent)
    }

    private fun refreshAllState() {
        viewModel.photoUri = null
        viewModel.searchResult = null
        viewModel.searchResultSentence = null

        textView1.text = getString(R.string.search_fragment_text1_empty_image)
        textView2.text = getString(R.string.search_fragment_text2_empty_image)

        selectedImageView.setImageBitmap(null)
        emptyImageLayout.visibility = View.VISIBLE
        selectedImageLayout.visibility = View.GONE

        selectImageButton.setOnClickListener {
            showSelectAlert()
        }
        cancelButton.text = getString(R.string.search_fragment_cancel_button_select_image)
        cancelButton.setOnClickListener {
            removeSelectedPhoto()
        }
        searchButton.text = getString(R.string.search_fragment_search_button_select_image)
        searchButton.setOnClickListener {
            viewModel.photoUri?.let { classify(it) }
        }
    }
}
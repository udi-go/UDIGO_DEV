package com.ssac.place.activity.main

import android.net.Uri
import androidx.lifecycle.ViewModel

class SearchFragmentViewModel : ViewModel() {
    var photoUri: Uri? = null
    var searchResult: String? = null
    var searchResultSentence: String? = null
}
package com.kirille.lifepriority.ui.bookmarks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BookmarksViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Prepared fragment to future ViewModel integration"
    }
    val text: LiveData<String> = _text
}
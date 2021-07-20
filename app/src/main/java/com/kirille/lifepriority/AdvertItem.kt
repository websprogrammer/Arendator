package com.kirille.lifepriority

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AdvertItem(
        var postId: Int,
        var name: String,
        var profileLink: String,
        var description: String,
        var photos: String,
        var date: Int,
        var isFavorite: Boolean,
        var isSelected: Boolean,
        var isNew: Boolean

) : Parcelable
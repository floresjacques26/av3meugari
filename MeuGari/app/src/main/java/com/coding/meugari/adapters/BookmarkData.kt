package com.coding.meugari.adapters

import com.google.gson.annotations.SerializedName

data class BookmarkData(
    @SerializedName("BookmarkData")
    val code: Int,
    val image: Int,
    val title: String,
    val location: String
)

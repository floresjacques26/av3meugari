package com.coding.meugari.adapters

import com.google.gson.annotations.SerializedName

data class LayerData(
    @SerializedName("LayerData")
    val code: Int,
    val image: Int,
    val name: String,
    val description: String
)

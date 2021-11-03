package com.hyapp.sytrrraewb.mainfolder

import com.google.gson.annotations.SerializedName
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
data class ResponseStateEntity(
    @SerializedName("id") val id: String = "",
    @SerializedName("url") val url: String = "",
    @SerializedName("access") val access: Boolean = true
)

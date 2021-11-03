package com.hyapp.sytrrraewb.mainfolder

import io.michaelrocks.paranoid.Obfuscate
import retrofit2.http.GET

@Obfuscate
interface ApiInterface {

    @GET(".")
    suspend fun requestState(): ResponseStateEntity
}
package com.kirille.lifepriority

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit


interface APIService {
    @GET("get")
    fun getAdverts(
            @Query("page") page: Int?,
            @Query("city") city: String?,
            @Query("key_words") key_words: String?,
            @Query("rent_type") rent_type: Int?,
            @Query("room_type") room_type: Int?

    ): Call<JsonObject>

    @GET("send_token")
    fun sendToken(
            @Query("token") token: String?,
            @Query("city") city: String?,
            @Query("key_words") key_words: String?,
            @Query("rent_type") rent_type: Int?,
            @Query("room_type") room_type: Int?,
            @Query("notifications") notifications: Int?
    ): Call<JsonObject>


    companion object Factory {
        private const val BASE_URL = "http://104.248.4.131/"

        fun create(): APIService {
            val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            return retrofit.create(APIService::class.java)
        }
    }

}
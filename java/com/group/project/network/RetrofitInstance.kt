package com.group.project.network


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//object for api singleton in nature
object RetrofitInstance {

    val api: ApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl("https://egercounsel.herokuapp.com")
                /*Takes json data and parse it to our data class*/
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }
}
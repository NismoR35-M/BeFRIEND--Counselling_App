package com.group.project.network

import com.group.project.models.DataRespond
import retrofit2.Response
import retrofit2.http.GET

interface ApiInterface {

    //GET request
    @GET("/articles")
    //respond should happen synchronously and not on the main thread
    suspend fun  getArticles(): Response<List<DataRespond>>
}
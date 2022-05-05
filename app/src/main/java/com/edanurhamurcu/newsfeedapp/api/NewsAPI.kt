package com.edanurhamurcu.newsfeedapp.api

import com.edanurhamurcu.newsfeedapp.models.NewsResponse
import com.edanurhamurcu.newsfeedapp.util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {
    //here we define single request that we can execute from the code
    //we use Api interface to access api for request
    //function to get all breaking news from the api
    // we need to specify the type of http request -GET here
    //and we return the responses from the API

    @GET("v2/top-headlines")
    suspend fun getBreakingNews(
        //request parameters to function
        @Query("country")
        countryCode: String = "us", //default to us

        @Query("page")  //to paginate the request
        pageNumber: Int= 1,

        @Query("apiKey")
        apiKey: String= API_KEY

    ):Response<NewsResponse> //return response


    @GET("v2/everything")
    suspend fun searchForNews(

        @Query("q")
        searchQuery: String,

        @Query("page")
        pageNumber: Int= 1,

        @Query("apiKey")
        apiKey: String= API_KEY
    ):Response<NewsResponse>
}
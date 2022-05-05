package com.edanurhamurcu.newsfeedapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edanurhamurcu.newsfeedapp.NewsApplication
import com.edanurhamurcu.newsfeedapp.models.Article
import com.edanurhamurcu.newsfeedapp.models.NewsResponse
import com.edanurhamurcu.newsfeedapp.repository.NewsRepository
import com.edanurhamurcu.newsfeedapp.util.Resource
import dagger.hilt.android.internal.Contexts.getApplication
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.util.*


@RequiresApi(Build.VERSION_CODES.M)
class NewsViewModel(
    app: Application,
    val newsRepository: NewsRepository //parameter
) : AndroidViewModel(app){ //inheriting from android view model to use application context
    //here we use application context to get the context throughout the app running,
    //so it will work even if the activity changes or destroys, the app context will still work until the app's running

    //LIVEDATA OBJECT
    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()

    //Pagination
    var breakingNewsPage= 1
    var searchNewsPage= 1
    var breakingNewsResponse : NewsResponse? = null
    var searchNewsResponse : NewsResponse? = null


    init {
        getNews("in")
    }

    //we cannot start the function in the coroutine so we start the it here
    //viewModelScope makes the function alive only as long as the ViewModel is alive


    fun getNews(countryCode: String)= viewModelScope.launch {
        //breakingNews.postValue(Resource.Loading()) //init loading state before the network call
        safeBreakingNewsCall(countryCode)
        //actual response
        //val response= newsRepository.getBreakingNews(countryCode, breakingNewsPage)

        //handling response
        //breakingNews.postValue(handleBreakingNewsResponse(response))
    }


    fun searchNews(searchQuery: String)= viewModelScope.launch {
        safeSearchNewsCall(searchQuery)
        //searchNews.postValue(Resource.Loading()) //init loading state before the network call

        //actual response
        //val response= newsRepository.searchNews(searchQuery, searchNewsPage)

        //handling response
        //searchNews.postValue(handleSearchNewsResponse(response))
    }

    private fun handleNewsResponse(response: Response<NewsResponse>) : Resource<NewsResponse> {
        if(response.isSuccessful) {
            response.body()?.let { resultResponse ->
                breakingNewsPage++
                if(breakingNewsResponse == null) {
                    breakingNewsResponse = resultResponse
                } else {
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchNewsResponse== null){
                    searchNewsResponse= resultResponse //if first page save the result to the response
                }else{
                    val oldArticles= searchNewsResponse?.articles //else, add all articles to old
                    val newArticle= resultResponse.articles //add new response to new
                    oldArticles?.addAll(newArticle) //add new articles to old articles
                }
                return  Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


    //function to save articles to db: coroutine
    fun saveArticle(article: Article)= viewModelScope.launch {
        newsRepository.upsert(article)
    }

    //function to get all saved news articles
    fun getSavedArticle()= newsRepository.getSavedNews()


    //function to delete article from db
    fun deleteSavedArticle(article: Article)= viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }


    private suspend fun safeBreakingNewsCall(countryCode: String){
        breakingNews.postValue(Resource.Loading())
        try{
            if (hasInternetConnection()){
                val response= newsRepository.getBreakingNews(countryCode, breakingNewsPage)
                //handling response
                breakingNews.postValue(handleNewsResponse(response))
            }else{
                breakingNews.postValue(Resource.Error("No Internet Connection"))
            }

        } catch (t: Throwable){
            when(t){
                is IOException-> breakingNews.postValue(Resource.Error("Network Failure"))
                else-> breakingNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }


    private suspend fun safeSearchNewsCall(searchQuery: String){
        searchNews.postValue(Resource.Loading())
        try{
            if (hasInternetConnection()){
                val response= newsRepository.searchNews(searchQuery, searchNewsPage)
                //handling response
                searchNews.postValue(handleSearchNewsResponse(response))
            }else{
                searchNews.postValue(Resource.Error("No Internet Connection"))
            }

        } catch (t: Throwable){
            when(t){
                is IOException -> searchNews.postValue(Resource.Error("Network Failure"))
                else-> searchNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }



    private fun hasInternetConnection(): Boolean{
        val connectivityManager= getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val activeNetwork= connectivityManager.activeNetwork?: return false
        val capabilities= connectivityManager.getNetworkCapabilities(activeNetwork)?: return false

        return when{
            capabilities.hasTransport(TRANSPORT_WIFI)-> true
            capabilities.hasTransport(TRANSPORT_CELLULAR)-> true
            capabilities.hasTransport(TRANSPORT_ETHERNET)->true
            else -> false
        }
    }
}

private fun <E> List<E>?.addAll(newArticles: List<E>) {

}

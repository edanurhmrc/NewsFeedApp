package com.edanurhamurcu.newsfeedapp.ui.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.edanurhamurcu.newsfeedapp.R
import com.edanurhamurcu.newsfeedapp.adapters.NewsAdapter
import com.edanurhamurcu.newsfeedapp.ui.NewsViewModel
import kotlinx.android.synthetic.main.fragment_news.*
import java.util.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.edanurhamurcu.newsfeedapp.ui.NewsActivity
import com.edanurhamurcu.newsfeedapp.util.Constants.Companion.QUERY_PAGE_SIZE
import com.edanurhamurcu.newsfeedapp.util.Resource

class NewsFragment : Fragment(R.layout.fragment_news) {
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter

    val TAG= "BreakingNewsFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //set viewModels to fragment activity
        //and we cast that as MainActivity so that we can have access to the view model created at MainActivity
        viewModel= (activity as NewsActivity).viewModel

        setupRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle= Bundle().apply{
                //adding serializable article object class to the bundle
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment,
                bundle
            )
        }

        //subscribe to live data
        viewModel.breakingNews.observe(viewLifecycleOwner, Observer { response->
            when(response){
                is Resource.Success->{
                    hideProgressBar()
                    //check null
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages= newsResponse.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage= viewModel.breakingNewsPage == totalPages
                        if (isLastPage){
                            rvNews.setPadding(0,0,0,0)
                        }
                    }
                }
                is Resource.Error->{
                    hideProgressBar()
                    response.message?.let { message->
                        Log.e(TAG, "An error occured: $message" )
                        Toast.makeText(activity, "An error occurred: $message", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading->{
                    showProgressBar()
                }
            }
        })
    }

    private fun setupRecyclerView(){
        newsAdapter= NewsAdapter()
        rvNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@NewsFragment.scrollListener)

        }
    }
    private fun hideProgressBar(){
        paginationProgressBar.visibility= View.INVISIBLE
        isLoading= false
    }
    private fun showProgressBar(){
        paginationProgressBar.visibility= View.VISIBLE
        isLoading= true
    }

    var isLoading= false
    var isLastPage= false
    var isScrolling= false

    val scrollListener= object : RecyclerView.OnScrollListener(){
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            //manually calculating payout numbers for pagination
            val layoutManager= recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition= layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount= layoutManager.childCount
            val totalItemCount= layoutManager.itemCount

            val  isNotLoadingAndNotLastPage= !isLoading && !isLastPage
            val isAtLastItem= firstVisibleItemPosition+ visibleItemCount >= totalItemCount
            val isNotAtBeginning= firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible= totalItemCount >= QUERY_PAGE_SIZE

            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate){
                viewModel.getNews("us")
                isScrolling= false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling= true
            }
        }
    }
}
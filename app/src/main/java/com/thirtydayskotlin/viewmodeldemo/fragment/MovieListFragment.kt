package com.thirtydayskotlin.viewmodeldemo.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.shashank.platform.moviefinder.ConnectivityReceiver
import com.shashank.platform.moviefinder.CustomAdapterMovie
import com.shashank.platform.moviefinder.SearchResults
import com.thirtydayskotlin.viewmodeldemo.R
import com.thirtydayskotlin.viewmodeldemo.databinding.FragmentMovieListBinding
import com.thirtydayskotlin.viewmodeldemo.network.ApiClient
import com.thirtydayskotlin.viewmodeldemo.network.ApiInterface
import com.thirtydayskotlin.viewmodeldemo.network.toast
import com.thirtydayskotlin.viewmodeldemo.service.MovieDetailScrollingActivity
import com.thirtydayskotlin.viewmodeldemo.service.PaginationScrollListener
import com.thirtydayskotlin.viewmodeldemo.service.RecyclerItemClickListener
import kotlinx.android.synthetic.main.fragment_movie_list.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class MovieListFragment : Fragment(),ConnectivityReceiver.ConnectivityReceiverListener {

    var PAGE_START = 1
    var isLoading = false
    var isLastPage = false
    var TOTAL_PAGES = 20
    var currentPage = PAGE_START
    lateinit var s1: String
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var apiInterface: ApiInterface
    internal lateinit var searchView: SearchView
    internal var searchResultsList: MutableList<SearchResults.SearchItem> = ArrayList()
    lateinit var mAdapter: CustomAdapterMovie



    init {

        apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
        checkConnection()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View?
    {
        val binding= DataBindingUtil.inflate<FragmentMovieListBinding>(inflater,R.layout.fragment_movie_list,container,false)


        mAdapter = CustomAdapterMovie(searchResultsList)
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.movieRecyclerView.setLayoutManager(linearLayoutManager)
        binding.movieRecyclerView.setItemAnimator(DefaultItemAnimator())
        binding.movieRecyclerView.setAdapter(mAdapter)

        binding.movieRecyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(
                activity!!.applicationContext,
                object :
                    RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val searchItem = searchResultsList[position]
                        val intent = Intent(context, MovieDetailScrollingActivity::class.java)
                        intent.putExtra("poster", searchItem.poster)
                        intent.putExtra("title", searchItem.title)
                        startActivity(intent)
                    }

                })
        )
        binding.movieRecyclerView.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {
            override fun getTotalPageCount(): Int {
                return TOTAL_PAGES
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

            protected override fun loadMoreItems() {
                isLoading = true
                currentPage += 1

                // mocking network delay for API call
                Handler().postDelayed({ loadNextPage(s1) }, 1000)
            }
        })

        binding.linearLayout.visibility = View.VISIBLE
        binding.movieRecyclerView.visibility = View.GONE


        binding.imgSearch.setOnClickListener { view:View->
          search(binding.etxSearchMovie.text.toString())
            activity!!.getSystemService(Context.INPUT_METHOD_SERVICE)
            val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etxSearchMovie.getWindowToken(), 0);
        }
        return binding.root
    }

    private fun search(search: String) {

        if (checkConnection() == true) {
            isLoading = false
            isLastPage = false
            currentPage = PAGE_START
            searchResultsList.clear()
            getSearchResultMoviesData(search)
        }
    }

    fun getSearchResultMoviesData(s: String) {
        s1 = s
        movie_recycler_view.setVisibility(View.VISIBLE)
        linear_layout.setVisibility(View.GONE)
        movie_recycler_view.showShimmerAdapter()
        val call = apiInterface.getSearchResultData(s, "98baf47f", currentPage)
        call.enqueue(object : Callback<SearchResults> {
            override fun onResponse(call: Call<SearchResults>, response: retrofit2.Response<SearchResults>) {
                movie_recycler_view.hideShimmerAdapter()
                if (response.isSuccessful) {
                    if (response.body()!!.getResponse().equals("True")) {
                        searchResultsList.addAll(response.body()!!.getSearch()!!)
                        if (currentPage <= TOTAL_PAGES)
                            mAdapter.addLoadingFooter()
                        else
                            isLastPage = true
                    } else {
                        movie_recycler_view.setVisibility(View.GONE)
                        linear_layout.setVisibility(View.VISIBLE)
                        context!!.toast("Too many results!")
                    }
                } else

                    context!!.toast("Error")
            }

            override fun onFailure(call: Call<SearchResults>, t: Throwable) {
                movie_recycler_view.hideShimmerAdapter()
                context!!.toast(""+t.message)
            }
        })
    }


    fun loadNextPage(s: String) {

        val call = apiInterface.getSearchResultData(s, "69841868", currentPage)
        call.enqueue(object : Callback<SearchResults> {
            override fun onResponse(call: Call<SearchResults>, response: Response<SearchResults>) {
                if (response.isSuccessful) {
                    if (response.body()!!.getResponse().equals("True")) {
                        mAdapter.removeLoadingFooter()
                        isLoading = false
                        searchResultsList.addAll(response.body()!!.getSearch()!!)
                        if (currentPage != TOTAL_PAGES)
                            mAdapter.addLoadingFooter()
                        else
                            isLastPage = true
                    } else {
                        isLoading = false
                        isLastPage = true
                        mAdapter.removeLoadingFooter()

                        context!!.toast("Movie not found!")
                    }
                } else

                    context!!.toast("Error")

            }

            override fun onFailure(call: Call<SearchResults>, t: Throwable) {


                context!!.toast(""+t.message)
            }
        })


    }


    private fun showToast(isConnected: Boolean) {
        if (!isConnected)
           context!!.toast(getString(R.string.no_connectivity))
    }
    //Check internet connectivity code
    // Method to manually check connection status
    private fun checkConnection(): Boolean {
        val isConnected = ConnectivityReceiver.isConnected()
        showToast(isConnected)
        return isConnected
    }
    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showToast(isConnected)
    }

}

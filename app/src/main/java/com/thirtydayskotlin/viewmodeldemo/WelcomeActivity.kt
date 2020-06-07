package com.thirtydayskotlin.viewmodeldemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.shashank.platform.moviefinder.ConnectivityReceiver
import com.shashank.platform.moviefinder.CustomAdapterMovie
import com.shashank.platform.moviefinder.SearchResults
import com.thirtydayskotlin.viewmodeldemo.network.ApiClient
import com.thirtydayskotlin.viewmodeldemo.network.ApiInterface
import kotlinx.android.synthetic.main.activity_welcome.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class WelcomeActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
    }
}

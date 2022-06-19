package com.group.project.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.group.project.adapters.ArticlesAdapter
import com.group.project.network.RetrofitInstance
import com.group.project.databinding.ActivityArticlesBinding
import retrofit2.*
import java.io.IOException

//const val BASE_URL = "https://egercounsel.herokuapp.com/articles/"
const val TAG = "ArticlesActivity"
class ArticlesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticlesBinding
    //instance of recycler view adapter
    private lateinit var articlesAdapter: ArticlesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticlesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()

        //use coroutine to update data behind the scene
        lifecycleScope.launchWhenCreated {
            binding.progressBar.isVisible = true
            val response = try {
                RetrofitInstance.api.getArticles()
            } catch (e: IOException){
                Log.e(TAG,"IOException,check your internet connection")
                binding.progressBar.isVisible = false
                return@launchWhenCreated
            } catch (e: HttpException){
                Log.e(TAG,"HttpException, unexpected response")
                binding.progressBar.isVisible = false
                return@launchWhenCreated
            }
            if (response.isSuccessful && response.body() != null) {
                articlesAdapter.articles = response.body()!!
            } else {
                Log.e(TAG,"Response not successful")
            }
            binding.progressBar.isVisible = false
        }

        //get data from retrofit
        //getMyData()
    }
    private fun setupRecyclerView() = binding.rvArticles.apply {
        articlesAdapter = com.group.project.adapters.ArticlesAdapter()
        adapter = articlesAdapter
        layoutManager = LinearLayoutManager(this@ArticlesActivity)
    }
//
//    private fun getMyData() {
//        val retrofitBuilder = Retrofit.Builder()
//            .addConverterFactory(GsonConverterFactory.create())
//            .baseUrl(BASE_URL)
//            .build()
//            .create(ApiInterface::class.java)
//
//        //variable to get the data from the builder
//        val retrofitData = retrofitBuilder.getData()
//
//        //shortcut ctrl+Shift+space
//        retrofitData.enqueue(object : Callback<List<DataRespond>?> {
//            override fun onResponse(
//                call: Call<List<DataRespond>?>,
//                response: Response<List<DataRespond>?>
//            ) {
//                val responseBody = response.body()!!
//
//                //test in text
//                val myStringBuilder = StringBuilder()
//                for (myData in responseBody) {
//                    myStringBuilder.append(myData.id)
//                    myStringBuilder.append("\n")
//                }
//                textViewId.text = myStringBuilder
//
//                //show data in recycler view
//            }
//
//            override fun onFailure(call: Call<List<DataRespond>?>, t: Throwable) {
//                Log.d("ArticlesActivity", "onFailure"+t.message)
//            }
//        })
//    }
}
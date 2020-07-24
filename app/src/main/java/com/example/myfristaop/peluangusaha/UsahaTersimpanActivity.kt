package com.example.myfristaop.peluangusaha

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.myfristaop.peluangusaha.adapter.UsahaTersimpanAdapter
import com.example.myfristaop.peluangusaha.api.PeluangUsahaApi
import com.example.myfristaop.peluangusaha.model.UsahaTersimpanResponse
import com.example.myfristaop.peluangusaha.preferences.UserPreferences
import kotlinx.android.synthetic.main.activity_usaha_tersimpan.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val EXTRA_USAHA_TERSIMPAN ="EXTRA_USAHA_TERSIMPAN"

class UsahaTersimpanActivity : AppCompatActivity() {

    private lateinit var userPreferences : UserPreferences
    private val prefFileName = "DATAUSER"

    lateinit var retrofit: Retrofit
    lateinit var peluangUsahaApi: PeluangUsahaApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usaha_tersimpan)

        userPreferences =UserPreferences(this@UsahaTersimpanActivity, prefFileName)
        retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        peluangUsahaApi = retrofit.create(PeluangUsahaApi::class.java)

        ambilUsahaTersimpan()
    }
    fun ambilUsahaTersimpan() {
        doAsync {
            val token = userPreferences.token
            var call : Call<List<UsahaTersimpanResponse>> =  peluangUsahaApi.ambilUsahaTersimpan(token)
            call.enqueue(object : Callback<List<UsahaTersimpanResponse>> {
                override fun onResponse(call: Call<List<UsahaTersimpanResponse>>, response: Response<List<UsahaTersimpanResponse>>) {
                    if(response.isSuccessful) {
                        var usahaTersimpan : List<UsahaTersimpanResponse>? =  response.body()
                        if (usahaTersimpan != null) {
                            uiThread {
                                showRvUsahTersimpan(usahaTersimpan)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<List<UsahaTersimpanResponse>>, t: Throwable) {
                    Log.d("Semua usaha -----------", t.toString())
                }
            })
        }
    }

    private fun showRvUsahTersimpan (list: List<UsahaTersimpanResponse>) {
        rvUsahaTersimpan.layoutManager = LinearLayoutManager(this)
        val adapter = UsahaTersimpanAdapter(list as ArrayList<UsahaTersimpanResponse>)

        rvUsahaTersimpan.setHasFixedSize(true)

        adapter.setOnClickListener(object : UsahaTersimpanAdapter.OnItemClickListener {
            override fun onClickItem(usaha: UsahaTersimpanResponse) {
                val intent = Intent(this@UsahaTersimpanActivity, DetailUsahaTersimpanActivity::class.java)
                intent.putExtra(EXTRA_USAHA_TERSIMPAN, usaha)
                startActivity(intent)
            }
        })
        rvUsahaTersimpan.adapter = adapter
        pbListUsahaTersimpan.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        ambilUsahaTersimpan()
    }
}

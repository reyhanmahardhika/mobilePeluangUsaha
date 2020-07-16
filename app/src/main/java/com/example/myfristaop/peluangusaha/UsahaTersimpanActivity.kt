package com.example.myfristaop.peluangusaha

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.myfristaop.peluangusaha.api.PeluangUsahaApi
import com.example.myfristaop.peluangusaha.model.UsahaResponse
import com.example.myfristaop.peluangusaha.model.UsahaTersimpanResponse
import com.example.myfristaop.peluangusaha.preferences.UserPreferences
import kotlinx.android.synthetic.main.activity_usaha_tersimpan.*
import org.jetbrains.anko.doAsync
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
                    var usahaTersimpan : List<UsahaTersimpanResponse>? =  response.body()
                    if (usahaTersimpan != null) {
                        var text = ""
                        for (i in usahaTersimpan) {
                            text += "id usaha: "+ i.id_usaha + "idWilayah: " + i.id_wilayah +"\n"
                        }
                        txtUsahaTersimpan.text = text
                    }
                }

                override fun onFailure(call: Call<List<UsahaTersimpanResponse>>, t: Throwable) {
                    Log.d("Semua usaha -----------", t.toString())
                }
            })
        }
    }
}

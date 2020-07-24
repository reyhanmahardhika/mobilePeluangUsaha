package com.example.myfristaop.peluangusaha

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.example.myfristaop.peluangusaha.api.PeluangUsahaApi
import com.example.myfristaop.peluangusaha.model.UsahaTersimpanResponse
import com.example.myfristaop.peluangusaha.preferences.UserPreferences
import kotlinx.android.synthetic.main.activity_profil.*
import kotlinx.android.synthetic.main.activity_profil.foto_profile
import org.jetbrains.anko.doAsync
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfilActivity : AppCompatActivity() {
    // Di dalam userPpreferences tersimapn data user dan juga token untuk mengakses web
    lateinit var userPreference: UserPreferences
    private val prefFileName = "DATAUSER"

    lateinit var retrofit: Retrofit
    lateinit var peluangUsahaApi: PeluangUsahaApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        peluangUsahaApi = retrofit.create(PeluangUsahaApi::class.java)

        userPreference = UserPreferences(this, prefFileName)

        ambilUsahaTersimpan()
        nickname.text = userPreference.nama
        email_pengguna.text = userPreference.email

        usahaTersimpan.setOnClickListener(){
            val intent = Intent(this@ProfilActivity, UsahaTersimpanActivity::class.java)
            startActivity(intent)
        }

    }
    fun ambilUsahaTersimpan() {
        doAsync {
            val token = userPreference.token
            var call : Call<List<UsahaTersimpanResponse>> =  peluangUsahaApi.ambilUsahaTersimpan(token)
            call.enqueue(object : Callback<List<UsahaTersimpanResponse>> {
                override fun onResponse(call: Call<List<UsahaTersimpanResponse>>, response: Response<List<UsahaTersimpanResponse>>) {
                    val usahaTersimpan : List<UsahaTersimpanResponse>? =  response.body()
                    jumlahUsahaTersimpan.text=("${usahaTersimpan?.size} Usaha")
                }
                override fun onFailure(call: Call<List<UsahaTersimpanResponse>>, t: Throwable) {
                    Log.d("Semua usaha -----------", t.toString())
                }
            })
        }
    }



}

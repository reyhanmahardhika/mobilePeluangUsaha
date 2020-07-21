package com.example.myfristaop.peluangusaha

import android.R.attr.*
import android.app.ActionBar
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.example.myfristaop.peluangusaha.api.PeluangUsahaApi
import com.example.myfristaop.peluangusaha.model.UsahaResponse
import com.example.myfristaop.peluangusaha.model.UsahaTersimpanResponse
import com.example.myfristaop.peluangusaha.preferences.UserPreferences
import kotlinx.android.synthetic.main.fragment_daftar_usaha.*
import kotlinx.android.synthetic.main.fragment_usaha.*
import kotlinx.android.synthetic.main.fragment_usaha.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class RekomendasiUsaha : AppCompatActivity(){
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.fragment_daftar_usaha)
    lateinit var userPreferences : UserPreferences
    val prefFileName = "DATAUSER"

    lateinit var retrofit: Retrofit
    lateinit var peluangUsahaApi: PeluangUsahaApi
    val main= MainActivity
    var usaha : List<UsahaResponse>? = null

    fun ambilSemuaUsaha() {
      doAsync {
        val token = userPreferences.token
        val call: Call<List<UsahaResponse>> = peluangUsahaApi.ambilSemuaUsaha(token)
        call.enqueue(object : Callback<List<UsahaResponse>> {
          override fun onResponse(call: Call<List<UsahaResponse>>, response: Response<List<UsahaResponse>>) {
            usaha = response.body()
          }

          override fun onFailure(call: Call<List<UsahaResponse>>, t: Throwable) {
            Log.d("Semua usaha -----------", t.toString())
          }
        })
      }
    }
    ambilSemuaUsaha()
    //sebelumnya dilakukan proses pengurutan dari hasil vektor v
    //jika hasil dari vektor v != 0 maka usaha tersebut d tampilkan,
    // jika jumlah vektor v yang !=0 lebih dari 10 maka usaha yang d tampilkan hanya 10 teratas

    for(i in 0..10) {//atur jumlah usaha

      var view: View = LayoutInflater.from(this).inflate(R.layout.fragment_usaha, null)
      view.nama_usaha.setText("nama usaha}")
      view.usaha.setOnClickListener{
        Toast.makeText(this, " Nama Usaha "+i.toString(), Toast.LENGTH_SHORT).show()
        //detail usaha
      }
      view.remove_bookmark.setOnClickListener{
        view.remove_bookmark.visibility = View.GONE
        view.bookmark.visibility = View.VISIBLE
        //data usaha disimpan
      }
      view.bookmark.setOnClickListener{
        view.remove_bookmark.visibility = View.VISIBLE
        view.bookmark.visibility = View.GONE
        //data usaha tidak disimpan
      }
      layout_daftarUsaha.addView(view)

    }
  }
}


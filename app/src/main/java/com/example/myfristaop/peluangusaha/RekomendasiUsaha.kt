package com.example.myfristaop.peluangusaha

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.example.myfristaop.peluangusaha.adapter.RekomendasiUsahaAdapter
import com.example.myfristaop.peluangusaha.api.PeluangUsahaApi
import com.example.myfristaop.peluangusaha.model.UsahaResponse
import com.example.myfristaop.peluangusaha.model.VektorV
import com.example.myfristaop.peluangusaha.model.Wilayah
import com.example.myfristaop.peluangusaha.preferences.UserPreferences
import kotlinx.android.synthetic.main.activity_rekomendasi_usaha.*
import org.jetbrains.anko.doAsync
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val EXTRA_REKOMENDASI_USAHA ="EXTRA_REKOMENDASI_USAHA"
class RekomendasiUsaha : AppCompatActivity(){

  private lateinit var userPreferences: UserPreferences
  private val prefFileName = "DATAUSER"
  private var latitude: Double? = null
  private var longitude: Double? = null
  private var wilayah: String? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_rekomendasi_usaha)

    userPreferences = UserPreferences(this, prefFileName)
    latitude = intent.getDoubleExtra("LATITUDE", 0.0)
    longitude = intent.getDoubleExtra("LONGITUDE", 0.0)
    wilayah = intent.getStringExtra("ID_WILAYAH")

    var listVektorV = intent.getParcelableArrayListExtra<VektorV>("VEKTOR_V")
    var sortedListVektorV =listVektorV.sortedByDescending { it.nilaiVektor }
    tampilkanRekomendasi(sortedListVektorV)

  }

  private fun tampilkanRekomendasi(listVektorV: List<VektorV>) {
    var adapter = RekomendasiUsahaAdapter(listVektorV)
    rvRekomendasiUsaha.layoutManager = LinearLayoutManager(this)
    rvRekomendasiUsaha.setHasFixedSize(true)
    rvRekomendasiUsaha.adapter = adapter
    adapter.setOnClickListener(object : RekomendasiUsahaAdapter.OnItemClickListener {
      override fun onClickItem(vektorV: UsahaResponse) {
        var intent = Intent(this@RekomendasiUsaha, DetailRekomendasiUsahaActivity::class.java)
        intent.putExtra(EXTRA_REKOMENDASI_USAHA, vektorV)
        intent.putExtra("LATITUDE", latitude)
        intent.putExtra("LONGITUDE", longitude)
        intent.putExtra("ID_WILAYAH", wilayah)
        startActivity(intent)
        Toast.makeText(this@RekomendasiUsaha, vektorV.nama_usaha, Toast.LENGTH_SHORT).show()

      }
    })
  }
}


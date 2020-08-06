package com.example.myfristaop.peluangusaha

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.myfristaop.peluangusaha.adapter.RekomendasiUsahaAdapter
import com.example.myfristaop.peluangusaha.api.PeluangUsahaApi
import com.example.myfristaop.peluangusaha.model.*
import com.example.myfristaop.peluangusaha.preferences.UserPreferences
import kotlinx.android.synthetic.main.activity_detail_rekomendasi_usaha.*
import kotlinx.android.synthetic.main.activity_rekomendasi_usaha.*
import kotlinx.android.synthetic.main.fragment_usaha.*
import kotlinx.android.synthetic.main.item_usaha_list.*
import org.jetbrains.anko.doAsync
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val  EXTRA_REKOMENDASI_USAHA ="EXTRA_REKOMENDASI_USAHA"
class RekomendasiUsaha : AppCompatActivity(){

    private lateinit var userPreferences: UserPreferences
    private val prefFileName = "DATAUSER"
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var wilayah: String? = null
    private var alamat: String? = null
    private var modal: Int? = null
    lateinit var peluangUsahaApi: PeluangUsahaApi
    lateinit var retrofit: Retrofit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rekomendasi_usaha)

        retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        peluangUsahaApi = retrofit.create(PeluangUsahaApi::class.java)

        userPreferences = UserPreferences(this, prefFileName)
        latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        longitude = intent.getDoubleExtra("LONGITUDE", 0.0)
        wilayah = intent.getStringExtra("ID_WILAYAH")
        alamat = intent.getStringExtra("ALAMAT")
        modal = intent.getIntExtra("MODAL", 0)


        val listVektorV = intent.getParcelableArrayListExtra<VektorV>("VEKTOR_V").sortedByDescending { it.nilaiVektor }
        if(listVektorV.size < 1) {
            txtLabelRekomendasiUsaha.text = "Tidak ditemukan usaha yang tepat di lokasi anda"
            labelDaftarRekomendasiUsaha.visibility = View.GONE
        } else {
            txtLabelRekomendasiUsaha.text = "Ditemukan ${listVektorV.slice(0..9).size} usaha yang tepat di lokasi anda."
        }
        tampilkanRekomendasi(listVektorV.slice(0..9))

        txtAlamatRekomendasiUsaha.text = alamat
        txtModalRekomendasiUsaha.text = modal.toString()

    }

    private fun tampilkanRekomendasi(listVektorV: List<VektorV>) {
        var adapter = RekomendasiUsahaAdapter(listVektorV)
        rvRekomendasiUsaha.layoutManager = LinearLayoutManager(this)
        rvRekomendasiUsaha.setHasFixedSize(true)
        rvRekomendasiUsaha.adapter = adapter
        adapter.setOnClickListener(object : RekomendasiUsahaAdapter.OnItemClickListener {
            override fun onClickItem(vektorV: UsahaResponse) {
                val intent = Intent(this@RekomendasiUsaha, DetailRekomendasiUsahaActivity::class.java)
                intent.putExtra(EXTRA_REKOMENDASI_USAHA, vektorV)
                intent.putExtra("LATITUDE", latitude)
                intent.putExtra("LONGITUDE", longitude)
                intent.putExtra("ID_WILAYAH", wilayah)
                startActivity(intent)
            }
        })
    }
}


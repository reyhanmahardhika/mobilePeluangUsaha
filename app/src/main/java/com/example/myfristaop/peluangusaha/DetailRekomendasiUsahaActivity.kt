package com.example.myfristaop.peluangusaha

import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.bumptech.glide.load.resource.drawable.DrawableResource
import com.example.myfristaop.peluangusaha.adapter.TargetDanPesaingAdapter
import com.example.myfristaop.peluangusaha.adapter.Tempat
import com.example.myfristaop.peluangusaha.api.PeluangUsahaApi
import com.example.myfristaop.peluangusaha.model.UsahaResponse
import com.example.myfristaop.peluangusaha.model.UsahaTersimpan
import com.example.myfristaop.peluangusaha.model.UsahaTersimpanResponse
import com.example.myfristaop.peluangusaha.preferences.UserPreferences
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.SyncHttpClient
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_detail_rekomendasi_usaha.*
import kotlinx.android.synthetic.main.dialog_map_layout.view.*
import kotlinx.android.synthetic.main.fragment_usaha.*
import kotlinx.coroutines.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat
import kotlin.math.roundToInt

class DetailRekomendasiUsahaActivity : AppCompatActivity() {

    lateinit var pos: LatLng
    lateinit var lokasiUsaha : Location
    val radius = 1000
    lateinit var dataTargetPasar: ArrayList<Tempat>
    lateinit var dataPesaing: ArrayList<Tempat>

    private lateinit var userPreferences : UserPreferences
    private val prefFileName = "DATAUSER"

    lateinit var retrofit: Retrofit
    lateinit var peluangUsahaApi: PeluangUsahaApi
    var saveState = 0

    var usahaTersimpan : List<UsahaTersimpanResponse>? = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_rekomendasi_usaha)
        val usaha = intent.getParcelableExtra<UsahaResponse>(EXTRA_REKOMENDASI_USAHA)

        retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        peluangUsahaApi = retrofit.create(PeluangUsahaApi::class.java)

        userPreferences = UserPreferences(this, prefFileName)


        txtNamaDetailRekomendasiUsaha.text = "Usaha ${usaha.nama_usaha}"
        txtModalDetailRekomendasiUsaha.text = "Modal Rp.${usaha.modal}"
        txtDeskripsiDetailRekomendasiUsaha.text = usaha.deskripsi_usaha
        txtBahanBakuDetailRekomendasiUsaha.text = usaha.bahan_baku

        val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)
        val idWilayah = intent.getStringExtra("ID_WILAYAH")


        pos = LatLng(latitude, longitude)
        lokasiUsaha = Location("")
        lokasiUsaha.latitude = pos.latitude
        lokasiUsaha.longitude = pos.longitude

        CoroutineScope(Dispatchers.IO).launch {
            ambilDataPesaing(pos, radius, usaha.nama_usaha)
            ambilDataTarget(pos, radius, usaha.target_pasar)
        }
        cekUsahaTersimpan()

        btnTampilkanTargetRekomendasiUsahaDalamPeta.setOnClickListener {
            tampilkanTempatDalamMap("Peta Daftar Target Pasar", dataTargetPasar)
        }
        btnTampilkanPesaingRekomendasiUsahaDalamPeta.setOnClickListener {
            tampilkanTempatDalamMap("Peta Daftar Pesaing", dataPesaing)
        }
        fabHapusDetailRekomendasiUsaha.setOnClickListener {
            if(saveState == 1) {
                val alertDialog = AlertDialog.Builder(this)

                alertDialog.setMessage("Usaha ini sudah ada pada daftar usaha tersimpan, Anda yakin ingin menghapus?")
                alertDialog.setCancelable(true)
                alertDialog.setPositiveButton("Ya") { dialog, id -> hapusUsahaTersimpan(usaha.id_usaha, userPreferences.USER_ID, idWilayah, latitude.toString(), longitude.toString())}
                alertDialog.setNegativeButton("Tidak") { dialog, id -> dialog.cancel()}
                alertDialog.show()
            } else {
                simpanUsaha(usaha.id_usaha, userPreferences.USER_ID, idWilayah, latitude.toString(), longitude.toString())
            }
        }

    }
    fun cekUsahaTersimpan() {
        ambilUsahaTersimpan()
    }
    fun ambilUsahaTersimpan() {
        doAsync {
            val token = userPreferences.token
            var call : Call<List<UsahaTersimpanResponse>> =  peluangUsahaApi.ambilUsahaTersimpan(token)
            call.enqueue(object : Callback<List<UsahaTersimpanResponse>> {
                override fun onResponse(call: Call<List<UsahaTersimpanResponse>>, response: Response<List<UsahaTersimpanResponse>>) {
                    usahaTersimpan =  response.body()
                    val usaha = intent.getParcelableExtra<UsahaResponse>(EXTRA_REKOMENDASI_USAHA)
                    for (i in 0 until usahaTersimpan!!.size){
                        if(usahaTersimpan!![i].id_usaha==usaha.id_usaha){
                            saveState=1
                            break
                        }
                    }
                    if(saveState==1){ fabHapusDetailRekomendasiUsaha.setImageResource(R.drawable.ic_delete_white_24dp) }
                    else{ fabHapusDetailRekomendasiUsaha.setImageResource(R.drawable.ic_save_white_24dp) }
                }

                override fun onFailure(call: Call<List<UsahaTersimpanResponse>>, t: Throwable) {
                    Log.d("Semua usaha -----------", t.toString())
                }
            })
        }
    }
    fun simpanUsaha(id_usaha: String, id_pengguna: String, id_wilayah: String, latitude: String, longitude: String) {
    fabHapusDetailRekomendasiUsaha.isEnabled = false
        doAsync {
            val token = userPreferences.token
            val usahaTersimpan = UsahaTersimpan("",id_usaha, id_pengguna, id_wilayah, latitude, longitude)
            var call = peluangUsahaApi.simpanUsaha(token, usahaTersimpan)
            call.enqueue(object : Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@DetailRekomendasiUsahaActivity, "Gagal menyimpan, coba lagi nanti.", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if(response.code() == 200) {
                        Toast.makeText(this@DetailRekomendasiUsahaActivity, "Berhasil menyimpan, ${response.code()}.", Toast.LENGTH_SHORT).show()
                        saveState = 1
                        fabHapusDetailRekomendasiUsaha.isEnabled = true
                        fabHapusDetailRekomendasiUsaha.setImageResource(R.drawable.ic_delete_white_24dp)
                    }
                }
            })
        }

    }

    private fun hapusUsahaTersimpan(id_usaha: String, id_pengguna: String, id_wilayah: String, latitude: String, longitude: String) {
        fabHapusDetailRekomendasiUsaha.isEnabled = false
        doAsync {
            val token = userPreferences.token
            val usahaTersimpan = UsahaTersimpan("",id_usaha, id_pengguna, id_wilayah, latitude, longitude)
            var call  =  peluangUsahaApi.hapusUsahaTersimpanByUser(token, usahaTersimpan)
            call.enqueue(object: Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@DetailRekomendasiUsahaActivity, "Gagal menghapus, coba lagi nanti.", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if(response.code() == 200) {
                        Toast.makeText(this@DetailRekomendasiUsahaActivity, "Berhasil menghapus, ${response.code()}.", Toast.LENGTH_SHORT).show()
                        saveState = 0
                        fabHapusDetailRekomendasiUsaha.isEnabled = true
                        fabHapusDetailRekomendasiUsaha.setImageResource(R.drawable.ic_save_white_24dp)
                    }
                }
            })
        }
    }

    private fun tampilkanTempatDalamMap(title: String, listTempat: ArrayList<Tempat>) {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_map_layout, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setCancelable(true)
        val  mAlertDialog = mBuilder.show()
        mDialogView.dialogLabel.text = title
        mDialogView.btnTutupDialogMap.setOnClickListener {
            mAlertDialog.dismiss()
        }
        var mMapView = mDialogView.mapsLayoutOnDialog
        MapsInitializer.initialize(this@DetailRekomendasiUsahaActivity);
        mMapView.onCreate(mAlertDialog.onSaveInstanceState());
        mMapView.onResume();

        mMapView.getMapAsync {googleMap ->
            val lokasiUsahaAnda = LatLng(lokasiUsaha.latitude, lokasiUsaha.longitude)
            val icon = BitmapDescriptorFactory.fromResource(R.drawable.small_business_38px)
            val markerLokasiUsahaAnda = googleMap.addMarker(MarkerOptions().position(lokasiUsahaAnda).title("Lokasi Usaha Anda"))
            markerLokasiUsahaAnda.setIcon(icon)
            markerLokasiUsahaAnda.isDraggable = false
            markerLokasiUsahaAnda.showInfoWindow()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiUsahaAnda, 14.7f))
            googleMap.addCircle(CircleOptions().center(lokasiUsahaAnda).radius(1000.0).strokeWidth(2f))

            for(tempat in listTempat) {
                val lokasiTempat = LatLng(tempat.pos.latitude, tempat.pos.longitude)
                var marker = googleMap.addMarker(MarkerOptions().position(lokasiTempat).title("${tempat.nama}, ${tempat.jarak}"))
            }
        }
    }

    private suspend fun tampilkanDaftarTargetPasar(target: ArrayList<Tempat>) {
        withContext(Dispatchers.Main) {
            dataTargetPasar = target
            val adapter = TargetDanPesaingAdapter(target)
            rvTargetPasarRekomendasiUsaha.setHasFixedSize(true)
            rvTargetPasarRekomendasiUsaha.layoutManager = LinearLayoutManager(this@DetailRekomendasiUsahaActivity)
            rvTargetPasarRekomendasiUsaha.adapter = adapter
            pbTargetDetailRekomendasiUsaha.visibility = View.INVISIBLE
            btnTampilkanTargetRekomendasiUsahaDalamPeta.visibility = View.VISIBLE
        }
    }

    private suspend fun tampilkandaftarPesaing(pesaing: ArrayList<Tempat>) {
        withContext (Dispatchers.Main) {
            pbPesaingDetailUsaha.visibility = View.INVISIBLE
            if(pesaing.size > 0) {
                dataPesaing = pesaing
                var adapter = TargetDanPesaingAdapter(pesaing)
                rvPesaingRekomendasiUsaha.layoutManager = LinearLayoutManager(this@DetailRekomendasiUsahaActivity)
                rvPesaingRekomendasiUsaha.setHasFixedSize(true)
                rvPesaingRekomendasiUsaha.adapter = adapter
                btnTampilkanPesaingRekomendasiUsahaDalamPeta.visibility = View.VISIBLE
            } else {
                txtTidakAdaPesaingDetailRekomendasiUsaha.visibility = View.VISIBLE
                rvPesaingRekomendasiUsaha.visibility= View.GONE
            }
        }
    }

    private suspend fun ambilDataPesaing(pos: LatLng, radius: Int, name: String) {
        withContext(Dispatchers.IO) {
            async{

                println("debug: launching job1: ${Thread.currentThread().name}")
                var client = SyncHttpClient()
                var url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${pos.latitude},${pos.longitude}&radius=${radius}&name=${name}&key=AIzaSyCIzmKLdFHcim2FlP7e4FmVl-L4i7UlSNc"
                val charset = Charsets.UTF_8
                var listPesaing: ArrayList<Tempat> = arrayListOf()
                var handler = object : AsyncHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {

                        if (responseBody != null) {
                            var hasil = JSONObject(responseBody.toString(charset))
                            var res: JSONArray? = hasil.getJSONArray("results")
                            if (res != null) {
                                for (i in 0 until res.length()) {
                                    try {
                                        val oneObject: JSONObject = res?.getJSONObject(i)
                                        val posObj: JSONObject = oneObject.getJSONObject("geometry").getJSONObject("location")
                                        val namaTempat = oneObject.getString("name")
                                        var lokasiPesaing = Location("")
                                        lokasiPesaing.latitude = posObj.getDouble("lat")
                                        lokasiPesaing.longitude = posObj.getDouble(("lng"))

                                        val jarak = lokasiUsaha.distanceTo(lokasiPesaing)
                                        Log.w("nama tempat", namaTempat)
                                        Log.w("jarak", "$jarak")
                                        val df = DecimalFormat("#.##")
                                        if(jarak/1000 <= 1.0) {
                                            var a = Tempat(namaTempat, "±${jarak.roundToInt()}m", lokasiPesaing)
                                            listPesaing.add(a)
                                        }
                                    } catch (e: JSONException) {
                                        break
                                    }
                                }
                            }

                        } else {
                            Log.w("pesaing", "pesaing tidak ada")
                        }
                        return
                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?, error: Throwable?) {

                        Log.d("pesaing", "Failed" + statusCode.toString())
                    }

                }
                client.get(url, handler)
                tampilkandaftarPesaing(listPesaing)
            }.await()
        }
    }

    private suspend fun ambilDataTarget(pos: LatLng, radius: Int, targetPasar: String) {
        var target = targetPasar.toLowerCase().split(",")
        var listTarget: ArrayList<Tempat> = arrayListOf()
        withContext(Dispatchers.IO) {
            async {
                for (t in target) {
                    var client = SyncHttpClient()
                    var url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${pos.latitude},${pos.longitude}&radius=${radius}&name=${t.trim()}&key=AIzaSyCIzmKLdFHcim2FlP7e4FmVl-L4i7UlSNc"
                    val charset = Charsets.UTF_8
                    var handler = object : AsyncHttpResponseHandler() {
                        override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                            if (responseBody != null) {
                                var hasil = JSONObject(responseBody.toString(charset))
                                var res: JSONArray? = hasil.getJSONArray("results")
                                if (res != null) {
                                    for (i in 0 until res.length()) {
                                        try {
                                            val oneObject: JSONObject = res?.getJSONObject(i)
                                            val posObj: JSONObject = oneObject.getJSONObject("geometry").getJSONObject("location")


                                            val namaTempat = oneObject.getString("name")
                                            var lokasiTarget = Location("")
                                            lokasiTarget.latitude = posObj.getDouble("lat")
                                            lokasiTarget.longitude = posObj.getDouble(("lng"))


                                            val jarak = lokasiUsaha.distanceTo(lokasiTarget)
                                            Log.w("nama tempat", namaTempat)
                                            Log.w("jarak", "${Math.round(jarak)}")
                                            val df = DecimalFormat("#.##")
                                            if(jarak/1000 <= 1.0) {
                                                var a = Tempat(namaTempat, "±${Math.round(jarak)}m", lokasiTarget)
                                                listTarget.add(a)
                                            }
                                        } catch (e: JSONException) {
                                            break
                                        }
                                    }
                                }
                            }
                        }

                        override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?, error: Throwable?) {

                        }
                    }

                    client.get(url, handler)
                    println("${t.trim()} : ${listTarget.size} : ")
                }
                tampilkanDaftarTargetPasar(listTarget)

            }
        }
    }
}

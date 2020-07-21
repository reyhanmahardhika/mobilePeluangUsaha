package com.example.myfristaop.peluangusaha

import android.location.Location
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.example.myfristaop.peluangusaha.adapter.TargetDanPesaingAdapter
import com.example.myfristaop.peluangusaha.adapter.Tempat
import com.example.myfristaop.peluangusaha.api.PeluangUsahaApi
import com.example.myfristaop.peluangusaha.model.UsahaTersimpanResponse
import com.example.myfristaop.peluangusaha.preferences.UserPreferences
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.SyncHttpClient
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_detail_usaha_tersimpan.*
import kotlinx.android.synthetic.main.dialog_map_layout.view.*
import kotlinx.android.synthetic.main.dialog_map_layout.view.mapsLayoutOnDialog
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.jetbrains.anko.doAsync
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


class DetailUsahaTersimpanActivity : AppCompatActivity() {

    lateinit var pos: LatLng
    lateinit var lokasiUsaha : Location
    val radius = 1000
    lateinit var dataTargetPasar: ArrayList<Tempat>
    lateinit var dataPesaing: ArrayList<Tempat>

    private lateinit var userPreferences : UserPreferences
    private val prefFileName = "DATAUSER"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_usaha_tersimpan)
        userPreferences = UserPreferences(this, prefFileName)

        val usaha = intent.getParcelableExtra<UsahaTersimpanResponse>(EXTRA_USAHA_TERSIMPAN)
        txtNamaDetailUsahaTersimpan.text = "Usaha ${usaha.nama_usaha}"
        txtModalDetailUsahaTersimpan.text = "Modal Rp.${usaha.modal}"
        txtDeskripsiDetailUsahaTersimpan.text = usaha.deskripsi_usaha
        txtBahanBakuDetailUsahaTersimpan.text = usaha.bahan_baku

        fabHapusDetailUsahaTersimpan.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)

            alertDialog.setMessage("Anda yakin ingin menghapus?")
            alertDialog.setCancelable(true)
            alertDialog.setPositiveButton("Ya") { dialog, id -> hapusUsahaTersimpan(usaha.id_usaha_tersimpan) }
            alertDialog.setNegativeButton("Tidak") { dialog, id -> dialog.cancel()}
            alertDialog.show()
        }
        pos = LatLng(usaha.latitude.toDouble(), usaha.longitude.toDouble())
        lokasiUsaha = Location("")
        lokasiUsaha.latitude = pos.latitude
        lokasiUsaha.longitude = pos.longitude

        CoroutineScope(IO).launch {
            ambilDataPesaing(pos, radius, usaha.nama_usaha)
            ambilDataTarget(pos, radius, usaha.target_pasar)
        }

        btnTampilkanTargetUsahaTersimpanDalamPeta.setOnClickListener {
            tampilkanTempatDalamMap("Peta Daftar Target Pasar", dataTargetPasar)
        }
        btnTampilkanPesaingUsahaTersimpanDalamPeta.setOnClickListener {
            tampilkanTempatDalamMap("Peta Daftar Pesaing", dataPesaing)
        }

    }
    private fun hapusUsahaTersimpan(id: String) {
        var retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        var peluangUsahaApi = retrofit.create(PeluangUsahaApi::class.java)
        doAsync {
            val token = userPreferences.token
            var call  =  peluangUsahaApi.hapusUsahaTersimpan(token, id)
            call.enqueue(object: Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("hapus usaha", "failed: " + t)
                }

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Toast.makeText(this@DetailUsahaTersimpanActivity,  "Usaha telah terhapus.", Toast.LENGTH_SHORT).show()
                    finish()
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
        MapsInitializer.initialize(this@DetailUsahaTersimpanActivity);
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
        withContext(Main) {
            dataTargetPasar = target
            val adapter = TargetDanPesaingAdapter(target)
            rvTargetPasarUsahaTersimpan.setHasFixedSize(true)
            rvTargetPasarUsahaTersimpan.layoutManager = LinearLayoutManager(this@DetailUsahaTersimpanActivity)
            rvTargetPasarUsahaTersimpan.adapter = adapter
            pbTargetDetailUsaha.visibility = View.INVISIBLE
            btnTampilkanTargetUsahaTersimpanDalamPeta.visibility = View.VISIBLE
        }
    }

    private suspend fun tampilkandaftarPesaing(pesaing: ArrayList<Tempat>) {
        withContext (Main) {
            pbPesaingDetailUsaha.visibility = View.INVISIBLE
            if(pesaing.size > 0) {
                dataPesaing = pesaing
                var adapter = TargetDanPesaingAdapter(pesaing)
                rvPesaingUsahaTersimpan.layoutManager = LinearLayoutManager(this@DetailUsahaTersimpanActivity)
                rvPesaingUsahaTersimpan.setHasFixedSize(true)
                rvPesaingUsahaTersimpan.adapter = adapter
                btnTampilkanPesaingUsahaTersimpanDalamPeta.visibility = View.VISIBLE
            } else {
                txtTidakAdaPesaingDetailUsaha.visibility = View.VISIBLE
                rvPesaingUsahaTersimpan.visibility= View.GONE
            }
        }
    }

    private suspend fun ambilDataPesaing(pos: LatLng, radius: Int, name: String) {
        withContext(IO) {
            async{

                println("debug: launching job1: ${Thread.currentThread().name}")
                var client = SyncHttpClient()
                var url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${pos.latitude},${pos.longitude}&radius=${radius}&name=${name}&key=${BuildConfig.MAP_KEY}"
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
        withContext(IO) {
            async {
                for (t in target) {
                    var client = SyncHttpClient()
                    var url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${pos.latitude},${pos.longitude}&radius=${radius}&name=${t.trim()}&key=${BuildConfig.MAP_KEY}"
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

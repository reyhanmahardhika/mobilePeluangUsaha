package com.example.myfristaop.peluangusaha

import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.example.myfristaop.peluangusaha.adapter.TargetDanPesaingAdapter
import com.example.myfristaop.peluangusaha.adapter.Tempat
import com.example.myfristaop.peluangusaha.model.UsahaTersimpanResponse
import com.google.android.gms.maps.model.LatLng

import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.SyncHttpClient
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_detail_usaha_tersimpan.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat


class DetailUsahaTersimpanActivity : AppCompatActivity() {

    lateinit var pos: LatLng
    lateinit var lokasiUsaha : Location
    val radius = 1000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_usaha_tersimpan)

        val usaha = intent.getParcelableExtra<UsahaTersimpanResponse>(EXTRA_USAHA_TERSIMPAN)

        txtNamaDetailUsahaTersimpan.text = "Usaha ${usaha.nama_usaha}"
        txtModalDetailUsahaTersimpan.text = "Modal Rp.${usaha.modal}"
        txtDeskripsiDetailUsahaTersimpan.text = usaha.deskripsi_usaha
        txtBahanBakuDetailUsahaTersimpan.text = usaha.bahan_baku

        fabHapusDetailUsahaTersimpan.setOnClickListener {
            Toast.makeText(this, "Fitur belum selesai" + usaha.nama_usaha, Toast.LENGTH_SHORT).show()
        }
        pos = LatLng(usaha.latitude.toDouble(), usaha.longitude.toDouble())
        lokasiUsaha = Location("")
        lokasiUsaha.latitude = pos.latitude
        lokasiUsaha.longitude = pos.longitude


        CoroutineScope(IO).launch {
            ambilDataPesaing(pos, radius, usaha.nama_usaha)
            ambilDataTarget(pos, radius, usaha.target_pasar)
        }


    }
    private suspend fun tampilkanDaftarTargetPasar(target: ArrayList<Tempat>) {
        withContext(Main) {
            val adapter = TargetDanPesaingAdapter(target)
            rvTargetPasarUsahaTersimpan.setHasFixedSize(true)
            rvTargetPasarUsahaTersimpan.layoutManager = LinearLayoutManager(this@DetailUsahaTersimpanActivity)
            rvTargetPasarUsahaTersimpan.adapter = adapter

        }
    }

    private suspend fun tampilkandaftarPesaing(pesaing: ArrayList<Tempat>) {
        withContext (Main) {
            var adapter = TargetDanPesaingAdapter(pesaing)
            rvPesaingUsahaTersimpan.layoutManager =LinearLayoutManager(this@DetailUsahaTersimpanActivity)
            rvPesaingUsahaTersimpan.setHasFixedSize(true)
            rvPesaingUsahaTersimpan.adapter = adapter
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
                                        val df = DecimalFormat("#,##")
                                        if(jarak/1000 <= 1.0) {
                                            var a = Tempat(namaTempat, "±${df.format(jarak)}m", lokasiPesaing)
                                            listPesaing.add(a)
                                        }
                                    } catch (e: JSONException) {
                                        break
                                    }
                                }
                            }

                        } else {
                            Log.w("results =========", "kosong result")
                        }
                        return
                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?, error: Throwable?) {

                        Log.d("------resp", "Failed" + statusCode.toString())
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
                                            val df = DecimalFormat("#,##")
                                            if(jarak/1000 <= 1.0) {
                                                var a = Tempat(namaTempat, "±${df.format(jarak)}m", lokasiTarget)
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
                            TODO("Not yet implemented")
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


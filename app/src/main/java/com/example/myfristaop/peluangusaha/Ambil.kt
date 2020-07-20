package com.example.myfristaop.peluangusaha

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import com.example.myfristaop.peluangusaha.adapter.Tempat
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.internal.t
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.SyncHttpClient
import cz.msebera.android.httpclient.Header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.*


class Ambil :MainActivity(){
    suspend fun Data(pos: LatLng, radius: Int, kataKunci: String,req : Int) {
        var jarakTerdekat =0
        var jarak =0
        var jumlah :Int =0
        var handlerSelesai = false

        withContext(Dispatchers.IO) {
            async {
                val client = SyncHttpClient()
                val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${pos.latitude},${pos.longitude}&radius=$radius&name=$kataKunci&key=${BuildConfig.MAP_KEY}"
                val charset = Charsets.UTF_8
                val handler = object : AsyncHttpResponseHandler() {
                    @SuppressLint("LongLogTag")
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                        if (responseBody != null) {
                            var hasil = JSONObject(responseBody.toString(charset))
                            var res: JSONArray? = hasil.getJSONArray("results")
                            if (res != null) {
                                for (i in 0 until res.length()) {
                                    try {
                                        val oneObject: JSONObject = res.getJSONObject(i)
                                        val posObj: JSONObject = oneObject.getJSONObject("geometry").getJSONObject("location")
                                        val namaTempat = oneObject.getString("name")
                                        var lokasiTarget = Location("")
                                        lokasiTarget.latitude = posObj.getDouble("lat")
                                        lokasiTarget.longitude = posObj.getDouble(("lng"))

                                        val lokasiUsaha : Location = Location("")
                                        lokasiUsaha.latitude = pos.latitude
                                        lokasiUsaha.longitude = pos.longitude

                                        jarak = lokasiUsaha.distanceTo(lokasiTarget).toInt()
                                        if(jarak <= 1000) {
                                            jumlah+=1
                                            if(i==0){jarakTerdekat = jarak}
                                            else if(jarak<jarakTerdekat){jarakTerdekat=jarak}
                                        }
                                    } catch (e: JSONException) {
                                        break
                                    }
                                }
                            }
                        }
                        if(req ==1) {
                            val data : String = ("$kataKunci,$jumlah,$jarakTerdekat")
                            InputData(data,req)
                        }
                        else if(req==2){
                            val data:String =("$kataKunci,$jumlah")
                            InputData(data,req)
                        }
                        handlerSelesai=true
                    }
                    override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?, error: Throwable?) {
                        TODO("Not yet implemented")
                    }

                }
                client.get(url, handler)

            }.await()
        }
    }
}



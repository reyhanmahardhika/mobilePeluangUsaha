package com.example.myfristaop.peluangusaha

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import com.example.myfristaop.peluangusaha.adapter.Tempat
import com.example.myfristaop.peluangusaha.api.PeluangUsahaApi
import com.example.myfristaop.peluangusaha.model.UsahaResponse
import com.example.myfristaop.peluangusaha.model.Wilayah
import com.example.myfristaop.peluangusaha.preferences.UserPreferences
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
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.text.DecimalFormat
import java.util.*


class Ambil :MainActivity(){

    suspend fun Data(pos: LatLng, radius: Int, kataKunci: String,req : Int) {

        withContext(Dispatchers.IO) {
            async {
                var jumlah = 0
                val client = SyncHttpClient()
                val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${pos.latitude},${pos.longitude}&radius=$radius&name=$kataKunci&key=${BuildConfig.MAP_KEY}"
                val charset = Charsets.UTF_8
                val handler = object : AsyncHttpResponseHandler() {
                    @SuppressLint("LongLogTag")
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                        if (responseBody != null) {
                            val hasil = JSONObject(responseBody.toString(charset))
                            val res: JSONArray? = hasil.getJSONArray("results")
                            if (res != null) {
                                var jarakTerdekat = 10000
                                var jarakSebelumnya : Int
                                for (i in 0 until res.length()) {
                                    try {
                                        val oneObject: JSONObject = res.getJSONObject(i)
                                        val posObj: JSONObject = oneObject.getJSONObject("geometry").getJSONObject("location")
                                        val namaTempat = oneObject.getString("name")
                                        val lokasiTarget = Location("")
                                        lokasiTarget.latitude = posObj.getDouble("lat")
                                        lokasiTarget.longitude = posObj.getDouble(("lng"))

                                        val lokasiUsaha : Location = Location("")
                                        lokasiUsaha.latitude = pos.latitude
                                        lokasiUsaha.longitude = pos.longitude

                                        jarakSebelumnya = lokasiUsaha.distanceTo(lokasiTarget).toInt()
                                        if(jarakSebelumnya <= 1000) {
                                            jumlah+=1
                                           if(jarakSebelumnya<jarakTerdekat)
                                                {jarakTerdekat=jarakSebelumnya}
                                        }
                                    } catch (e: JSONException) {
                                        break }
                                }
                                if(req ==1) {
                                    val data : String = ("$kataKunci,$jumlah,$jarakTerdekat")
                                    InputData(data,req)
                                }
                                else if(req==2){
                                    val data:String =("$kataKunci,$jumlah")
                                    InputData(data,req)
                                }
                            }
                        }
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



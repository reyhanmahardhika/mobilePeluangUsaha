package com.example.myfristaop.peluangusaha

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.SyncHttpClient
import cz.msebera.android.httpclient.Header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject



class Ambil :MainActivity(){

    suspend fun Data(pos: LatLng, radius: Int, kataKunci: String,req : Int) {

        withContext(Dispatchers.IO) {
            async {
                var jumlah = 0
                val client = SyncHttpClient()
                val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${pos.latitude},${pos.longitude}&radius=$radius&name=$kataKunci&key=AIzaSyCIzmKLdFHcim2FlP7e4FmVl-L4i7UlSNc"
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

                                        val lokasiUsaha = Location("")
                                        lokasiUsaha.latitude = pos.latitude
                                        lokasiUsaha.longitude = pos.longitude

//                                        jarakSebelumnya = lokasiUsaha.distanceTo(lokasiTarget).toInt()
                                        jarakSebelumnya = getDistance(lokasiUsaha, lokasiTarget).toInt()
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

    fun rad(x: Double): Double {
        return x * Math.PI /180
    }
    private fun getDistance(p1: Location, p2: Location): Double {
        var R = 6378137; // Earth’s mean radius in meter
        var dLat = rad(p2.latitude - p1.latitude)
        var dLong = rad(p2.longitude - p1.longitude)
        var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(rad(p1.latitude)) * Math.cos(rad(p2.latitude)) *
                Math.sin(dLong / 2) * Math.sin(dLong / 2)
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        var d = R * c;
        return d
    }


}



package com.example.myfristaop.peluangusaha

import android.os.AsyncTask
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONException
import org.json.JSONObject

class ParserTask(mMap: GoogleMap) : AsyncTask<String, Int, List<HashMap<String, String>>>(){
    val map = mMap
    override fun doInBackground(vararg params: String?): List<HashMap<String, String>>? {
        //Create json parser class
        val jsonParser : JsonParser = JsonParser()

        var mapList : List<HashMap<String,String>> = emptyList()
        var obj : JSONObject? =null
        try {
            obj = JSONObject(params [0])

            mapList=jsonParser.parseResult((obj))
        }
        catch (e : JSONException){
            e.printStackTrace()
        }
        return mapList
    }
    override fun onPostExecute(hashMaps : List<HashMap<String,String>>) {
        lateinit var marker : Marker
        for(i in 0..(hashMaps.size)-1){
            val hashMapList : HashMap<String,String> = hashMaps.get(i)

            //get data latitude, longtitude, name
            val lat : Double = hashMapList.get("lat")!!.toDouble()
            val lng : Double = hashMapList.get("lng")!!.toDouble()
            val name : String = hashMapList.get("name").toString()

            val latLng : LatLng = LatLng(lat,lng)

            marker = map.addMarker(MarkerOptions()
            .position(latLng).draggable(true).title(name))
            marker.isDraggable = false
        }
        marker.showInfoWindow()
    }
}
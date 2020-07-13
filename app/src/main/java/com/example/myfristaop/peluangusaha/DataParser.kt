package com.example.myfristaop.peluangusaha

import android.util.Log

import org.json.JSONException
import org.json.JSONObject

import java.util.HashMap
import org.json.JSONArray



/**
 * Created by kodetr on 01/04/19.
 */
class DataParser {

    private fun getPlace(googlePlaceJson: JSONObject): HashMap<String, String> {
        val googlePlaceMap = HashMap<String, String>()
        var placeName = "-NA-"
        var vicinity = "-NA-"
        var latitude = ""
        var longitude = ""
        var reference = ""

        Log.d("getPlace", "Entered")

        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name")
            }
            if (!googlePlaceJson.isNull("vicinity")) {
                vicinity = googlePlaceJson.getString("vicinity")
            }
            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat")
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng")
            reference = googlePlaceJson.getString("reference")
            googlePlaceMap["place_name"] = placeName
            googlePlaceMap["vicinity"] = vicinity
            googlePlaceMap["lat"] = latitude
            googlePlaceMap["lng"] = longitude
            googlePlaceMap["reference"] = reference
            Log.d("getPlace", "Putting Places")
        } catch (e: JSONException) {
            Log.d("getPlace", "Error")
            e.printStackTrace()
        }

        return googlePlaceMap
    }
//Fungsi ini digunakan untuk menambahkan fungsi untuk menampung data dari fungsi getPlace untuk ditampung didalam List
    private fun getPlaces(jsonArray: JSONArray): List<HashMap<String, String>> {
        val placesCount = jsonArray.length()
        val placesList = ArrayList<HashMap<String, String>>()
        var placeMap: HashMap<String, String>
        Log.d("Places", "getPlaces")

        for (i in 0 until placesCount) {
            try {
                placeMap = getPlace(jsonArray.get(i) as JSONObject)
                placesList.add(placeMap)
                Log.d("Places", "Adding places")

            } catch (e: JSONException) {
                Log.d("Places", "Error in Adding places")
                e.printStackTrace()
            }

        }
        return placesList
    }
//--------------------------------------------------------------------

    fun parse(jsonData: String): List<HashMap<String, String>> {

        var jsonObject :JSONObject = JSONObject(jsonData)
        var jsonArray = jsonObject.getJSONArray("results")

        try {
            Log.d("Places", "parse")
            jsonObject = JSONObject(jsonData)
            jsonArray = jsonObject.getJSONArray("results")
        } catch (e: JSONException) {
            Log.d("Places", "parse error")
            e.printStackTrace()
        }

        return getPlaces(jsonArray)
    }
}
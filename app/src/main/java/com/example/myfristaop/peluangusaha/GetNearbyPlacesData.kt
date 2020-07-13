package com.example.myfristaop.peluangusaha

import android.os.AsyncTask
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import java.util.HashMap

/**
 * Created by kodetr on 01/04/19.
 */
class GetNearbyPlacesData : AsyncTask<Any, String, String>() {

    internal lateinit var googlePlacesData: String
    internal lateinit var mMap: GoogleMap
    internal lateinit var url: String

    override fun doInBackground(vararg params: Any): String {
        try {
            mMap = params[0] as GoogleMap
            url = params[1] as String
            val downloadUrl = DownloadUrl()
            googlePlacesData = downloadUrl.readUrl(url)
        } catch (e: Exception) {
            Log.d("GooglePlacesReadTask", e.toString())
        }

        return googlePlacesData
    }

    override fun onPostExecute(result: String) {
        var nearbyPlacesList: List<HashMap<String, String>>? = null
        val dataParser = DataParser()
        nearbyPlacesList = dataParser.parse(result)
        ShowNearbyPlaces(nearbyPlacesList)
    }

    private fun ShowNearbyPlaces(nearbyPlacesList: List<HashMap<String, String>>) {
        for (i in nearbyPlacesList.indices) {
            val markerOptions = MarkerOptions()
            val googlePlace = nearbyPlacesList[i]
            val lat = java.lang.Double.parseDouble(googlePlace["lat"].toString())
            val lng = java.lang.Double.parseDouble(googlePlace["lng"].toString())
            val placeName = googlePlace["place_name"]
            val vicinity = googlePlace["vicinity"]
            val latLng = LatLng(lat, lng)

            markerOptions.position(latLng)
            markerOptions.title("$placeName : $vicinity")
            mMap.addMarker(markerOptions)
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15.7F))
        }
    }
}
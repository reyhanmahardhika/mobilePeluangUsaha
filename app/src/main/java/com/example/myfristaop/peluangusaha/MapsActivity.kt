package com.example.myfristaop.peluangusaha

import android.graphics.Camera
import android.location.Geocoder
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    var kelurahan=""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val toolbar = findViewById <Toolbar> (R.id.app_toolbar)
        setSupportActionBar(toolbar)
        toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_action_name)
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener { Toast.makeText(applicationContext,"Navigation icon was clicked",Toast.LENGTH_SHORT).show() }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        var markerAwal=0
        val medan = LatLng( 3.597031, 98.678513)

        val cu = CameraUpdateFactory.newLatLngZoom(medan,13F)
        mMap.animateCamera(cu)

        mMap.setOnMapClickListener(GoogleMap.OnMapClickListener { LatLng ->
            val position = LatLng

            if(markerAwal==0){
                addMarker(position)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,15.7F))
                markerAwal=1
            }
            else {
                mMap.clear()
                addMarker(position)
                val zoom =mMap.cameraPosition.zoom.toDouble()
                if(zoom<15.7) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,15.7F))
                }
                else{
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(position))
                }
            }
        })
    }
    private fun addMarker(latLng: LatLng) {
        val marker = mMap.addMarker(MarkerOptions()
                .position(latLng).draggable(true).title(getAddress(latLng)))
        marker.showInfoWindow()
        marker.isDraggable=false
        val circle: Circle = mMap.addCircle(CircleOptions().center(latLng).radius(500.0))
    }
    private fun getAddress(latLng: LatLng): String {
        val geocoder = Geocoder(this)
        val list = geocoder.getFromLocation(latLng.latitude,latLng.longitude, 1)
        kelurahan= list[0].subLocality

        txt_cari.setText(list[0].getAddressLine(0).toString())
        return kelurahan
    }



}

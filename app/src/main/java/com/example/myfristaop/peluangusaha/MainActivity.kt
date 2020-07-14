package com.example.myfristaop.peluangusaha

import android.location.Geocoder
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.compat.Place
import com.google.android.libraries.places.compat.ui.PlaceAutocompleteFragment
import com.google.android.libraries.places.compat.ui.PlaceSelectionListener
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.app_bar_navigation.*

import kotlinx.android.synthetic.main.activity_map.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private lateinit var nav: ActionBarDrawerToggle
    private lateinit var mMap: GoogleMap
    private var mapFragment: MapFragment = MapFragment()
    var kelurahan = ""
    var position: LatLng = LatLng(0.0, 0.0)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        mapFragment = getFragmentManager().findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this)

        val toolbar = findViewById<Toolbar>(R.id.app_toolbar)
        setSupportActionBar(toolbar)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_action_name)
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false)

        fab_myLocation.setOnClickListener { view ->
            Snackbar.make(view, "Fitur ini sedang dalam tahap pengerjaan", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        Toast.makeText(applicationContext, "Klik pada peta untuk menentukan lokasi usaha Anda!", Toast.LENGTH_LONG).show()
        mMap = googleMap
        val medan = LatLng(3.597031, 98.678513)
        val txt_alamat =
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment
        txt_alamat.setHint("Cari Lokasi Anda")


        val cu = CameraUpdateFactory.newLatLngZoom(medan, 13F)
        mMap.animateCamera(cu)

        mMap.setOnMapClickListener(GoogleMap.OnMapClickListener { LatLng ->
            position = LatLng

            mMap.clear()
            addMarker(position, txt_alamat)
            val zoom = mMap.cameraPosition.zoom.toDouble()
            if (zoom < 15.7) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.7F))
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(position))
            }
        })

        txt_alamat.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                try {
                    mMap.clear()
                    addMarker(place.getLatLng(), txt_alamat)
                    val zoom = mMap.cameraPosition.zoom.toDouble()
                    if (zoom < 15.7) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.7F))
                    } else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()))
                    }
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "Periksa koneksi jaringan Anda dan coba kembali!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(status: Status) {
                Toast.makeText(applicationContext, "Periksa koneksi jaringan Anda dan coba kembali!", Toast.LENGTH_SHORT).show()
            }
        })

    }


    private fun getUrl(latLng: LatLng, kataKunci: String, radius: Double): String {
        var googlePlacesUrl = ("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + position.latitude + "," + position.longitude + "&radius=" + radius + "&name=" + kataKunci + "&sensor=true&key=" + getString(R.string.google_maps_key))
        Log.d("getUrl", googlePlacesUrl)
        return (googlePlacesUrl)
    }

    private fun addMarker(latLng: LatLng, txt_alamat: PlaceAutocompleteFragment) {
        try {
            val marker = mMap.addMarker(MarkerOptions()
                    .position(latLng).draggable(true).title(getAddress(latLng, txt_alamat)))
            marker.showInfoWindow()
            marker.isDraggable = false
            val circle: Circle = mMap.addCircle(CircleOptions().center(latLng).radius(500.0))
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Periksa koneksi jaringan Anda dan coba kembali!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun getAddress(latLng: LatLng, txt_alamat: PlaceAutocompleteFragment): String {
        var alamat =""
        try{
            val geocoder = Geocoder(this)
            val list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            alamat = list[0].getAddressLine(0)
            kelurahan = list[0].subLocality
            txt_alamat.setText(alamat)

        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Periksa koneksi jaringan Anda dan coba kembali!", Toast.LENGTH_SHORT).show()
        }
        return alamat
    }

    /*-------------------- dari sini ke bawah adalah untuk menu navigation -------------------------*/

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.edit_profile -> {
                // Handle the camera action
            }
            R.id.usaha_tersimpan -> {

            }
            R.id.logout -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}

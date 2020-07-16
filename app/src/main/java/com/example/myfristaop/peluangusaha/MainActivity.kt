package com.example.myfristaop.peluangusaha


import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat

import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.example.myfristaop.peluangusaha.api.PeluangUsahaApi
import com.example.myfristaop.peluangusaha.model.UsahaResponse
import com.example.myfristaop.peluangusaha.model.Wilayah
import com.example.myfristaop.peluangusaha.preferences.UserPreferences

import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.compat.Place
import com.google.android.libraries.places.compat.ui.PlaceAutocompleteFragment
import com.google.android.libraries.places.compat.ui.PlaceSelectionListener
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.app_bar_navigation.*

import kotlinx.android.synthetic.main.activity_map.*
import org.jetbrains.anko.doAsync
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL


open class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private lateinit var nav: ActionBarDrawerToggle
    lateinit var mMap: GoogleMap
    private var mapFragment: MapFragment = MapFragment()
    lateinit var txt_alamat : PlaceAutocompleteFragment

    var kelurahan = ""
    private var position: LatLng = LatLng(0.0, 0.0)
    lateinit var user : FusedLocationProviderClient

    private lateinit var userPreferences : UserPreferences
    private val prefFileName = "DATAUSER"

    lateinit var retrofit: Retrofit
    lateinit var peluangUsahaApi: PeluangUsahaApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        // Sebelum lanjut cek apakah user sudah login

        userPreferences = UserPreferences(this, prefFileName)
        checkLogin()

        retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        peluangUsahaApi = retrofit.create(PeluangUsahaApi::class.java)

        // Mengambil kepadatan penduduk berdasarkan kecamatan
        // ambilKepadatanPenduduk("Binjai")

        // Mengambil semua usaha
        ambilSemuaUsaha()



        if(position== LatLng(0.0, 0.0)){ btn_cari.isEnabled=false}
        //inisialisasi text autocomplate alamat
        txt_alamat = fragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment

        mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this)



        val toolbar = findViewById<Toolbar>(R.id.app_toolbar)
        setSupportActionBar(toolbar)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_action_name)
        supportActionBar!!.setDisplayShowTitleEnabled(false)


        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        fab_myLocation.setOnClickListener { view ->
            try{
                user = LocationServices.getFusedLocationProviderClient(this)
                if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
                    getCurrentLocation()
                }
                else{
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),44)
                }
            }
            catch(e: Exception){Snackbar.make(view, "Gagal mendapatkan Lokasi pengguna", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()} }
        nav_view.setNavigationItemSelectedListener(this)
    }


    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val task: Task<Location> = user.lastLocation
            task.addOnSuccessListener(this) { location: Location? ->
                if (location == null) {
                } else location.apply {
                    position = LatLng(location.latitude, location.longitude)
                    addMarker(position, txt_alamat)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.count()>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getCurrentLocation()
        }
    }
    //dijalankan ketika map sudah berhasil di tampilkan
    override fun onMapReady(googleMap: GoogleMap) {
        Toast.makeText(applicationContext, "Klik pada peta untuk menentukan lokasi usaha Anda!", Toast.LENGTH_LONG).show()
        mMap = googleMap
        val medan = LatLng(3.597031, 98.678513)

        txt_alamat.setHint("Cari Lokasi Anda")


        val cu = CameraUpdateFactory.newLatLngZoom(medan, 13F)
        mMap.animateCamera(cu)

        mMap.setOnMapClickListener(GoogleMap.OnMapClickListener { LatLng ->
            position = LatLng
            addMarker(position, txt_alamat)

        })

        txt_alamat.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                mMap.clear()
                position=place.latLng
                addMarker(position, txt_alamat) }
            override fun onError(status: Status) {
                Log.d("error","Map gagal dimuat")
            }
        })

        btn_cari.setOnClickListener {
            try {
                if(position!= LatLng(0.0, 0.0)) {
                    val url: String = getUrl(position, txt_modal.text.toString(), 1000.0)
                    new_PlaceTask(mMap).execute(url)}
                 else{ android.widget.Toast.makeText(applicationContext, "Mohon tentukan lokasi usaha Anda terlebih dahulu ", android.widget.Toast.LENGTH_LONG).show() }
            }
            catch(e:Exception){
                Toast.makeText(applicationContext, "Harap tentukan lokasi usaha Anda!", Toast.LENGTH_LONG).show()
            }
        }

    }

    //digunakan untuk mengambil url untuk menjalankan place API by nearby search
    private fun getUrl(position : LatLng, kataKunci: String, radius: Double): String {
        var googlePlacesUrl=""
        try {
                googlePlacesUrl = ("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                        position.latitude + "," +
                        position.longitude + "&radius=" +
                        radius + "&name=Masjid&sensor=true&key=AIzaSyBeNiSKscMn5k2Ymh0FKA6Ubmt7weTjVMU")
                Log.d("getUrl", googlePlacesUrl)
                android.widget.Toast.makeText(applicationContext, "Google PlaceURL berhasil dimuat ", android.widget.Toast.LENGTH_LONG).show()
        }
        catch(e:Exception){
            android.widget.Toast.makeText(applicationContext, "Google PlaceURL gagal dimuat!! ", android.widget.Toast.LENGTH_LONG).show()
        }
        return (googlePlacesUrl)
    }

    private class new_PlaceTask(mMap: GoogleMap) : AsyncTask<String, Int, String>() {
        val map = mMap
        override fun doInBackground(vararg params: String?): String {
            var data :String = null.toString()
            try{
                data = downloadUrl(params[0])
            }catch (e: IOException){
                e.printStackTrace()
            }
            return data
        }

        override fun onPostExecute(s: String?) {
            ParserTask(map).execute(s)
        }

        @Throws(IOException::class)
        fun downloadUrl(string: String?): String {
            //initialize url
            val url : URL = URL(string)
            //initialize connection
            val connection : HttpURLConnection = url.openConnection( )as HttpURLConnection
            //Connect Connection
            connection.connect()
            //Initialize input stream
            val stream : InputStream = connection.inputStream
            //Initialize buffer reader
            val reader : BufferedReader = BufferedReader(InputStreamReader(stream))
            //Initaialize string builder
            val builder : StringBuilder = StringBuilder()
            var line :String?=null

            while({ line = reader.readLine(); line }() != null){
                builder.append(line)
            }
            //get append data
            val data : String = builder.toString()
            reader.close()

            return  data
        }

    }

    //digunakan untuk membuat marker
    private fun addMarker(latLng: LatLng, txt_alamat: PlaceAutocompleteFragment) {
        try {
            mMap.clear()
            val marker = mMap.addMarker(MarkerOptions()
                    .position(latLng).draggable(true).title(getAddress(latLng, txt_alamat)))
            marker.showInfoWindow()
            marker.isDraggable = false
            val circle: Circle = mMap.addCircle(CircleOptions().center(latLng).radius(1000.0))

            val zoom = mMap.cameraPosition.zoom.toDouble()
            if (zoom < 15.7) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.7F))
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(position))
            }

        }catch(e:Exception){Log.d("Error",e.toString())}
    }

    //digunakan untuk mendapatkan alamat dari lokasi usaha
    private fun getAddress(latLng: LatLng, txt_alamat: PlaceAutocompleteFragment): String {
        var alamat =""
        try{
            val geocoder = Geocoder(this)
            val list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            alamat = list[0].getAddressLine(0)
            kelurahan = list[0].subLocality
            txt_alamat.setText(alamat)

        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Gagal mendapatkan lokasi, Periksa koneksi jaringan Anda dan coba kembali!", Toast.LENGTH_LONG).show()
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
                startActivity(Intent(this, ProfilActivity::class.java))
            }
            R.id.usaha_tersimpan -> {
                startActivity(Intent(this, UsahaTersimpanActivity::class.java))
            }
            R.id.logout -> {

                startActivity(Intent(this, Login::class.java))
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun checkLogin() {
        Log.w("dari ------- main", userPreferences.token)

        if(userPreferences.token == "")
            startActivity(Intent(this@MainActivity, Login::class.java))
        else {
            showToast(userPreferences.email)
        }
    }
    fun ambilKepadatanPenduduk(kecamatan: String) {
        doAsync {
            val call : Call<Wilayah> =  peluangUsahaApi.getWilayah("Martubung")
            call.enqueue(object : Callback<Wilayah> {
                override fun onResponse(call: Call<Wilayah>, response: Response<Wilayah>) {
                    Log.d("b----------------------", response.body().toString())
                }

                override fun onFailure(call: Call<Wilayah>, t: Throwable) {
                    Log.d("b----------------------", t.toString())
                }
            })
        }

    }

    fun ambilSemuaUsaha() {
        doAsync {
            val token = userPreferences.token
            var call : Call<List<UsahaResponse>> =  peluangUsahaApi.ambilSemuaUsaha(token)
            call.enqueue(object : Callback<List<UsahaResponse>> {
                override fun onResponse(call: Call<List<UsahaResponse>>, response: Response<List<UsahaResponse>>) {
                    Log.d("Semua usaha -----------", response.body().toString())
                }

                override fun onFailure(call: Call<List<UsahaResponse>>, t: Throwable) {
                    Log.d("Semua usaha -----------", t.toString())
                }
            })
        }

    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

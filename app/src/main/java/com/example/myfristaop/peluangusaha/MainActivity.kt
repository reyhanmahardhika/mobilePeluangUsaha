package com.example.myfristaop.peluangusaha

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.myfristaop.peluangusaha.adapter.Tempat
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
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.compat.Place
import com.google.android.libraries.places.compat.ui.PlaceAutocompleteFragment
import com.google.android.libraries.places.compat.ui.PlaceSelectionListener
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.SyncHttpClient
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.activity_usaha_tersimpan.*
import kotlinx.android.synthetic.main.app_bar_navigation.*
import kotlinx.coroutines.*
import org.jetbrains.anko.AnkoAsyncContext
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat

const val EXTRA_REKOMENDASI_USAHA ="EXTRA_REKOMENDASI_USAHA"
open class MainActivity() : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private lateinit var nav: ActionBarDrawerToggle
    lateinit var mMap: GoogleMap
    private var mapFragment: MapFragment = MapFragment()
    lateinit var txt_alamat : PlaceAutocompleteFragment

    var kelurahan = ""
    var kota = ""
    private var position: LatLng = LatLng(0.0, 0.0)
    lateinit var user : FusedLocationProviderClient

    private lateinit var userPreferences : UserPreferences
    private val prefFileName = "DATAUSER"

    lateinit var retrofit: Retrofit
    lateinit var peluangUsahaApi : PeluangUsahaApi

    val df : DecimalFormat = DecimalFormat("#.###") //Decimal Format


    //--------- ini adalah variabel yang digunakan untuk perhitungan algoritma-----//
    var  tpadat :Double = 0.0
    var kpadat : Double = 0.0
    var cpadat : Double = 0.0
    var padat : Double = 0.0
    var spadat : Double = 0.0

    var tingkatKepadatanLokasi : String = "tidak padat"

    var cari_targetPasar : MutableList<String> = mutableListOf()
    var daftarUsaha : MutableList<String> = mutableListOf()

    companion object {
        var tLokasi : MutableList<String> = mutableListOf()
        var pesaingUsaha : MutableList<String> = mutableListOf()
        val V : Array<Array<String?>> = Array(pesaingUsaha.size,{ arrayOfNulls<String>(3)} )
    }

    var kepadatan_penduduk_lokasi : Double = 0.0

    //variabel usaha
    var usaha : List<UsahaResponse>? = null

    //variabel bobot kriteria
    val bobot1 : Double = 5.0
    val bobot2 : Double = 4.0
    val bobot3 : Double = 3.0
    val bobot4 : Double = 5.0
    val bobot5 : Double = 2.0

    //variabel hasil normalisasi(perbaikan bobot)
    var W1 : Double = (bobot1/(bobot1+bobot2+bobot3+bobot4+bobot5))
    var W2 : Double = (bobot2/(bobot1+bobot2+bobot3+bobot4+bobot5))
    var W3 : Double = (bobot3/(bobot1+bobot2+bobot3+bobot4+bobot5))
    var W4 : Double = (bobot4/(bobot1+bobot2+bobot3+bobot4+bobot5))
    var W5 : Double = (bobot5/(bobot1+bobot2+bobot3+bobot4+bobot5))

    //---------------------------------------------------------------------------------


    @SuppressLint("NewApi")
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

        ambilSemuaUsaha()

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
                    fab_myLocation.background.setTint(resources.getColor(R.color.colorPrimary))
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
    @SuppressLint("NewApi")
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
            fab_myLocation.background.setTint(resources.getColor(R.color.colorWhite))

        })

        txt_alamat.clearIcon.setOnClickListener(){
            mMap.clear()
            position= LatLng(0.0,0.0)
            txt_alamat.setText(null)
            fab_myLocation.background.setTint(resources.getColor(R.color.colorWhite))
        }
        txt_alamat.input.textSize=17.toFloat()

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
            if(position!=LatLng(0.0,0.0)) {
                if(txt_modal.text.toString()!=""){
                    if(kota == "Kota Medan"){
                        txt_modal.clearFocus()
                        CariRekomendasiUsaha()
                    }
                    else{showToast("Lokasi yang ditetapkan harus berada di kawasan Kota Medan!") }
                }
                else{showToast("Harap tentukan modal usaha yang Anda miliki terlebih dahulu!")
                }
            }
            else {showToast("Harap tentukan lokasi usaha Anda terlebih dahulu!")
            }
        }

    }

    //digunakan untuk membuat marker
    private fun addMarker(latLng: LatLng, txt_alamat: PlaceAutocompleteFragment) {
        try {
            mMap.clear()
            val icon = BitmapDescriptorFactory.fromResource(R.drawable.small_business_38px)
            val marker = mMap.addMarker(MarkerOptions()
                    .position(latLng).draggable(true).title("Lokasi Usaha Anda"))
            getAddress(position,txt_alamat)
            marker.setIcon(icon)
            marker.showInfoWindow()
            marker.isDraggable = false
            val circle: Circle = mMap.addCircle(CircleOptions().center(latLng).radius(1000.0).strokeWidth(2f))

            val zoom = mMap.cameraPosition.zoom.toDouble()
            if (zoom < 14.7) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 14.7F))
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(position))
            }
            showToast("Anda dapat zoom dan klik peta untuk menyesuai lokasi usaha Anda")

        }catch(e:Exception){Log.d("Error",e.toString())}
    }

    //digunakan untuk mendapatkan alamat dari lokasi usaha
    private fun getAddress(latLng: LatLng, txt_alamat: PlaceAutocompleteFragment): String {
        var alamat =""
        try{
            val geocoder = Geocoder(this)
            val list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            alamat = list[0].getAddressLine(0)
            kota = list[0].subAdminArea
            Log.d("Kota : ",kota)
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

    lateinit var alertDialog : AlertDialog.Builder

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
                alertDialog = AlertDialog.Builder(this)
                alertDialog.setMessage("Anda yakin ingin keluar?")
                alertDialog.setCancelable(true)
                alertDialog.setPositiveButton("Yes") { dialog, id -> logout() }
                alertDialog.setNegativeButton("Tidak") { dialog, id -> dialog.cancel()}
                alertDialog.show()

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    // Check apakah user sudah login (data user ada di userPreferences)
    fun checkLogin() {
        if(userPreferences.token == "")
            startActivity(Intent(this@MainActivity, Login::class.java))
        else {
            showToast(userPreferences.email)
        }
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    // Hapus akun dan logout
    fun logout() {
        userPreferences.clearValue()
        checkLogin()
    }

    /*-------------------------------------Ini adalah fungsi/method perhitungan algoritma SPK Peluang usaha-----------------------------------------*/

    fun ambilKepadatanPenduduk(kelurahan: String) {
        doAsync {
            val call : Call<Wilayah> =  peluangUsahaApi.getWilayah(kelurahan)
            call.enqueue(object : Callback<Wilayah> {
                override fun onResponse(call: Call<Wilayah>, response: Response<Wilayah>) {
                    var wilayah = response.body()
                    kepadatan_penduduk_lokasi = wilayah?.kepadatan_penduduk!!.toDouble()
                    hitungKepadatan(kepadatan_penduduk_lokasi)
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
            val call : Call<List<UsahaResponse>> =  peluangUsahaApi.ambilSemuaUsaha(token)
            call.enqueue(object : Callback<List<UsahaResponse>> {
                override fun onResponse(call: Call<List<UsahaResponse>>, response: Response<List<UsahaResponse>>) {
                    usaha = response.body()
                    //gabungkan target pasar dari  masing masing usaha menjadi 1 variabel
                    for(i in 0..((usaha!!.size)-1)){
                        val tmp_targetpasar = usaha!![i].target_pasar.toLowerCase().split(", ")
                        for(j in 0..((tmp_targetpasar.size)-1)){
                            if(i==0){ cari_targetPasar.add(tmp_targetpasar[j])}
                            else if(cari_targetPasar.indexOf(tmp_targetpasar[j]) == -1 ){
                                cari_targetPasar.add(tmp_targetpasar[j])
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<List<UsahaResponse>>, t: Throwable) {
                    Log.d("Semua usaha -----------", t.toString())
                }
            })
        }
    }

    fun hitungKepadatan(x : Double){

        if (x <= 3233) tpadat = 1.0
        else if (x in 3233.0..8288.75)
        {
            tpadat = ((8288.75 - x) / (8288.75 - 3233))
        }
        else tpadat = 0.0

        if (x <= 3233 || x >= 8288.75) kpadat = 0.0
        else if (x in 3233.0..8288.75)
        {
            kpadat = ((x - 3233) / (8288.75 - 3233))
        }
        else kpadat = ((13344.5 - x) / (13344.5 - 3233))

        if (x <= 8288.75 || x >= 18400.25) cpadat = 0.0
        else if (x in 8288.75..13344.5)
        {
            cpadat = ((x - 8288.75) / (13344.5 - 8288.75))
        }
        else cpadat = ((18400.25 - x) / (18400.25 - 8288.75))

        if (x <= 13344.5 || x >= 23456) padat = 0.0
        else if (x in 13344.5..18400.25)
        {
            padat = ((x - 13344.5) / (18400.25 - 13344.5))
        }
        else padat = ((23456 - x) / (23456 - 13344.5))

        if (x <= 18400.25) spadat = 0.0
        else if (x in 18400.25..23456.0)
        {
            spadat = ((x - 18400.25) / (23456 - 18400.25))
        }
        else spadat = 1.0


        if (kpadat >= tpadat) { tingkatKepadatanLokasi = "kurang padat"; }
        if (cpadat >= kpadat && cpadat >= tpadat) { tingkatKepadatanLokasi = "cukup padat"; }
        if (padat >= cpadat && padat >= kpadat && padat >=tpadat) { tingkatKepadatanLokasi = "padat"; }
        if (spadat >= padat && spadat >= cpadat && spadat >= kpadat && spadat>=tpadat) { tingkatKepadatanLokasi = "sangat padat"; }

    }

    @SuppressLint("LongLogTag", "ResourceAsColor")
    fun CariRekomendasiUsaha(){

        txt_prosesAnalisis.visibility= View.VISIBLE
        pbPerhitunganAlgoritma.visibility = View.VISIBLE
        layoutMainToolbar.isEnabled=false
        layoutMainbawah.isEnabled=false
        layoutMap.isEnabled=false

        val ambil :Ambil = Ambil()
        val modal : Int = txt_modal.text.toString().toInt()
        // Mengambil semua usaha

        Log.d("Banyaknya Usaha : ", ""+usaha!!.size)
        Log.d("Usaha --->> ", ""+usaha.toString())
        Log.d("Target pasar semua usaha",""+cari_targetPasar.toString())

        CoroutineScope(Dispatchers.IO).launch {
            ambilKepadatanPenduduk(kelurahan)
            Log.d("Kelurahan : ",kelurahan)
            println("Kepadatan penduduk = $tingkatKepadatanLokasi")

            for(i in 0..((cari_targetPasar.size)-1)){ //mencari data target pasar
                    ambil.Data(position,1000,cari_targetPasar[i],1) }
            Log.d("Target pasar lokasi",""+tLokasi.toString())


            for(i in 0..((usaha!!.size)-1)) {
                    ambil.Data(position, 1000, usaha!![i].nama_usaha, 2)  // 2 = request Pesaing
            }
            Log.d("Jumlah Pesaing pada lokasi",""+ pesaingUsaha.toString())

            //------Pembentukan Tabel 3.1.1.4 Nilai Preferensi Setiap Usaha--------
            val preferensi : Array<Array<String?>> = Array(usaha!!.size,{ arrayOfNulls<String>(7)} )

            println("\nTabel Nilai Preferensi Kriteria : ")
            println("\nAlternatif\tC1\tC2\tC3\tC4\tC5")
            for(i in 0..((usaha!!.size)-1)){
                var jlhTargetPasar = 0

                for(j in 0..6){
                    if(j==0){
                        preferensi[i][j]="U" + (i + 1).toString()
                    }
                    else if(j==1){
                        preferensi[i][j]=usaha!![i].nama_usaha
                    }
                    else if(j==2){
                        //nilai modal usaha(1/0) "1 = memenuhi" ; "0= tidak memenuhi"
                        if(usaha!![i].modal<=modal){
                            preferensi[i][j]="1" }
                        else{
                            preferensi[i][j]="0" }
                    }
                    else if(j==3){
                        //menghitung nilai jumlah target pasar
                        for(a in 0 until cari_targetPasar.size){
                            val targetpasar_usaha = usaha!![i].target_pasar.toLowerCase().split(", ")
                            val tmp = tLokasi[a].split(",")
                            for(b in 0 until targetpasar_usaha.size) {
                                if(tmp[0]==targetpasar_usaha[b]){
                                    jlhTargetPasar+= tmp[1].toInt()
                                    break
                                }
                            }
                        }
                        preferensi[i][j]= jlhTargetPasar.toString()
                    }
                    else if(j==4){
                        //Menghitung Jarak Target Pasar
                        var jarak = 0

                        for(a in 0 until cari_targetPasar.size){
                            val targetpasar_usaha = usaha!![i].target_pasar.toLowerCase().split(", ")
                            val tmp = tLokasi[a].split(",")
                            for(b in 0 until targetpasar_usaha.size) {
                                if(tmp[0]==targetpasar_usaha[b]){
                                    if(jarak==0){jarak = tmp[2].toInt()}
                                    else{
                                        if(jarak > tmp[2].toInt()){
                                            jarak = tmp[2].toInt()
                                        }
                                    }
                                    break
                                }
                            }
                        }
                        preferensi[i][j]= jarak.toString()
                    }
                    else if(j==5){
                        val preferensiKepadatan : Array<String> = arrayOf("tidak padat",
                                "kurang padat","cukup padat","padat","sangat padat")
                        var ramai = 1
                        var sepi = 5
                        for(a in 0 until preferensiKepadatan.size){
                            if(tingkatKepadatanLokasi==preferensiKepadatan[a]){
                                if(usaha!![i].kepadatan_penduduk.toString()=="1"){
                                    preferensi[i][j]=ramai.toString() }
                                else{
                                    preferensi[i][j]=sepi.toString() }
                            }
                            ramai += 1
                            sepi -= 1
                        }
                    }
                    else if(j==6){
                        for(a in 0 until usaha!!.size){
                            val tmp = pesaingUsaha[a].split(",")
                            preferensi[i][j]=tmp[1]
                        }
                    }
                    print(preferensi[i][j].toString()+"\t")
                }
                println()
            }
            println("\nHasil Perhitungan Nilai Vektor S")
            val S : Array<Array<String?>> = Array(pesaingUsaha.size,{ arrayOfNulls<String>(3)} )
            var totalNilaiS:Double=0.0

            for(i in 0 until usaha!!.size){
                var S_temp :Double =0.0
                S[i][0]="U"+(i+1).toString()
                S[i][1]=usaha!![i].nama_usaha.toString()
                if(preferensi[i][4]!= "0" ){
                S_temp =
                        (Math.pow((preferensi[i][2]!!.toDouble()),W1))*
                        (Math.pow((preferensi[i][3]!!.toDouble()),W2))*
                        (Math.pow((preferensi[i][4]!!.toDouble()),-W3))*
                        (Math.pow((preferensi[i][5]!!.toDouble()),W4))*
                        (Math.pow((preferensi[i][6]!!.toDouble()),-W5))
                }
                S[i][2]= (S_temp).toString()
                totalNilaiS += S_temp
                println("S${i+1} = ${S[i][2]}")
            }


            for(i in 0 until usaha!!.size){
                V[i][0]="U${i+1}"
                V[i][1]=usaha!![i].nama_usaha.toString()
                V[i][2]=(S[i][2]!!.toDouble()/totalNilaiS).toString()
                println("V${i+1} = ${V[i][2]}")
            }
            CoroutineScope(Dispatchers.Main).launch {
                txt_prosesAnalisis.visibility= View.INVISIBLE
                pbPerhitunganAlgoritma.visibility = View.INVISIBLE
                layoutMainToolbar.isClickable=true
                layoutMainbawah.isClickable=true
                layoutMap.isClickable=true

                val intent = Intent(this@MainActivity, RekomendasiUsaha::class.java)
                intent.putExtra("HASIL_REKOMENDASI", V)
                intent.putExtra("DATA_USAHA", "")
                startActivity(intent)

            }
        }

    }
    class InputData (data : String, req : Int) {
        init {
            if (req == 1) {
                tLokasi.add(data)
            } else if (req == 2) {
                pesaingUsaha.add(data)
            }
        }
    }
}



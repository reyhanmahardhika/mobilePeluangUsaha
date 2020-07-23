package com.example.myfristaop.peluangusaha

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.example.myfristaop.peluangusaha.api.PeluangUsahaApi
import com.example.myfristaop.peluangusaha.model.UsahaTersimpanResponse
import com.example.myfristaop.peluangusaha.preferences.UserPreferences
import kotlinx.android.synthetic.main.activity_profil.*
import kotlinx.android.synthetic.main.activity_profil.foto_profile
import org.jetbrains.anko.doAsync
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfilActivity : AppCompatActivity() {
    // Di dalam userPpreferences tersimapn data user dan juga token untuk mengakses web
    lateinit var userPreference: UserPreferences
    private val prefFileName = "DATAUSER"

    lateinit var retrofit: Retrofit
    lateinit var peluangUsahaApi: PeluangUsahaApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        peluangUsahaApi = retrofit.create(PeluangUsahaApi::class.java)

        userPreference = UserPreferences(this, prefFileName)

        ambilUsahaTersimpan()
        nickname.text = userPreference.nama
        email_pengguna.text = userPreference.email
        btn_edit_profile.setOnClickListener{
            val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Choose your profile picture")
            builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
                if (options[item] == "Take Photo") {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                        openCameraForImage()
                    else
                        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),0)
                }
                else if (options[item] == "Choose from Gallery") {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                        openGalleryForImage()
                    else
                        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
                } else if (options[item] == "Cancel") {
                    dialog.dismiss()
                }
            })
            builder.show()
        }
    }
    fun ambilUsahaTersimpan() {
        doAsync {
            val token = userPreference.token
            var call : Call<List<UsahaTersimpanResponse>> =  peluangUsahaApi.ambilUsahaTersimpan(token)
            call.enqueue(object : Callback<List<UsahaTersimpanResponse>> {
                override fun onResponse(call: Call<List<UsahaTersimpanResponse>>, response: Response<List<UsahaTersimpanResponse>>) {
                    val usahaTersimpan : List<UsahaTersimpanResponse>? =  response.body()
                    jumlahUsahaTersimpan.text=("${usahaTersimpan?.size} Usaha")
                }
                override fun onFailure(call: Call<List<UsahaTersimpanResponse>>, t: Throwable) {
                    Log.d("Semua usaha -----------", t.toString())
                }
            })
        }
    }

    private fun openCameraForImage(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 0)
    }
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0 -> if (resultCode === Activity.RESULT_OK) {
                val imageBitmap = data?.extras?.get("data") as Bitmap
                foto_profile.setImageBitmap(imageBitmap)
            }
            1 -> if (resultCode === Activity.RESULT_OK) {
                foto_profile.setImageURI(data?.data) // handle chosen image
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 0 && grantResults.count()>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            openCameraForImage()
        }
        else if(requestCode == 1 && grantResults.count()>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            openGalleryForImage()
        }
    }
}

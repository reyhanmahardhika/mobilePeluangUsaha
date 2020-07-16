package com.example.myfristaop.peluangusaha


import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_profile.*


class Profile : AppCompatActivity(){

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_profile)
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

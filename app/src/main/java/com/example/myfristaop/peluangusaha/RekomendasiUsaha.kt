package com.example.myfristaop.peluangusaha

import android.R.attr.*
import android.app.ActionBar
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_daftar_usaha.*
import kotlinx.android.synthetic.main.fragment_usaha.*
import kotlinx.android.synthetic.main.fragment_usaha.view.*


class RekomendasiUsaha : AppCompatActivity(){
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.fragment_daftar_usaha)
    for(i in 1..10) {//atur jumlah usaha
      var view: View = LayoutInflater.from(this).inflate(R.layout.fragment_usaha, null)
      view.nama_usaha.setText("Nama Usaha "+i.toString())
      view.usaha.setOnClickListener{
        Toast.makeText(this, " Nama Usaha "+i.toString(), Toast.LENGTH_SHORT).show()
        //detail usaha
      }
      view.remove_bookmark.setOnClickListener{
        view.remove_bookmark.visibility = View.GONE
        view.bookmark.visibility = View.VISIBLE
        //data usaha disimpan
      }
      view.bookmark.setOnClickListener{
        view.remove_bookmark.visibility = View.VISIBLE
        view.bookmark.visibility = View.GONE
        //data usaha tidak disimpan
      }
      layout_daftarUsaha.addView(view)

    }
  }
}


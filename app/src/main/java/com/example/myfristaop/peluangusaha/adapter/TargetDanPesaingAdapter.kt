package com.example.myfristaop.peluangusaha.adapter

import android.location.Location
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.myfristaop.peluangusaha.R
import com.google.android.gms.maps.model.LatLng

data class Tempat(val nama: String, val jarak: String, val pos: Location)
class TargetDanPesaingAdapter (private val list: ArrayList<Tempat>): RecyclerView.Adapter<TargetDanPesaingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_target_dan_pesaing_list, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.namaTempat.text = list[p1].nama
        p0.jarakTempat.text = list[p1].jarak
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaTempat: TextView = itemView.findViewById(R.id.txtNamaTempatItem)
        val jarakTempat: TextView = itemView.findViewById(R.id.txtJarakTempatItem)
    }
}
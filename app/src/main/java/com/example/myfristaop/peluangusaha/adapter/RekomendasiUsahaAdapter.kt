package com.example.myfristaop.peluangusaha.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.myfristaop.peluangusaha.R
import com.example.myfristaop.peluangusaha.model.UsahaResponse
import com.example.myfristaop.peluangusaha.model.VektorV

class RekomendasiUsahaAdapter (private val listVektor: List<VektorV>): RecyclerView.Adapter<RekomendasiUsahaAdapter.ViewHolder>() {
        private lateinit var onItemClickListener: OnItemClickListener

    fun setOnClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_usaha_list, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listVektor.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        ////        Glide.with(holder.itemView.context).load(listPlace[position].photo).apply(RequestOptions().override(350, 550)).into(holder.destinationPhoto)
        p0.nomorUsaha.text = (p1+1).toString()
        p0.namaUsaha.text = listVektor[p1].usaha.nama_usaha
        p0.modalUsaha.text = ("Modal : Rp${listVektor[p1].usaha.modal}")

        //p0.tomboHapusUsaha.setOnClickListener { onItemClickListener.onClickItem(listVektor[p1].usaha)}
        p0.itemView.setOnClickListener { onItemClickListener.onClickItem(listVektor[p1].usaha) }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val nomorUsaha : TextView= itemView.findViewById(R.id.nomorUsaha)
        val namaUsaha: TextView = itemView.findViewById(R.id.txtNamaUsaha)
        //val tomboHapusUsaha : ImageView = itemView.findViewById(R.id.imgHapusItemUsahaTersimpan)
        val modalUsaha : TextView = itemView.findViewById(R.id.txtModalUsaha)
    }

    interface OnItemClickListener {
        fun onClickItem(vektorV: UsahaResponse)
    }
}
//
//class UsahaTersimpanAdapter(private val listUsaha : ArrayList<UsahaTersimpanResponse>) : RecyclerView.Adapter<UsahaTersimpanAdapter.ViewHolder>() {
//
//    private lateinit var onItemClickListener: OnItemClickListener
//
//    fun setOnClickListener(onItemClickListener: OnItemClickListener) {
//        this.onItemClickListener = onItemClickListener
//    }
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usaha_list, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun getItemCount(): Int {
//        return listUsaha.size
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
////        Glide.with(holder.itemView.context).load(listPlace[position].photo).apply(RequestOptions().override(350, 550)).into(holder.destinationPhoto)
//        holder.namaUsaha.text = listUsaha[position].nama_usaha
//        holder.modalUsaha.text = ("Modal : Rp${listUsaha[position].modal}")
//        holder.itemView.setOnClickListener { onItemClickListener.onClickItem(listUsaha[position]) }
//    }
//
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val namaUsaha: TextView = itemView.findViewById(R.id.txtNamaUsahaTersimpan)
//        //        val tomboHapusUsaha : ImageView = itemView.findViewById(R.id.imgHapusItemUsahaTersimpan)
//        val modalUsaha : TextView = itemView.findViewById(R.id.txtModalUsahaTersimpan)
//    }
//
//    interface OnItemClickListener {
//        fun onClickItem(usaha : UsahaTersimpanResponse)
//    }
//}
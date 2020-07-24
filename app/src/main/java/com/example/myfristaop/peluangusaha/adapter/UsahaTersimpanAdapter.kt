package com.example.myfristaop.peluangusaha.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.myfristaop.peluangusaha.R
import com.example.myfristaop.peluangusaha.model.UsahaTersimpanResponse


class UsahaTersimpanAdapter(private val listUsaha : ArrayList<UsahaTersimpanResponse>) : RecyclerView.Adapter<UsahaTersimpanAdapter.ViewHolder>() {

    private lateinit var onItemClickListener: OnItemClickListener

    fun setOnClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usaha_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listUsaha.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView.context).load(listUsaha[position].gambar).apply(RequestOptions().centerCrop()).into(holder.imgUsaha)
        holder.nomorUsaha.text = (position+1).toString()
        holder.namaUsaha.text = listUsaha[position].nama_usaha
        holder.modalUsaha.text = ("Modal : Rp${listUsaha[position].modal}")
        holder.itemView.setOnClickListener { onItemClickListener.onClickItem(listUsaha[position]) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomorUsaha : TextView= itemView.findViewById(R.id.nomorUsaha)
        val namaUsaha: TextView = itemView.findViewById(R.id.txtNamaUsaha)
        val imgUsaha : ImageView = itemView.findViewById(R.id.imgUsaha)
        val modalUsaha : TextView = itemView.findViewById(R.id.txtModalUsaha)
    }

    interface OnItemClickListener {
        fun onClickItem(usaha : UsahaTersimpanResponse)
    }
}
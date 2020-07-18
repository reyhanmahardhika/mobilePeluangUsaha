package com.example.myfristaop.peluangusaha.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UsahaTersimpanResponse
(
        val id_usaha_tersimpan: String,
        val id_usaha: String,
        val id_pengguna: String,
        val id_wilayah: String,
        val latitude: String,
        val longitude: String,
        val nama_usaha: String,
        val jenis_usaha: String,
        val gambar: String,
        val modal: String,
        val deskripsi_usaha: String,
        val bahan_baku: String,
        val target_pasar: String,
        val kepadatan_penduduk: String
): Parcelable

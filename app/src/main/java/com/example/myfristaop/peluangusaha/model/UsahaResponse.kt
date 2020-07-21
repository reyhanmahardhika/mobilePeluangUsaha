package com.example.myfristaop.peluangusaha.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UsahaResponse(
        var id_usaha: String,
        var nama_usaha: String,
        var jenis_usaha: String,
        var gambar: String,
        var modal: Int,
        var deskripsi_usaha: String,
        var bahan_baku: String,
        var target_pasar: String,
        var kepadatan_penduduk: String
): Parcelable

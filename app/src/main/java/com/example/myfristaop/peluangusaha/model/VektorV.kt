package com.example.myfristaop.peluangusaha.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VektorV(val namaVektor: String, val idUsaha: String, val nilaiVektor: Double, val usaha: UsahaResponse): Parcelable
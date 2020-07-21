package com.example.myfristaop.peluangusaha.api

import com.example.myfristaop.peluangusaha.model.*
import retrofit2.Call
import retrofit2.http.*


interface PeluangUsahaApi {

    @GET("wilayah/ambilSatuWilayah")
    fun getWilayah(@Query("kelurahan") kelurahan:String ) : Call<Wilayah>

    @POST("auth/login")
    fun login(@Body user: User) : Call<UserResponse>

    @POST("auth/mendaftar")
    fun register (@Body register: Register): Call<UserResponse>

    @GET("usaha/ambilUsahaTersimpan")
    fun ambilUsahaTersimpan(@Header ("token") token: String): Call<List<UsahaTersimpanResponse>>

    @GET("usaha/ambilUsaha")
    fun ambilSemuaUsaha(@Header ("token") token: String) : Call<List<UsahaResponse>>

    @DELETE("usaha/hapusUsahaTersimpanById/{id_usaha}")
    fun hapusUsahaTersimpan(@Header ("token") token: String, @Path("id_usaha") id_usaha: String): Call<Void>

}
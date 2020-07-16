package com.example.myfristaop.peluangusaha

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.myfristaop.peluangusaha.preferences.UserPreferences
import kotlinx.android.synthetic.main.activity_profil.*

class ProfilActivity : AppCompatActivity() {
    // Di dalam userPpreferences tersimapn data user dan juga token untuk mengakses web
    lateinit var userPreference: UserPreferences
    private val prefFileName = "DATAUSER"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        userPreference = UserPreferences(this, prefFileName)
        txtEmail.text = userPreference.email
        txtNama.text = userPreference.nama
        txtToken.text = userPreference.token
    }
}

package com.example.myfristaop.peluangusaha

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils

import android.widget.Toast
import com.example.myfristaop.peluangusaha.api.PeluangUsahaApi
import com.example.myfristaop.peluangusaha.model.User
import com.example.myfristaop.peluangusaha.model.UserResponse
import com.example.myfristaop.peluangusaha.preferences.UserPreferences

import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.util.regex.Matcher
import java.util.regex.Pattern


class Login : AppCompatActivity(){
    private lateinit var userPreferences : UserPreferences
    private val prefFileName = "DATAUSER"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        userPreferences = UserPreferences(this, prefFileName)

        val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val peluangUsahaApi = retrofit.create(PeluangUsahaApi::class.java)


        btn_login?.setOnClickListener{
//            if(emailLogin.text.toString().isEmailValid() && passwordLogin.text.toString().isPasswordValid()){
//                Toast.makeText(this, "Valid", Toast.LENGTH_SHORT).show()
//                intent = Intent(this,MainActivity::class.java)
//                startActivity(intent)
//            }
//            else
//                Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show()

            var user = User(emailLogin.text.toString(), passwordLogin.text.toString())

            val call =  peluangUsahaApi.login(user)
            call.enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    val userResponse = response.body()
                    userPreferences.id = userResponse?.id_pengguna ?: ""
                    userPreferences.nama = userResponse?.nama_pengguna ?: ""
                    userPreferences.email = userResponse?.email ?: ""
                    userPreferences.token = userResponse?.token ?: ""
                    this@Login.finish()
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Toast.makeText(this@Login, "Maaf anda belum bisa login. Coba lagi nanti.", Toast.LENGTH_SHORT).show()
                }
            })
        }
        mendaftar?.setOnClickListener {
            intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    private fun String.isPasswordValid(): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=\\S+$).{8,20}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(this)
        return matcher.matches()
    }
}

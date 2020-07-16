package com.example.myfristaop.peluangusaha

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.myfristaop.peluangusaha.api.PeluangUsahaApi
import com.example.myfristaop.peluangusaha.model.Register
import com.example.myfristaop.peluangusaha.model.UserResponse
import com.example.myfristaop.peluangusaha.preferences.UserPreferences
import kotlinx.android.synthetic.main.activity_register.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.regex.Matcher
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity(){
  lateinit var userPreferences : UserPreferences
  private val prefFileName = "DATAUSER"
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_register)

    userPreferences = UserPreferences(this, prefFileName)
    btn_register.setOnClickListener{

      val retrofit: Retrofit = Retrofit.Builder()
              .baseUrl(BuildConfig.BASE_URL)
              .addConverterFactory(GsonConverterFactory.create())
              .build()

      var register = Register(userNameRegister.text.toString(), emailRegister.text.toString(), passwordRegister.text.toString())
      val peluangUsahaApi = retrofit.create(PeluangUsahaApi::class.java)
      val call = peluangUsahaApi.register(register)
      call.enqueue(object : Callback<UserResponse> {

        override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
          val userResponse = response.body()
          userPreferences.id = userResponse?.id_pengguna ?: ""
          userPreferences.nama = userResponse?.nama_pengguna ?: ""
          userPreferences.email = userResponse?.email ?: ""
          userPreferences.token = userResponse?.token ?: ""
          startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
        }
        override fun onFailure(call: Call<UserResponse>, t: Throwable) {
         showToast("Maaf anda belum bisa login. Coba lagi nanti.")
        }
      })

//      if(usernameRegister.text.toString().length >= 4 && emailRegister.text.toString().isEmailValid() && passwordRegister.text.toString().isPasswordValid() && confirmPassword.text.toString() == passwordRegister.text.toString()) {
//        Toast.makeText(this, "Valid", Toast.LENGTH_SHORT).show()
//      }
//      else
//        Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show()


    }
    masuk?.setOnClickListener {
      intent = Intent(this,Login::class.java)
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

  fun showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
  }
}


package com.example.myfristaop.peluangusaha

import android.R.attr.password
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.util.regex.Matcher
import java.util.regex.Pattern


class Login : AppCompatActivity(){
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)
    val emailLogin = findViewById<EditText>(R.id.emailLogin)
    val passwordLogin = findViewById<EditText>(R.id.passwordLogin)
    val btn_login = findViewById<Button>(R.id.btn_login);
    val mendaftar = findViewById<TextView>(R.id.mendaftar)
    btn_login?.setOnClickListener{
      if(emailLogin.text.toString().isEmailValid() && passwordLogin.text.toString().isPasswordValid())
        Toast.makeText(this, "Valid", Toast.LENGTH_SHORT).show()
      else
        Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show()
    }
    mendaftar?.setOnClickListener {
      intent = Intent(this,Register::class.java)
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

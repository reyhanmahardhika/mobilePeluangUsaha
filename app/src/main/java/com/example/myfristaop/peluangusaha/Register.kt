package com.example.myfristaop.peluangusaha

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

class Register : AppCompatActivity(){
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_register)
    val usernameRegister = findViewById<EditText>(R.id.usernameRegister)
    val emailRegister = findViewById<EditText>(R.id.emailRegister)
    val passwordRegister = findViewById<EditText>(R.id.passwordRegister)
    val confirmPassword = findViewById<EditText>(R.id.confirmPasswordRegister)
    val btn_Register = findViewById<Button>(R.id.btn_register);
    val masuk = findViewById<TextView>(R.id.masuk)
    btn_Register?.setOnClickListener{
      if(usernameRegister.text.toString().length >= 4 && emailRegister.text.toString().isEmailValid() && passwordRegister.text.toString().isPasswordValid() && confirmPassword.text.toString() == passwordRegister.text.toString()) {
        Toast.makeText(this, "Valid", Toast.LENGTH_SHORT).show()
      }
      else
        Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show()
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
}


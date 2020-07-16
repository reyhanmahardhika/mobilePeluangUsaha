package com.example.myfristaop.peluangusaha.preferences

import android.content.Context
import android.content.SharedPreferences


class UserPreferences(context: Context, name: String) {
    val USER_ID = "id_user"
    val USER_NAME = "nama_user"
    val USER_EMAIL = "email"
    val USER_TOKEN = "token"

    private var myPreferences : SharedPreferences

    init {
        myPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }
    inline fun SharedPreferences.editMe(operation: (SharedPreferences.Editor) -> Unit) {
        val editMe = edit()
        operation(editMe)
        //editMe.apply()
         editMe.commit()
    }
    var id: String
        get() = myPreferences.getString(USER_ID, "")
        set(value) {
            myPreferences.editMe {
                it.putString(USER_ID, value)
            }
        }
    var nama: String
        get()  = myPreferences.getString(USER_NAME, "")
        set(value) {
            myPreferences.editMe {
                it.putString(USER_NAME, value)
            }
        }
    var email: String
        get() = myPreferences.getString(USER_EMAIL, "")
        set(value) {
            myPreferences.editMe {
                it.putString(USER_EMAIL, value)
            }
        }

    var token: String
        get() = myPreferences.getString(USER_TOKEN, "")
        set(value) {
            myPreferences.editMe {
                it.putString(USER_TOKEN, value)
            }
        }

    fun clearValue() {
        myPreferences.edit().clear().commit()
    }

}
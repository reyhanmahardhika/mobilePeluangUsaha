package com.example.myfristaop.peluangusaha

import android.provider.ContactsContract
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class JsonParser{

    private fun parseJsonObject(obj :JSONObject): HashMap<String, String>{
        val datalist : HashMap<String,String> = HashMap()
        try {
            val name : String = obj.getString("name")

            //mengambil Latitude and Longtitude
            val latitude : String = obj.getJSONObject("geometry").getJSONObject("location").getString("lat")
            val longtitude : String = obj.getJSONObject("geometry").getJSONObject("location").getString("lng")

            //Menaruh semua ke dalam HashMap
            datalist.put("name",name)
            datalist.put("lat",latitude)
            datalist.put("lng",longtitude)

        }
        catch (e: JSONException){
            e.printStackTrace()
        }

        //mengembalikan hashmap
        return datalist
    }

    private fun parseJsonArray(jsonArray : JSONArray) :List<HashMap<String,String>>{
        val dataList:MutableList <HashMap<String,String>> = ArrayList()

        for (i in 0..(jsonArray.length())-1) {
            try {
                val data: HashMap<String, String> = parseJsonObject(jsonArray.get(i) as JSONObject)

                dataList.add(data)
            }
            catch(e : JSONException){
                e.printStackTrace()
            }
        }
        return dataList
    }

    fun parseResult(obj: JSONObject) : List<HashMap<String,String>>{
        var jsonArray : JSONArray? = null
        try {
            jsonArray=obj.getJSONArray("results")
        }
        catch(e : JSONException){
            e.printStackTrace()
        }
        return parseJsonArray(jsonArray as JSONArray)
    }
}
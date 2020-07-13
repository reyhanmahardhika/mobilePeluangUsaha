package com.example.myfristaop.peluangusaha

import android.util.Log

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by kodetr on 01/04/19.
 */
class DownloadUrl {

    @Throws(IOException::class)
    fun readUrl(strUrl: String): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()
            iStream = urlConnection.inputStream

            val br = BufferedReader(InputStreamReader(iStream!!))

            val sb = StringBuffer()
            var line = ""
            while (br.readLine() != null) {
                line=br.readLine()
                sb.append(line)
            }

            data = sb.toString()
            Log.d("downloadUrl", data)
            br.close()

        } catch (e: Exception) {
            Log.d("Exception", e.toString())
        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }
        return data
    }
}
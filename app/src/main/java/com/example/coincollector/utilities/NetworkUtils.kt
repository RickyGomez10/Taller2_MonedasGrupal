package com.example.coincollector.utilities

import android.net.Uri
import android.util.Log
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*

object NetworkUtils {

    val COINS_API_BASE_URL = "https://coin-app.herokuapp.com/country/"

    private val TAG = NetworkUtils::class.java.simpleName

    fun buildUrl(countryName: String): URL? {
        val builtUri = Uri.parse(COINS_API_BASE_URL)
            .buildUpon()
            .appendPath(countryName)
            .build()

        var url: URL? = null

        try {
            url = URL(builtUri.toString())
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        Log.d(TAG, "Built URI " + url!!)

        return url
    }

    @Throws(IOException::class)
    fun getResponseFromHttpUrl(url: URL): String? {
        val urlConnection = url.openConnection() as HttpURLConnection
        try {
            val ins = urlConnection.inputStream

            val scanner = Scanner(ins)
            scanner.useDelimiter("\\A")

            val hasInput = scanner.hasNext()
            if (hasInput) {
                return scanner.next()
            } else {
                return null
            }
        } finally {
            urlConnection.disconnect()
        }
    }

}
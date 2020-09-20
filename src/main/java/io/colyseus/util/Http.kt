package io.colyseus.util

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object Http {
    private const val HTTP_CONNECT_TIMEOUT = 10000
    private const val HTTP_READ_TIMEOUT = 10000
    @JvmOverloads
    @Throws(IOException::class, HttpException::class)
    fun request(url: String?, method: String? = "GET", httpHeaders: MutableMap<String, String>? = null, body: String? = null): String {
//        System.out.println("sending http request to server...")
//        System.out.println("url is " + url)
//        System.out.println("http request body is " + body)
//        System.out.println("http request method is " + method)
        val con = URL(url).openConnection() as HttpURLConnection
        con.requestMethod = method
        if (httpHeaders != null) {
            for ((key, value) in httpHeaders) {
                con.setRequestProperty(key, value)
            }
        }
        con.connectTimeout = HTTP_CONNECT_TIMEOUT
        con.readTimeout = HTTP_READ_TIMEOUT
        if (body != null) {
            con.doOutput = true
            val os = con.outputStream
            val input = body.toByteArray(StandardCharsets.UTF_8)
            os.write(input, 0, input.size)
        }
        val code = con.responseCode
        //        System.out.println("http response code is " + code)
        val inputStream: InputStream = if (code != HttpURLConnection.HTTP_OK) con.errorStream else con.inputStream
        val br = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
        val sb = StringBuilder()
        var responseLine: String?
        while (br.readLine().also { responseLine = it } != null) {
            sb.append(responseLine!!.trim { it <= ' ' })
        }
        val response = sb.toString()
        if (code != HttpURLConnection.HTTP_OK) {
            throw HttpException(response, code)
        }
        return response
    }

    class HttpException internal constructor(response: String?, var code: Int) : Exception(response)
}
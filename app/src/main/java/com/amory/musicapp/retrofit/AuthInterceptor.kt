package com.amory.musicapp.retrofit

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context:Context): Interceptor {
    private var token : String ?=null
    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("SAVE_TOKEN",Context.MODE_PRIVATE)

    override fun intercept(chain: Interceptor.Chain): Response {
        /*token = sharedPreferences.getString("token","")*/
        token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJuRk1nWHIxaVByanBRR1VXbzVHWGxGNkV3VHdWd3oyNU1Zc25wVUFmSnNnIn0.eyJleHAiOjE3MTc4OTExNTQsImlhdCI6MTcxNzg1NTE1NSwiYXV0aF90aW1lIjoxNzE3ODU1MTU0LCJqdGkiOiI0NDhjMzgyNC01NWY4LTRhY2YtYWE2OS05MTkzN2JjNDliYTgiLCJpc3MiOiJodHRwczovL2F1dGguYmVhdGJ1ZGR5LmlvLnZuL3JlYWxtcy9iZWF0YnVkZHkiLCJzdWIiOiJiZTcwNGNiMC0zNWMyLTRiYWYtODNiMC00M2M4MWQ3ZDhiYmQiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJ3ZWIiLCJub25jZSI6ImRnMXpqQzlLcElvOSIsInNlc3Npb25fc3RhdGUiOiI2OGZmOTI1OC02ZGY0LTQ5NDctOWIxNy01YTEzOGQyMTQzZTEiLCJyZXNvdXJjZV9hY2Nlc3MiOnsicmVzb3VyY2UtbWFuYWdlbWVudCI6eyJyb2xlcyI6WyJ1c2VyIl19fSwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSIsInNpZCI6IjY4ZmY5MjU4LTZkZjQtNDk0Ny05YjE3LTVhMTM4ZDIxNDNlMSJ9.I63QbcJ5USsOO68mPMj-0q8csnHND0TxGAGGITRThtofk7N-Rrr57EuwfWN-evMrfaxvCgLuHek_YknA4oj4C1FwECBauElN6kaUyfrvdFe3nYcgYWcI9KQJCuFrq8iav_M3bNf8Cm8jmAr7Ej3XUsD3HVXUV3iILn4zjsll3cew_2ozUwmLf50gs1LTe6DcnK5yV_hqDhk3vbIVeb2y-cbpp9s36e2yoM4V6x232cHR3DE56kvUbClw6TqtlX3W643ljkrfpWbfly1t6ge4d95zYnRbjz1gh5s-rFETdCSppPRreIM7Ryld_LFnr7A5_Pa6HWdFUAP9X5V8hCAL1Q"
        val request = chain.request()
        val builder = request.newBuilder()
        builder.addHeader("Authorization", "Bearer $token")
        val response = chain.proceed(builder.build())
        if (response.isSuccessful){
            return response
        }
        return response
    }

}
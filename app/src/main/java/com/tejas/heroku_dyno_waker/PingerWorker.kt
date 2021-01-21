package com.tejas.heroku_dyno_waker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.net.HttpURLConnection
import java.net.URL

class PingerWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    val TAG: String = this.javaClass.simpleName
    override fun doWork(): Result {
        val url: String? = inputData.getString("URL")
        var httpURLConnection: HttpURLConnection? = null
        try {
            val httpUrl: URL = URL(url)
            httpURLConnection = httpUrl.openConnection() as HttpURLConnection
            httpURLConnection.connectTimeout = 30000
        } catch (e: Exception) {
            Log.i(TAG, e.message)
            httpURLConnection?.disconnect()
            Result.retry()
        } finally {
            httpURLConnection?.disconnect()
        }
        return Result.success()
    }
}
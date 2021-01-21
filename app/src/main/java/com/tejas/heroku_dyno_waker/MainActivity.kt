package com.tejas.heroku_dyno_waker

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.tejas.heroku_dyno_waker.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val TAG: String = this.javaClass.name

    private lateinit var binding: ActivityMainBinding
    private var toast: Toast? = null
    private var mSharedPreferences: SharedPreferences? = null
    private lateinit var mSharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toast = Toast(applicationContext)
        mSharedPreferences = getPreferences(MODE_PRIVATE)
        mSharedPreferencesEditor = mSharedPreferences?.edit()!!
    }


    override fun onStart() {
        super.onStart()
        setAdapter()
    }

    fun startWorker(view: View) {
        var url: String? = binding.edittextAppurl.text.toString().trim()
        val workerName: String? = binding.edittextWorkername.text.toString().trim()

        if (url.isNullOrBlank() || workerName.isNullOrBlank()) return

//        if(mSharedPreferences?.getString(workerName,"").equals("")) return

        url = url.removeSuffix("/")
        val inputData: Data = workDataOf("URL" to url)
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

        val workerRequest = PeriodicWorkRequestBuilder<PingerWorker>(20, TimeUnit.MINUTES)
                .setInputData(inputData)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(workerName, ExistingPeriodicWorkPolicy.REPLACE, workerRequest)
        mSharedPreferencesEditor.putString(workerName, "${workerRequest.id},${url}")
        mSharedPreferencesEditor.commit()
        setAdapter()
        Toast.makeText(applicationContext, "Pinger Has Started..\n It will ping ${url} after every 20-25     Minutes", Toast.LENGTH_LONG).show()
    }

    private fun setAdapter() {
        val workerNames: MutableList<WorkerInfo>? = mSharedPreferences?.all?.map { WorkerInfo.fromString(it.key, it.value as String) }?.toMutableList()

        if (workerNames != null && workerNames.size > 0) {
            val workerAdapter: WorkerAdapter = WorkerAdapter(workerNames, applicationContext, mSharedPreferencesEditor)
            binding.listWorkers.adapter = workerAdapter
        }
    }
}
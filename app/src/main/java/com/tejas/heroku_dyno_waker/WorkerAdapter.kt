package com.tejas.heroku_dyno_waker

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import java.util.concurrent.TimeUnit

class WorkerAdapter(private var dataSet: MutableList<WorkerInfo>, val context: Context, val editor: SharedPreferences.Editor) : RecyclerView.Adapter<WorkerAdapter.ViewHolder>() {
    val mWorkManager: WorkManager

    init {
        mWorkManager = WorkManager.getInstance(context)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView_name: TextView
        val textView_app_url: TextView
        val editText_url: EditText
        val btn_edit: AppCompatImageButton
        val btn_save: AppCompatImageButton
        val btn_delete: AppCompatImageButton
        val btn_cancel_edit: AppCompatImageButton

        init {
            textView_name = view.findViewById(R.id.worker_name)
            textView_app_url = view.findViewById(R.id.label_app_url)
            editText_url = view.findViewById(R.id.url_edittext)
            btn_edit = view.findViewById(R.id.btn_edit)
            btn_save = view.findViewById(R.id.btn_save)
            btn_delete = view.findViewById(R.id.btn_delete)
            btn_cancel_edit = view.findViewById(R.id.btn_cancel_edit)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_worker, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workerInfo: WorkerInfo = dataSet[position]
        holder.textView_name.text = workerInfo.name
        holder.textView_app_url.text = workerInfo.url
        holder.editText_url.setText(workerInfo.url)

        holder.btn_edit.setOnClickListener {
            holder.textView_app_url.visibility = View.GONE
            holder.editText_url.visibility = View.VISIBLE

            holder.btn_edit.visibility = View.GONE
            holder.btn_delete.visibility = View.GONE

            holder.btn_save.visibility = View.VISIBLE
            holder.btn_cancel_edit.visibility = View.VISIBLE

//            dataSet = dataSet.filter { !it.equals(workerInfo) }
//            notifyDataSetChanged()
        }

        holder.btn_cancel_edit.setOnClickListener {
            holder.textView_app_url.visibility = View.VISIBLE
            holder.editText_url.visibility = View.GONE
            holder.editText_url.setText(workerInfo.url)

            holder.btn_edit.visibility = View.VISIBLE
            holder.btn_delete.visibility = View.VISIBLE

            holder.btn_save.visibility = View.GONE
            holder.btn_cancel_edit.visibility = View.GONE
        }

        holder.btn_save.setOnClickListener {
            val url: String? = holder.editText_url.text.toString().trim()
            if (url.isNullOrBlank() || url.equals(workerInfo.url)) {
                holder.textView_app_url.visibility = View.VISIBLE
                holder.editText_url.visibility = View.GONE
                holder.editText_url.setText(workerInfo.url)
                holder.btn_edit.visibility = View.VISIBLE
                holder.btn_delete.visibility = View.VISIBLE
                holder.btn_save.visibility = View.GONE
                holder.btn_cancel_edit.visibility = View.GONE
                return@setOnClickListener
            }

            val inputData: Data = workDataOf("URL" to url)
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()

            val workerRequest = PeriodicWorkRequestBuilder<PingerWorker>(25, TimeUnit.MINUTES)
                    .setInputData(inputData)
                    .setConstraints(constraints)
                    .build()

            mWorkManager.enqueueUniquePeriodicWork(workerInfo.name, ExistingPeriodicWorkPolicy.REPLACE, workerRequest)

            workerInfo.id = workerRequest.id.toString()
            workerInfo.url = url
            editor.putString(workerInfo.name, "${workerRequest.id},${url}").commit()

            holder.textView_app_url.visibility = View.VISIBLE
            holder.editText_url.visibility = View.GONE
            holder.editText_url.setText(workerInfo.url)

            holder.btn_edit.visibility = View.VISIBLE
            holder.btn_delete.visibility = View.VISIBLE

            holder.btn_save.visibility = View.GONE
            holder.btn_cancel_edit.visibility = View.GONE
            dataSet.remove(dataSet.find { it.equals(workerInfo.name) })
            dataSet.add(workerInfo)
            notifyDataSetChanged()
        }

        holder.btn_delete.setOnClickListener {
            mWorkManager.cancelUniqueWork(workerInfo.name)
            editor.remove(workerInfo.name).commit()
            dataSet = dataSet.filter { !it.equals(workerInfo) }.toMutableList()
            notifyDataSetChanged()
        }


    }

    override fun getItemCount(): Int = dataSet.size
}
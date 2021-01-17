package com.tejas.heroku_dyno_waker

class WorkerInfo(var id: String, var name: String, var url: String) {

    companion object {
        @JvmStatic
        fun fromString(name: String, str: String): WorkerInfo {
            val split: List<String> = str.split(",")
            return WorkerInfo(split[0], name, split[1])
        }
    }

}
package com.abutorab.resultentry

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.abutorab.resultentry.data.AppDatabase
import com.abutorab.resultentry.data.NetworkManager
import com.abutorab.resultentry.data.ResultRepository

class AppContainer(context: Context) {
    val database by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "result_database"
        ).build()
    }
    
    val networkManager by lazy { NetworkManager() }
    
    val resultRepository by lazy {
        ResultRepository(database.resultDao(), networkManager)
    }
}

class MyApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

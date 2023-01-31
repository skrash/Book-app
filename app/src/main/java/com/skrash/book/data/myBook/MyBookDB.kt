package com.skrash.book.data.myBook

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MyBookItemDbModel::class], version = 1, exportSchema = false)
abstract class MyBookDB: RoomDatabase() {

    abstract fun myBookListDao(): MyBookListDao

    companion object {

        private var INSTANCE: MyBookDB? = null
        private val LOCK = Any()
        private const val DB_NAME = "my_book.db"

        fun getInstance(application: Application): MyBookDB {
            INSTANCE?.let {
                return it
            }
            synchronized(LOCK) {
                INSTANCE?.let {
                    return it
                }
                val db = Room.databaseBuilder(
                    application,
                    MyBookDB::class.java,
                    DB_NAME
                ).build()
                INSTANCE = db
                return db
            }
        }
    }
}
package com.skrash.book.data.myBook

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MyBookItemDbModel::class], version = 2, exportSchema = false)
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
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = db
                return db
            }
        }

        val MIGRATION_1_2 = object : Migration(1,2){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE book_items ADD COLUMN path TEXT DEFAULT '' NOT NULL")
            }
        }
    }
}
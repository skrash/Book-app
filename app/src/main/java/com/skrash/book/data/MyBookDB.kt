package com.skrash.book.data

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.skrash.book.data.Bookmark.BookMarkDbModel
import com.skrash.book.data.Bookmark.BookmarkDao
import com.skrash.book.data.myBook.MyBookItemDbModel
import com.skrash.book.data.myBook.MyBookListDao

@Database(
    entities = [MyBookItemDbModel::class, BookMarkDbModel::class],
    version = 7,
    exportSchema = false
)
abstract class MyBookDB : RoomDatabase() {

    abstract fun myBookListDao(): MyBookListDao
    abstract fun bookmarkDao(): BookmarkDao

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
                    .addMigrations(MIGRATION_6_7)
                    .build()
                INSTANCE = db
                return db
            }
        }

        private val MIGRATION_6_7 = object : Migration(6,7){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE book_items ADD COLUMN shareAccess BOOLEAN NOT NULL DEFAULT 0")
            }
        }
    }
}
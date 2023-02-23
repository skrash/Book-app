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
    version = 6,
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
                    .addMigrations(MIGRATION_4_5)
                    .addMigrations(MIRGRATION_5_6)
                    .build()
                INSTANCE = db
                return db
            }
        }


        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `book_items2` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `author` TEXT NOT NULL, `description` TEXT NOT NULL, `rating` REAL NOT NULL, `popularity` REAL NOT NULL, `genres` TEXT NOT NULL, `tags` TEXT NOT NULL, `path` TEXT NOT NULL, `startOnPage` INTEGER NOT NULL, `fileExtension` TEXT NOT NULL)")
                database.execSQL("DROP TABLE book_items")
                database.execSQL("ALTER TABLE book_items2 RENAME TO book_items")
            }
        }

        private val MIRGRATION_5_6 = object : Migration(5,6){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("UPDATE book_items SET genres = 'Other'")
            }
        }
    }
}
package com.kirille.lifepriority

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase

class DataBaseHelper(_context: Context?) : SQLiteOpenHelper(_context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
                "CREATE TABLE FAVORITES ("
                        + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "POST_ID INTEGER, "
                        + "NAME TEXT, "
                        + "PROFILE_LINK TEXT, "
                        + "DESCRIPTION TEXT, "
                        + "PHOTOS TEXT, "
                        + "DATE INTEGER);"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "adverts"
    }

}
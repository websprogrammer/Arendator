package com.kirille.lifepriority

import android.content.ContentValues
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
                        + "DISTRICT TEXT, "
                        + "PRICE INTEGER, "
                        + "PHOTOS INTEGER, "
                        + "DATE INTEGER);"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1 && newVersion == 2) {
            db?.execSQL("ALTER TABLE FAVORITES ADD COLUMN DISTRICT TEXT;")
            db?.execSQL("ALTER TABLE FAVORITES ADD COLUMN PRICE INTEGER;")

            val defaultValues = ContentValues()
            // Set default values for added columns.
            defaultValues.put("DISTRICT", "")
            defaultValues.put("PRICE", 0)
            db?.update("FAVORITES", defaultValues, null, null)
        }


    }

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "adverts"
    }

}
package com.kirille.lifepriority

import android.content.Context
import android.database.sqlite.SQLiteException
import android.os.AsyncTask
import android.widget.Toast
import java.lang.ref.WeakReference

class GetFavorite(mContext: Context, _postId: Int) : AsyncTask<Void, Void, Boolean>() {
    private val fragmentReference: WeakReference<Context> = WeakReference(mContext)
    private var databaseHelper: DataBaseHelper = DataBaseHelper(fragmentReference.get())
    private val postId: Int = _postId

    private var isFavorite = false

    override fun doInBackground(vararg params: Void?): Boolean {
        try {
            val db = databaseHelper.readableDatabase
            val cursor = db.query("FAVORITES", arrayOf("_id"), "POST_ID = ?",
                    arrayOf(postId.toString()),
                    null,
                    null,
                    null)

            if (cursor.moveToFirst()) {
                isFavorite = true
            }

            cursor.close()
            db.close()
        } catch (e: SQLiteException) {
            Toast.makeText(fragmentReference.get(),
                    "Database unavailable",
                    Toast.LENGTH_SHORT).show()
        }

        return isFavorite
    }

}
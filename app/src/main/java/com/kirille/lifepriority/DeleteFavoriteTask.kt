package com.kirille.lifepriority

import android.content.Context
import android.database.sqlite.SQLiteException
import android.os.AsyncTask
import android.widget.Toast
import java.lang.ref.WeakReference

class DeleteFavoriteTask(mContext: Context, _postId: Int) : AsyncTask<Void, Void, Boolean>() {
    private val postId: Int = _postId
    private val fragmentReference: WeakReference<Context> = WeakReference(mContext)

    override fun onPreExecute() {}

    override fun doInBackground(vararg params: Void?): Boolean {
        val databaseHelper = DataBaseHelper(fragmentReference.get())

        return try {
            val db = databaseHelper.writableDatabase
            db.delete("FAVORITES",
                    "POST_ID = ?",
                    arrayOf(postId.toString())
            )
            db.close()
            true
        } catch (e: SQLiteException) {
            false
        }
    }

    override fun onPostExecute(success: Boolean) {
        if (!success) {
            val context = fragmentReference.get()

            Toast.makeText(
                    context,
                    context?.resources?.getString(R.string.database_unavailable),
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

}
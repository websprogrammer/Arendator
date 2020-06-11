package com.kirille.lifepriority

import android.content.Context
import android.database.sqlite.SQLiteException
import android.os.AsyncTask
import android.widget.Toast
import java.lang.ref.WeakReference
import kotlin.collections.ArrayList

class DeleteFavoritesTask(mContext: Context, private val advertItems: ArrayList<AdvertItem>) :
        AsyncTask<Void, Void, Boolean>() {
    private val fragmentReference: WeakReference<Context> = WeakReference(mContext)


    override fun onPreExecute() {}

    override fun doInBackground(vararg params: Void?): Boolean {


        val databaseHelper = DataBaseHelper(fragmentReference.get())

        val ids = mutableListOf<String>()
        for (item in advertItems) {
            ids.add(item.postId.toString())
        }

        val db = databaseHelper.writableDatabase
        return try {
            db.execSQL(
                    String.format("DELETE FROM FAVORITES WHERE POST_ID IN (%s);",
                            ids.joinToString(separator = ",")
                    )
            )
            true
        } catch (e: SQLiteException) {
            false
        } finally {
            db.close()
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
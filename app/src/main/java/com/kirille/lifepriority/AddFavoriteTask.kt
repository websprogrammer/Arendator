package com.kirille.lifepriority

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteException
import android.os.AsyncTask
import android.widget.Toast
import java.lang.ref.WeakReference

class AddFavoriteTask(mContext: Context, _item: AdvertItem) : AsyncTask<Void, Void, Boolean>() {
    private var favoritesValues: ContentValues = ContentValues()
    private val item: AdvertItem = _item
    private val fragmentReference: WeakReference<Context> = WeakReference(mContext)


    override fun onPreExecute() {
        favoritesValues.put("POST_ID", item.postId)
        favoritesValues.put("NAME", item.name)
        favoritesValues.put("PROFILE_LINK", item.profileLink)
        favoritesValues.put("DESCRIPTION", item.description)
        favoritesValues.put("DISTRICT", item.district)
        favoritesValues.put("PRICE", item.price)
        favoritesValues.put("PHOTOS", item.photos)
        favoritesValues.put("DATE", item.date)
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        val databaseHelper = DataBaseHelper(fragmentReference.get())
        val db = databaseHelper.writableDatabase
        return try {
            db.insert("FAVORITES", null, favoritesValues)
            true
        } catch (e: SQLiteException) {
            false
        } finally {
            db.close()
        }

    }

    override fun onPostExecute(success: Boolean) {
        val resources = fragmentReference.get()?.resources

        val message = when (success) {
            true -> resources?.getString(R.string.favorites_added)
            false -> resources?.getString(R.string.database_unavailable)
        }

        Toast.makeText(
                fragmentReference.get(),
                message,
                Toast.LENGTH_SHORT
        ).show()
    }
}
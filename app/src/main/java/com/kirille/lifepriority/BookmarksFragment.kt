package com.kirille.lifepriority

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.database.sqlite.SQLiteException
import android.widget.Toast


class BookmarksFragment : Fragment() {
    private var mContext: Context? = null

    private var advertsRecycler: RecyclerView? = null
    var advertAdapter: AdvertCardAdapter? = null
    private var linearLayoutManager: LinearLayoutManager? = null

    private var advertDatabaseHelper: DataBaseHelper? = null
    private var db: SQLiteDatabase? = null
    private var favoriteCursor: Cursor? = null

    private var scrollAvailable = false

    private var perPage = 10
    private var currentPage = 1

    private val dialogTaskTag = "DialogTaskTag"
    private val taskCode = 3

    private val dialogDeleteBookmarksTag = "dialogDeleteBookmarksTag"
    private val addingTaskCode = 5

    private var items: ArrayList<AdvertItem> = ArrayList()
    private var itemsToDelete: ArrayList<AdvertItem> = ArrayList()


    private val recyclerViewScrollLister = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            if (dy > 0) {
                val mLayoutManager = recyclerView.layoutManager as LinearLayoutManager

                if (scrollAvailable) {
                    val position = mLayoutManager.findLastVisibleItemPosition()
                    if (position == items.size - 1) {
                        scrollAvailable = false
                        fetchBookmarks()
                    }
                }
            }

        }
    }

    private val recyclerViewLongClickListener = object : AdvertCardAdapter.LongClickListener {
        override fun onLongClick(item: AdvertItem, position: Int, holder: RecyclerView.ViewHolder) {
            val bookmarksToolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
            bookmarksToolbar?.menu?.clear()

            if (itemsToDelete.contains(item)) {
                itemsToDelete.remove(item)
                item.isSelected = false
            } else {
                itemsToDelete.add(item)
                item.isSelected = true
            }

            advertAdapter?.notifyDataSetChanged()

            if (itemsToDelete.isEmpty()) {
                bookmarksToolbar?.setTitle(R.string.bookmarks)
                bookmarksToolbar?.inflateMenu(R.menu.dialog_menu)
                bookmarksToolbar?.navigationIcon = null
            } else {
                bookmarksToolbar?.title = itemsToDelete.size.toString()
                bookmarksToolbar?.setNavigationIcon(R.drawable.baseline_undo_white_36)
                bookmarksToolbar?.setNavigationOnClickListener {
                    for (_item in itemsToDelete) {
                        val currentItem = items[items.indexOf(_item)]
                        currentItem.isSelected = false
                    }

                    advertAdapter?.notifyDataSetChanged()
                    clearToolbar(bookmarksToolbar)
                    itemsToDelete.clear()
                }


                bookmarksToolbar?.inflateMenu(R.menu.dialog_menu4)
                bookmarksToolbar?.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {
                        when (item?.itemId) {
                            R.id.action_back_home -> {
                                val intent = Intent(mContext, MainActivity::class.java)
                                startActivityForResult(intent, 0)
                                return true
                            }

                            R.id.action_remove_bookmarks -> {
                                val alertDialog = AlertDialog.Builder(activity!!)
                                alertDialog.setTitle(R.string.confirm_bookmarks_deletion)
                                alertDialog.setPositiveButton(
                                        R.string.confirm
                                ) { _, _ ->
                                    DeleteBookmarksTask().execute()
                                    clearToolbar(bookmarksToolbar)

                                }
                                alertDialog.setNegativeButton(R.string.cancel) {
                                    dialog, _ -> dialog.dismiss()
                                }
                                alertDialog.create()
                                alertDialog.show()

                                return true

                            }
                        }
                        return false
                    }
                })

            }

        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        advertDatabaseHelper = DataBaseHelper(mContext)

        advertsRecycler = inflater.inflate(R.layout.fragment_bookmarks, container, false) as RecyclerView
        advertAdapter = AdvertCardAdapter(items)
        linearLayoutManager = LinearLayoutManager(mContext)

        advertsRecycler?.adapter = advertAdapter
        advertsRecycler?.layoutManager = linearLayoutManager
        advertsRecycler?.addOnScrollListener(recyclerViewScrollLister)

        advertAdapter?.setClickListener(object : AdvertCardAdapter.ClickListener {
            override fun onClick(item: AdvertItem, position: Int) {
                openDialog(item, position)
            }
        })

        advertAdapter?.setLongClickListener(recyclerViewLongClickListener)

        db = advertDatabaseHelper?.readableDatabase

        fetchBookmarks()

        return advertsRecycler
    }

    fun clearToolbar(bookmarksToolbar: Toolbar) {
        bookmarksToolbar.menu?.clear()
        bookmarksToolbar.setTitle(R.string.bookmarks)
        bookmarksToolbar.inflateMenu(R.menu.dialog_menu)
        bookmarksToolbar.navigationIcon = null
    }

    fun setEmptyView() {
        val emptyView = activity?.findViewById<TextView>(R.id.empty_view)
        emptyView?.visibility = TextView.VISIBLE
    }

    @SuppressLint("Recycle")
    fun fetchBookmarks() {
        val currentItemSize = items.size
        val entries = DatabaseUtils.queryNumEntries(db, "FAVORITES")


        favoriteCursor = db?.query(
                "FAVORITES",
                arrayOf("POST_ID", "NAME", "PROFILE_LINK", "DESCRIPTION", "PHOTOS", "DATE"),
                null,
                null,
                null,
                null,
                null,
                "$currentItemSize, $perPage"
        )

        while (favoriteCursor!!.moveToNext()) {
            val postId = favoriteCursor!!.getInt(favoriteCursor!!.getColumnIndexOrThrow("POST_ID"))
            val name = favoriteCursor!!.getString(favoriteCursor!!.getColumnIndexOrThrow("NAME"))
            val profileLink = favoriteCursor!!.getString(favoriteCursor!!.getColumnIndexOrThrow("PROFILE_LINK"))
            val description = favoriteCursor!!.getString(favoriteCursor!!.getColumnIndexOrThrow("DESCRIPTION"))
            val photos = favoriteCursor!!.getString(favoriteCursor!!.getColumnIndexOrThrow("PHOTOS"))
            val date = favoriteCursor!!.getInt(favoriteCursor!!.getColumnIndexOrThrow("DATE"))

            val item = AdvertItem(
                    postId,
                    name,
                    profileLink,
                    description.trimEnd(),
                    photos,
                    date,
                    isFavorite = true,
                    isSelected = false,
                    isNew = false
            )

            items.add(item)
        }


        if (entries - items.size > 0) {
            scrollAvailable = true
            currentPage++
        }

        advertsRecycler?.post {
            advertAdapter?.notifyDataSetChanged()
        }

        if (items.size == 0) {
            setEmptyView()
        }
    }


    fun openDialog(item: AdvertItem, position: Int) {
        activity.let {
            val advertDialogFragment = AdvertDialogFragment()

            val args = Bundle()
            args.putParcelable("item", item)
            args.putInt("position", position)

            advertDialogFragment.arguments = args
            advertDialogFragment.setTargetFragment(this, taskCode)
            advertDialogFragment.show(activity?.supportFragmentManager!!, dialogTaskTag)
        }
    }


    @SuppressLint("StaticFieldLeak")
    inner class DeleteBookmarksTask : AsyncTask<Void, Void, Boolean>() {

        private var dialog: DialogFragment? = null

        init {
            dialog = DialogDeleteBookmarks()
        }


        override fun onPreExecute() {
            dialog?.setTargetFragment(
                    this@BookmarksFragment,
                    addingTaskCode)
            dialog?.show(fragmentManager!!, dialogDeleteBookmarksTag)

        }

        override fun doInBackground(vararg params: Void?): Boolean {
            val databaseHelper = DataBaseHelper(mContext)

            val ids = mutableListOf<String>()
            for (item in itemsToDelete) {
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
                Toast
                    .makeText(mContext, R.string.database_unavailable, Toast.LENGTH_LONG)
                    .show()
            } else {
                for (_item in itemsToDelete) {
                    items.remove(_item)
                }

                if (items.isEmpty()) {
                    setEmptyView()
                }

                advertAdapter?.notifyDataSetChanged()
                itemsToDelete.clear()
            }

            dialog?.dismiss()

        }

    }


    override fun onDestroy() {
        super.onDestroy()

        favoriteCursor.let {
            favoriteCursor?.close()
        }

        db.let {
            db?.close()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
}
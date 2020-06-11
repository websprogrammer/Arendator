package com.kirille.lifepriority

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONException


class AdvertDialogFragment: DialogFragment() {
    private var mContext: Context? = null

    private var item: AdvertItem? = null
    private var toolbar: Toolbar? = null

    private val addingTaskCode = 2
    private val dialogFragmentTag = "dialogFragmentTag"

    private val menuListener: Toolbar.OnMenuItemClickListener = Toolbar.OnMenuItemClickListener {
        itemMenu ->
            when(itemMenu?.itemId){
                R.id.vk_link -> {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item!!.profileLink))
                    startActivity(browserIntent)
                }

                R.id.action_favorite -> {
                    if (targetFragment is MainFragment) {
                        val menuMenu = toolbar?.menu?.getItem(0)

                        if (item!!.isFavorite) {
                            DeleteFavoriteTask(mContext!!, item!!.postId).execute()
                            item!!.isFavorite = false
                            menuMenu?.setIcon(R.mipmap.baseline_favorite_border_white_36)
                        } else {
                            AddFavoriteTask(mContext!!, item!!).execute()
                            item!!.isFavorite = true
                            menuMenu?.setIcon(R.mipmap.baseline_favorite_white_36)
                        }

                        (targetFragment as? MainFragment)?.advertAdapter?.notifyDataSetChanged()

                    }

                }
            }
        return@OnMenuItemClickListener false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dialog, container, false)

        item = arguments?.getParcelable("item")

        val advertNameDetail = view.findViewById<TextView>(R.id.advertNameDetail)
        advertNameDetail.text = item!!.name

        val dateTimeString = getDateString(item!!.date, resources)
        val advertDateDetail = view.findViewById<TextView>(R.id.advertDateDetail)
        advertDateDetail.text = dateTimeString

        val advertTextDetail = view.findViewById<TextView>(R.id.advertTextDetail)
        advertTextDetail.text = item!!.description

        try {
            val jsonPhotoArray = JSONArray(item!!.photos)
            val photosRecyclerView = view.findViewById<RecyclerView>(R.id.recycler_photo_view)

            val galleryAdapter = GalleryAdapter(mContext!!, jsonPhotoArray)
            val horizontalLayoutManager = LinearLayoutManager(
                    activity,
                    LinearLayoutManager.HORIZONTAL,
                    false)

            photosRecyclerView.layoutManager = horizontalLayoutManager
            photosRecyclerView.adapter = galleryAdapter

            galleryAdapter.setGalleryListener(object : GalleryAdapter.GalleryListener {
                override fun onClick(position: Int) {
                    fragmentManager.let {
                        val dialogImageFragment = DialogImageFragment()

                        val args = Bundle()
                        args.putString("photos", item!!.photos)
                        args.putInt("selectedPosition", position)

                        dialogImageFragment.arguments = args
                        dialogImageFragment.setTargetFragment(
                                this@AdvertDialogFragment,
                                addingTaskCode)
                        dialogImageFragment.show(fragmentManager!!, dialogFragmentTag)
                    }
                }
            })


        } catch (e: JSONException){
            Log.d(dialogFragmentTag, "JSONException: $e")
        }


        toolbar = view.findViewById(R.id.toolbar)
        toolbar?.setNavigationIcon(R.mipmap.baseline_arrow_back_white_36)
        toolbar?.setNavigationOnClickListener {
            dismiss()
        }
        toolbar?.inflateMenu(R.menu.dialog_menu2)

        if (targetFragment is BookmarksFragment){
            toolbar?.menu?.getItem(0)?.isVisible = false
        }

        if (item!!.isFavorite){
            toolbar?.menu?.getItem(0)?.setIcon(R.mipmap.baseline_favorite_white_36)
        } else {
            toolbar?.menu?.getItem(0)?.setIcon(R.mipmap.baseline_favorite_border_white_36)
        }

        toolbar?.setOnMenuItemClickListener(menuListener)

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

}
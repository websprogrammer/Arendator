package com.kirille.lifepriority

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AdvertDialogFragment : DialogFragment() {
    private var mContext: Context? = null

    private var item: AdvertItem? = null
    private var toolbar: Toolbar? = null

    private val addingTaskCode = 2
    private val dialogFragmentTag = "dialogFragmentTag"

    private val settingsFileName = "com.kirille.lifepriority.prefs"

    private val menuListener: Toolbar.OnMenuItemClickListener = Toolbar.OnMenuItemClickListener { itemMenu ->
        when (itemMenu?.itemId) {
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

    @SuppressLint("InflateParams")
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

        val alertValues = resources.getStringArray(R.array.alert_values)

        val floatButton = view.findViewById<FloatingActionButton>(R.id.floatButton)
        floatButton.setOnClickListener {

            val feedbackDialogBuilder = AlertDialog.Builder(mContext!!)
            feedbackDialogBuilder.setTitle(R.string.alert_title)
            feedbackDialogBuilder.setItems(alertValues) { _, which ->

                val selectedOption = which + 1
                val sharedPref = activity?.getSharedPreferences(settingsFileName, 0)
                val city = sharedPref!!.getString("city", "nn")


                if (selectedOption != 4) {
                    sendFeedback(view, city!!, item!!, selectedOption, "")
                } else {
                    val otherFeedbackDialogBuilder = AlertDialog.Builder(mContext!!)
                    val otherFeedbackLayout = layoutInflater.inflate(R.layout.other_feedback_layout, null)

                    otherFeedbackDialogBuilder.setView(otherFeedbackLayout)
                    otherFeedbackDialogBuilder.setMessage(R.string.other_alert_title)
                    otherFeedbackDialogBuilder.setPositiveButton(R.string.send_feedback_button, null)


                    otherFeedbackDialogBuilder.setCancelable(true)
                    val otherFeedbackDialog = otherFeedbackDialogBuilder.create()
                    otherFeedbackDialog.show()

                    val positiveBtn = otherFeedbackDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    positiveBtn.setOnClickListener {
                        val message = otherFeedbackLayout.findViewById<EditText>(R.id.editOther)
                        if (message.text.isNotEmpty()) {
                            sendFeedback(view, city!!, item!!, selectedOption, message.text.toString())
                            otherFeedbackDialog.dismiss()
                        }
                    }

                }

            }
            feedbackDialogBuilder.setCancelable(true)
            feedbackDialogBuilder.create()
            feedbackDialogBuilder.show()
        }



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


        } catch (e: JSONException) {
            Log.d(dialogFragmentTag, "JSONException: $e")
        }


        toolbar = view.findViewById(R.id.toolbar)
        toolbar?.setNavigationIcon(R.mipmap.baseline_arrow_back_white_36)
        toolbar?.setNavigationOnClickListener {
            dismiss()
        }
        toolbar?.inflateMenu(R.menu.dialog_menu2)

        if (targetFragment is BookmarksFragment) {
            toolbar?.menu?.getItem(0)?.isVisible = false
        }

        if (item!!.isFavorite) {
            toolbar?.menu?.getItem(0)?.setIcon(R.mipmap.baseline_favorite_white_36)
        } else {
            toolbar?.menu?.getItem(0)?.setIcon(R.mipmap.baseline_favorite_border_white_36)
        }

        toolbar?.setOnMenuItemClickListener(menuListener)

        return view
    }


    private fun sendFeedback(view: View, city: String, item: AdvertItem, type: Int, message: String) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar2)
        progressBar?.visibility = ProgressBar.VISIBLE

        val apiService = APIService.create()
        apiService
                .sendFeedback(city, item.postId, type, message)
                .enqueue(object : Callback<JsonObject> {
                    override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                        progressBar?.visibility = ProgressBar.GONE
                        Toast.makeText(mContext,
                                R.string.send_token_error,
                                Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>?) {
                        progressBar?.visibility = ProgressBar.GONE
                        Toast.makeText(mContext,
                                R.string.send_feedback,
                                Toast.LENGTH_SHORT).show()
                    }

                })
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

}
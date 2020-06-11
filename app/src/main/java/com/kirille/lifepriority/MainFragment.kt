package com.kirille.lifepriority

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainFragment : androidx.fragment.app.Fragment() {
    private var mContext: Context? = null

    var advertAdapter: AdvertCardAdapter? = null
    private var advertsRecycler: RecyclerView? = null

    private var linearLayoutManager: LinearLayoutManager? = null

    private var items: ArrayList<AdvertItem> = ArrayList()

    private var scrollAvailable = false
    private var currentPage = 1
    private var pageCount: Int? = 0

    private val requestCode = 1
    private val dialogTaskTag = "DialogTaskTag"
    private val settingsFileName = "com.kirille.lifepriority.prefs"

    private var progressBar: ProgressBar? = null
    private var swipeRefresh: SwipeRefreshLayout? = null
    private var gridNoResults: GridLayout? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_main, container, false)

        val notificationManager = mContext?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()

        setToken()

        progressBar = view.findViewById(R.id.progressBar)

        advertAdapter = AdvertCardAdapter(items)
        advertAdapter?.setClickListener(object : AdvertCardAdapter.ClickListener {
            override fun onClick(item: AdvertItem, position: Int) {
                openDialog(item)
            }
        })

        advertsRecycler = view.findViewById(R.id.adverts_recycler)
        advertsRecycler?.adapter = advertAdapter
        advertsRecycler?.isNestedScrollingEnabled = false

        linearLayoutManager = LinearLayoutManager(activity)
        advertsRecycler?.layoutManager = linearLayoutManager

        advertsRecycler?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val mLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (!scrollAvailable){
                    val position = mLayoutManager.findLastVisibleItemPosition()
                    if (position == items.size - 1 && currentPage <= pageCount!!){
                        fetchAdverts()
                        scrollAvailable = true
                    }
                }
            }
        })

        gridNoResults = view.findViewById(R.id.no_results)

        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        swipeRefresh?.setOnRefreshListener {
            currentPage = 1
            items.clear()
            advertAdapter?.notifyDataSetChanged()
            swipeRefresh?.isRefreshing = false
        }
        swipeRefresh?.setColorSchemeResources(android.R.color.holo_red_dark)

        val refreshButton = view.findViewById<Button>(R.id.refresh_link)
        refreshButton?.setOnClickListener {
            gridNoResults?.visibility = GridLayout.GONE
            swipeRefresh?.isEnabled = false
            fetchAdverts()
        }

        fetchAdverts()
        return view
    }


    private fun handleResponse(body: JsonObject) {
        pageCount = body.get("pages")?.asInt
        if (pageCount == 0) {
            setEmptyView()
        } else {
            val adverts = body.get("adverts")?.asJsonArray
            val currentItemSize = items.size
            for (i in 0 until adverts!!.size()) {
                val advert = adverts.get(i)?.asJsonObject
                advert!!

                items.add(
                    AdvertItem(
                        advert.get("post_id")!!.asInt,
                        advert.get("profile_name")!!.asString,
                        advert.get("profile_link")!!.asString,
                        advert.get("description")!!.asString.trimEnd(),
                        advert.get("photos")!!.toString(),
                        advert.get("date")!!.asInt,
                        isFavorite = false,
                        isSelected = false,
                        isNew = false
                    ))
            }

            swipeRefresh?.isEnabled = true
            if (items.size > currentItemSize) {
                advertAdapter?.notifyItemRangeInserted(currentItemSize, items.size)
                currentPage++
            }
        }

    }

    private fun setToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(mContext, R.string.farebase_error, Toast.LENGTH_LONG).show()
                    return@OnCompleteListener
                }

                val token = task.result?.token
                val sharedPref = activity?.getSharedPreferences(settingsFileName, 0)

                with(sharedPref!!.edit()) {
                    putString("token", token)
                    apply()
                }

            })
    }

    private fun setEmptyView() {
        swipeRefresh?.isEnabled = false
        gridNoResults?.visibility = GridLayout.VISIBLE
    }

    private fun removeItemLoader() {
        items.removeAt(items.size - 1)
        val scrollPosition = items.size
        advertAdapter?.notifyItemRemoved(scrollPosition)
    }

    private fun fetchAdverts() {
        var hasItemLoader = false

        if (currentPage == 1) {
            progressBar?.visibility = View.VISIBLE
        } else {
            items.add(AdvertItem(
                    -1,
                    "",
                    "",
                    "",
                    "",
                    -1,
                    isFavorite = false,
                    isSelected = false,
                    isNew = false
            ))
            hasItemLoader = true
            advertsRecycler?.post{
              advertAdapter?.notifyItemInserted(items.size - 1)
            }
        }

        val (city, rentType, roomType, keyWords, _) = getPreferences(activity, resources)

        val apiService = APIService.create()
        apiService
            .getAdverts(currentPage, city, keyWords, rentType, roomType)
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    try {
                        if (hasItemLoader){
                            removeItemLoader()
                        }

                        if (response.isSuccessful){
                            val body = response.body()
                            handleResponse(body!!)
                        } else {
                            setEmptyView()
                        }

                    } finally {
                        progressBar?.visibility = View.GONE
                        scrollAvailable = false
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    if (hasItemLoader) {
                        removeItemLoader()
                    }
                    Toast.makeText(
                            activity,
                            R.string.results_failure,
                            Toast.LENGTH_LONG
                    ).show()

                    if (currentPage == 1) {
                        setEmptyView()
                    }

                    progressBar?.visibility = View.GONE
                    scrollAvailable = false
                }
            })

    }

    private fun openDialog(item: AdvertItem) {
        activity.let {
            val advertDialogFragment = AdvertDialogFragment()

            val args = Bundle()
            args.putParcelable("item", item)

            advertDialogFragment.arguments = args
            advertDialogFragment.setTargetFragment(this, requestCode)
            advertDialogFragment.show(activity?.supportFragmentManager!!, dialogTaskTag)
        }
    }


    override fun onResume() {
        super.onResume()

        advertAdapter.let {
            advertAdapter?.notifyDataSetChanged()
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
}
package com.kirille.lifepriority

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 *
 */
class SettingsFragment : androidx.fragment.app.Fragment() {
    private var mContext: Context? = null
    private var sharedPref: SharedPreferences? = null

    private val settingsFileName = "com.kirille.lifepriority.prefs"

    private var notifications = 0

    private var city: String? = null
    private var cityTextView: TextView? = null
    private var selectedCityItem = 0

    private var rentIn: Boolean = false
    private var rentOut: Boolean = false

    private var isApartment: Boolean = false
    private var isRoom: Boolean = false

    private var keyWords: String? = null
    private var keyWordsList: ArrayList<String> = ArrayList()
    private var keyWordsAdapter: KeyWordsAdapter? = null

    private var token: String? = ""

    private var progressBar: ProgressBar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        sharedPref = activity?.getSharedPreferences(settingsFileName, 0)
        val view = inflater.inflate(R.layout.fragment_settings, container, false)


        token = sharedPref!!.getString("token", "")
        // notificationsSwitch should be disabled if token is unavailable
        if (token != "") {
            val notificationsSettings = sharedPref!!.getInt("notifications", 0)

            val notificationsSwitch = view.findViewById<Switch>(R.id.notificationsSwitch)
            notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
                notifications = if (isChecked) 1 else 0
            }
            notificationsSwitch.isChecked = notificationsSettings == 1
            notificationsSwitch.isClickable = true
        }


        cityTextView = view.findViewById(R.id.city_select)
        val cityNames = resources.getStringArray(R.array.city_names)
        val cityValues = resources.getStringArray(R.array.city_values)
        val cityPosition = cityValues.indexOf(sharedPref!!.getString("city", cityValues[0]))
        cityTextView?.text = cityNames[cityPosition]
        selectedCityItem = cityPosition
        city = cityValues[cityPosition]

        val cityLayout = view.findViewById<RelativeLayout>(R.id.cityLayout)
        cityLayout.setOnClickListener {
            openDialog()
        }

        val rentInCheckbox = view.findViewById<CheckBox>(R.id.rentInCheckbox)
        rentInCheckbox.setOnCheckedChangeListener { _, isChecked ->
            rentIn = isChecked
        }
        rentInCheckbox.isChecked = sharedPref!!.getBoolean("rentIn", false)


        val rentOutCheckbox = view.findViewById<CheckBox>(R.id.rentOutCheckbox)
        rentOutCheckbox.setOnCheckedChangeListener { _, isChecked ->
            rentOut = isChecked
        }
        rentOutCheckbox.isChecked = sharedPref!!.getBoolean("rentOut", false)


        val apartmentCheckbox = view.findViewById<CheckBox>(R.id.apartmentCheckbox)
        apartmentCheckbox.setOnCheckedChangeListener { _, isChecked ->
            isApartment = isChecked

        }
        apartmentCheckbox.isChecked = sharedPref!!.getBoolean("isApartment", false)


        val roomCheckbox = view.findViewById<CheckBox>(R.id.roomCheckbox)
        roomCheckbox.setOnCheckedChangeListener { _, isChecked ->
            isRoom = isChecked
        }
        roomCheckbox.isChecked = sharedPref!!.getBoolean("isRoom", false)


        val butt = view.findViewById<Button>(R.id.buttonAdd)
        val words = view.findViewById<EditText>(R.id.edtInput)
        words.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val trimText = p0.toString().trim()

                if (trimText.length in 4..15) {
                    butt.visibility = View.VISIBLE
                } else {
                    butt.visibility = View.GONE
                }
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })


        keyWordsAdapter = KeyWordsAdapter(R.layout.key_words_item, keyWordsList)
        keyWordsAdapter?.setLongClickListener(object : KeyWordsAdapter.LongClickListener {
            override fun onLongClick(item: String, position: Int) {
                keyWordsList.removeAt(position)
                keyWordsAdapter?.notifyDataSetChanged()

                keyWords = keyWordsList.joinToString("|")
            }

        })

        val keyWordsRecycler = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.keyWordsRecycler)

        val layoutManager = FlexboxLayoutManager(mContext)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.justifyContent = JustifyContent.FLEX_START


        keyWordsRecycler.layoutManager = layoutManager
        keyWordsRecycler.adapter = keyWordsAdapter


        butt.setOnClickListener {
            if (keyWordsList.size > 5) {
                Toast.makeText(mContext,
                        R.string.key_words_alert,
                        Toast.LENGTH_SHORT).show()
            } else {
                val txt = words.text.toString().trim().replace("\\s+".toRegex(), " ")
                keyWordsList.add(txt)
                keyWordsAdapter?.notifyDataSetChanged()

                words.text.clear()

                keyWords = keyWordsList.joinToString("|")
            }

        }

        setKeyWords()

        return view
    }

    private fun openDialog() {
        val cityValues = resources.getStringArray(R.array.city_values)
        val cityNames = resources.getStringArray(R.array.city_names)

        val alertDialogBuilder = AlertDialog.Builder(mContext!!)
        alertDialogBuilder.setSingleChoiceItems(cityNames, selectedCityItem) { dialog, which ->
            city = cityValues[which]
            cityTextView?.text = cityNames[which]
            selectedCityItem = which
            dialog.cancel()
        }

        alertDialogBuilder.create()
        alertDialogBuilder.show()
    }

    private fun setKeyWords() {
        keyWords = sharedPref!!.getString("keyWords", "")

        if (keyWords != "") {
            for (word in keyWords?.split("|")!!.toTypedArray()) {
                keyWordsList.add(word)
            }
            keyWordsAdapter?.notifyDataSetChanged()
        }
    }

    private fun saveSettings() {
        progressBar = view?.findViewById(R.id.progressBar)
        progressBar?.visibility = ProgressBar.VISIBLE

        with(sharedPref!!.edit()) {
            putInt("notifications", notifications)
            apply()
        }

        with(sharedPref!!.edit()) {
            putBoolean("rentIn", rentIn)
            apply()
        }

        with(sharedPref!!.edit()) {
            putBoolean("rentOut", rentOut)
            apply()
        }

        with(sharedPref!!.edit()) {
            putBoolean("isApartment", isApartment)
            apply()
        }

        with(sharedPref!!.edit()) {
            putBoolean("isRoom", isRoom)
            apply()
        }

        with(sharedPref!!.edit()) {
            putString("city", city)
            apply()
        }

        with(sharedPref!!.edit()) {
            putString("keyWords", keyWords)
            apply()
        }

        // Just saving  settings in sharedPref if token is not available
        if (token.isNullOrBlank()) {
            successSaveHandler()
            return
        }

        sendTokenToServer()
    }

    private fun sendTokenToServer() {
        val roomType = if (isApartment && !isRoom) 1
        else if (!isApartment && isRoom) 2
        else 0

        val rentType = if (rentIn && !rentOut) 1
        else if (!rentIn && rentOut) 2
        else 0

        val apiService = APIService.create()
        apiService
                .sendToken(token, city, keyWords, rentType, roomType, notifications)
                .enqueue(object : Callback<JsonObject> {
                    override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                        progressBar?.visibility = ProgressBar.GONE
                        Toast.makeText(mContext,
                                R.string.send_token_error,
                                Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>?) {
                        successSaveHandler()
                    }

                })
    }

    private fun successSaveHandler() {
        progressBar?.visibility = ProgressBar.GONE
        val refresh = Intent(mContext, MainActivity::class.java)
        startActivity(refresh)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save_settings -> {
                saveSettings()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

}
package com.kirille.lifepriority

import android.content.res.Resources
import androidx.fragment.app.FragmentActivity
import java.util.*

const val preferencesFileName = "com.kirille.lifepriority.prefs"

data class SharedPrefResult(
        val city: String?,
        val rentType: Int,
        val roomType: Int,
        val keyWords: String?,
        val notifications: Int,
        val districts: String?
)


fun getPreferences(activity: FragmentActivity?, resources: Resources): SharedPrefResult {
    val sharedPref = activity?.getSharedPreferences(preferencesFileName, 0)
    sharedPref!!

    val rentIn = sharedPref.getBoolean("rentIn", false)
    val rentOut = sharedPref.getBoolean("rentOut", false)
    val rentType = if (rentIn && !rentOut) 2
        else if (!rentIn && rentOut) 1
        else 0

    val isApartment = sharedPref.getBoolean("isApartment", false)
    val isRoom = sharedPref.getBoolean("isRoom", false)
    val roomType = if (isApartment && !isRoom) 1
        else if (!isApartment && isRoom) 2
        else 0

    val city = sharedPref.getString("city", resources.getStringArray(R.array.city_values)[0])
    val keyWords = sharedPref.getString("keyWords", "")

    val notifications = sharedPref.getInt("notifications", 0)

    val districts = sharedPref.getString("districts", "")

    return SharedPrefResult(city, rentType, roomType, keyWords, notifications, districts)
}


fun getDateString(date: Int, resources: Resources): String {
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"))

    val months = resources.getStringArray(R.array.months)
    val todayTime = resources.getString(R.string.today_time)
    val yesterdayTime = resources.getString(R.string.yesterday_time)
    val prefixTime = resources.getString(R.string.prefix_time)

    val today = Calendar.getInstance()

    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DATE, -1)

    val advertDate = Calendar.getInstance()
    advertDate.timeInMillis = date * 1000L

    val month = months[advertDate.get(Calendar.MONTH)]
    val time = String.format(
            Locale.ENGLISH,
            "%02d:%02d",
            advertDate.get(Calendar.HOUR_OF_DAY),
            advertDate.get(Calendar.MINUTE)
    )

    return if (today.get(Calendar.YEAR) == advertDate.get(Calendar.YEAR)) {
        when {
            today.get(Calendar.DAY_OF_YEAR) == advertDate.get(Calendar.DAY_OF_YEAR) ->
                todayTime + prefixTime + time

            advertDate.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) ->
                yesterdayTime + prefixTime + time

            else -> advertDate.get(Calendar.DATE).toString() + " " + month + prefixTime + time
        }

    } else {
        if (advertDate.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
            yesterdayTime + prefixTime + time
        } else {
            advertDate.get(Calendar.DATE).toString() + " " + month + " " + advertDate.get(Calendar.YEAR) + prefixTime + time
        }
    }

}
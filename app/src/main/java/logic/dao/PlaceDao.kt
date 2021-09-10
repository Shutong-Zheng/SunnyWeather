package logic.dao

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.sunnyweather.android.SunnyWeatherApplication
import logic.model.Place

object PlaceDao {
    fun savePlace(place: Place){
        sharedPreferences().edit().apply {
            putString("place", Gson().toJson(place))
            apply()
        }
    }

    fun getSavedPlace(): Place{
        val placeJson = sharedPreferences().getString("place", "")
        return Gson().fromJson(placeJson, Place::class.java)
    }

    fun isPlaceSaved(): Boolean{
        return sharedPreferences().contains("place")
    }

    private fun sharedPreferences() = SunnyWeatherApplication.context.
                                        getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)
}
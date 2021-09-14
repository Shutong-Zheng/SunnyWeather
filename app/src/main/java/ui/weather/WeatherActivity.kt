package ui.weather

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import com.sunnyweather.android.R
import logic.model.Weather
import logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {
    val viewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(WeatherViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        val decroView = window.decorView
        decroView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
        }
        val navBtn = findViewById<Button>(R.id.navBtn)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        navBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        drawerLayout.addDrawerListener(object: DrawerLayout.DrawerListener{
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

            }

            override fun onDrawerOpened(drawerView: View) {

            }

            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })
        //从上一个Activity获取lat和lng
        if(viewModel.locationLng.isEmpty()){
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
            Log.d("WeatherActivity", viewModel.locationLng)
        }
        if(viewModel.locationLat.isEmpty()){
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
            Log.d("WeatherActivity", viewModel.locationLat)
        }
        if(viewModel.placeName.isEmpty()){
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
            Log.d("WeatherActivity", viewModel.placeName)
        }
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()    //获取成功则获取Weather对象
            if(weather != null){
                showWeatherInfo(weather);
            } else {
                Toast.makeText(this, "未能成功获取天气信息", Toast.LENGTH_LONG).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            findViewById<SwipeRefreshLayout>(R.id.swipeRefresh).isRefreshing = false
        })
        findViewById<SwipeRefreshLayout>(R.id.swipeRefresh).setColorSchemeResources(R.color.gray)
        refreshWeather()
        findViewById<SwipeRefreshLayout>(R.id.swipeRefresh).setOnRefreshListener {
            refreshWeather()
        }
    }

    fun refreshWeather(){
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        findViewById<SwipeRefreshLayout>(R.id.swipeRefresh).isRefreshing = true
    }

    private fun showWeatherInfo(weather: Weather){
        val placeName = findViewById<TextView>(R.id.placeName)
        placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
        val currentTempText = "${realtime.temperature.toInt()}"
        val currentTemp = findViewById<TextView>(R.id.currentTemp)
        currentTemp.text = currentTempText
        val currentSkyText =  getSky(realtime.skycon).info
        val currentSky = findViewById<TextView>(R.id.currentSky)
        currentSky.text = currentSkyText
        val currentAQIText = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        val currentAQI = findViewById<TextView>(R.id.currentAQI)
        currentAQI.text = currentAQIText
        val nowLayout = findViewById<RelativeLayout>(R.id.nowLayout)
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
        val forecastLayout = findViewById<LinearLayout>(R.id.forecastLayout)
        forecastLayout.removeAllViews()
        val days = daily.temperature.size
        for(i in 0 until days){
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                forecastLayout, false)
            val dateInfo = view.findViewById<TextView>(R.id.dateInfo)
            val skyIcon = view.findViewById<ImageView>(R.id.skyIcon)
            val skyInfo = view.findViewById<TextView>(R.id.skyInfo)
            val temperatureInfo = view.findViewById<TextView>(R.id.temperatureInfo)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            Log.d("WeatherActivity", simpleDateFormat.format(skycon.date))
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            forecastLayout.addView(view)
        }
        val lifeIndex = daily.lifeIndex
        findViewById<TextView>(R.id.coldRiskText).text = lifeIndex.coldRisk[0].desc
        findViewById<TextView>(R.id.dressingText).text = lifeIndex.dressing[0].desc
        findViewById<TextView>(R.id.ultravioletText).text = lifeIndex.ultraviolet[0].desc
        findViewById<TextView>(R.id.carWashingText).text = lifeIndex.carWashing[0].desc
        val weatherActivity = findViewById<ScrollView>(R.id.weatherLayout)
        weatherActivity.visibility = View.VISIBLE
    }
}
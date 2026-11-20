package cn.edu.sicnu.cs.ys.exp_08

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson

class WeatherActivity : AppCompatActivity() {

    private lateinit var rootLayout: View
    private lateinit var btnBack: Button
    private lateinit var btnRetry: Button
    private lateinit var tvTitle: TextView
    private lateinit var scrollWeather: ScrollView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorLayout: LinearLayout
    private lateinit var tvError: TextView

    private lateinit var tvCityName: TextView
    private lateinit var tvUpdateTime: TextView
    private lateinit var tvCurrentTemp: TextView
    private lateinit var tvCurrentType: TextView
    private lateinit var ivCurrentIcon: ImageView
    private lateinit var tvTodayRange: TextView
    private lateinit var tvWind: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvAir: TextView
    private lateinit var tvPm: TextView
    private lateinit var tvSun: TextView
    private lateinit var tvNotice: TextView
    private lateinit var tvLifeSuggestion: TextView
    private lateinit var llForecast: LinearLayout

    private lateinit var requestQueue: RequestQueue
    private val gson = Gson()

    private var cityCode: String = DEFAULT_CITY_CODE
    private var cityName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_weather)

        cityCode = intent.getStringExtra(EXTRA_CITY_CODE)?.takeIf { it.isNotBlank() } ?: DEFAULT_CITY_CODE
        cityName = intent.getStringExtra(EXTRA_CITY_NAME)?.takeIf { it.isNotBlank() }
            ?: getString(R.string.weather_title)

        initViews()
        initListeners()
        applyWindowInsets()

        tvTitle.text = getString(R.string.weather_title_city, cityName)
        requestQueue = Volley.newRequestQueue(applicationContext)
        loadWeather()
    }

    private fun initViews() {
        rootLayout = findViewById(R.id.weatherRoot)
        btnBack = findViewById(R.id.btnBack)
        btnRetry = findViewById(R.id.btnRetry)
        tvTitle = findViewById(R.id.tvTitle)
        scrollWeather = findViewById(R.id.scrollWeather)
        progressBar = findViewById(R.id.progressBar)
        errorLayout = findViewById(R.id.errorLayout)
        tvError = findViewById(R.id.tvError)

        tvCityName = findViewById(R.id.tvCityName)
        tvUpdateTime = findViewById(R.id.tvUpdateTime)
        tvCurrentTemp = findViewById(R.id.tvCurrentTemp)
        tvCurrentType = findViewById(R.id.tvCurrentType)
        ivCurrentIcon = findViewById(R.id.ivCurrentIcon)
        tvTodayRange = findViewById(R.id.tvTodayRange)
        tvWind = findViewById(R.id.tvWind)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvAir = findViewById(R.id.tvAir)
        tvPm = findViewById(R.id.tvPm)
        tvSun = findViewById(R.id.tvSun)
        tvNotice = findViewById(R.id.tvNotice)
        tvLifeSuggestion = findViewById(R.id.tvLifeSuggestion)
        llForecast = findViewById(R.id.llForecast)
    }

    private fun initListeners() {
        btnBack.setOnClickListener { finish() }
        btnRetry.setOnClickListener { loadWeather() }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val basePadding = dpToPx()
            view.setPadding(
                systemBars.left + basePadding,
                systemBars.top + basePadding,
                systemBars.right + basePadding,
                systemBars.bottom + basePadding
            )
            insets
        }
    }

    private fun loadWeather() {
        showLoading()
        val url = "http://t.weather.itboy.net/api/weather/city/$cityCode"

        val request = StringRequest(
            Request.Method.GET,
            url,
            { responseBody -> bindWeatherResponse(responseBody) },
            { error ->
                val detail = error.message ?: getString(R.string.weather_error_network_hint)
                showError(getString(R.string.weather_error_network, detail))
            }
        ).apply {
            tag = REQUEST_TAG
            retryPolicy = DefaultRetryPolicy(REQUEST_TIMEOUT_MS, REQUEST_MAX_RETRIES, 1.0f)
        }

        requestQueue.add(request)
    }

    private fun bindWeatherResponse(responseBody: String) {
        try {
            val weather = gson.fromJson(responseBody, WeatherResponse::class.java)
            val data = weather?.data
            if (data == null) {
                val fallback = getString(R.string.weather_error_response_invalid)
                showError(getString(R.string.weather_error_empty, weather?.message ?: fallback))
                return
            }

            showContent()
            val today = data.forecast?.firstOrNull()

            tvCityName.text = weather.cityInfo?.city?.takeIf { it.isNotBlank() } ?: cityName
            tvUpdateTime.text = getString(
                R.string.weather_update_time,
                weather.time ?: weather.date ?: "--",
                weather.cityInfo?.updateTime ?: "--"
            )
            tvCurrentTemp.text = getString(
                R.string.weather_current_temp,
                data.wendu?.takeIf { it.isNotBlank() } ?: "--"
            )
            tvCurrentType.text = today?.type ?: getString(R.string.weather_current_type_empty)
            ivCurrentIcon.setImageResource(WeatherIconMapper.forType(today?.type))

            tvTodayRange.text = getString(
                R.string.weather_today_range,
                today?.high ?: "--",
                today?.low ?: "--"
            )
            tvWind.text = getString(
                R.string.weather_wind,
                today?.fx ?: "--",
                today?.fl.orEmpty()
            ).trim()
            tvHumidity.text = getString(R.string.weather_humidity, data.shidu ?: "--")
            tvAir.text = getString(R.string.weather_air_quality, data.quality ?: "--")
            tvPm.text = getString(
                R.string.weather_pm,
                formatNumber(data.pm25),
                formatNumber(data.pm10)
            )
            tvSun.text = getString(
                R.string.weather_sun_time,
                today?.sunrise ?: "--",
                today?.sunset ?: "--"
            )
            tvNotice.text = getString(R.string.weather_notice, today?.notice ?: "--")
            tvLifeSuggestion.text = getString(R.string.weather_life_suggestion, data.ganmao ?: "--")

            bindForecastList(data.forecast.orEmpty())
        } catch (e: Exception) {
            showError(getString(R.string.weather_error_parse, e.message.orEmpty()))
        }
    }

    private fun bindForecastList(forecastList: List<Forecast>) {
        llForecast.removeAllViews()
        val inflater = LayoutInflater.from(this)

        forecastList.forEach { forecast ->
            val itemView = inflater.inflate(R.layout.item_forecast, llForecast, false)

            itemView.findViewById<ImageView>(R.id.ivForecastIcon)
                .setImageResource(WeatherIconMapper.forType(forecast.type))
            itemView.findViewById<TextView>(R.id.tvForecastDate).text = getString(
                R.string.weather_forecast_date,
                forecast.ymd ?: "--",
                forecast.week.orEmpty()
            ).trim()
            itemView.findViewById<TextView>(R.id.tvForecastType).text = forecast.type ?: "--"
            itemView.findViewById<TextView>(R.id.tvForecastTemp).text = getString(
                R.string.weather_forecast_temp,
                forecast.high ?: "--",
                forecast.low ?: "--"
            )
            itemView.findViewById<TextView>(R.id.tvForecastWind).text = getString(
                R.string.weather_forecast_wind,
                forecast.fx ?: "--",
                forecast.fl.orEmpty()
            ).trim()

            llForecast.addView(itemView)
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
        btnRetry.isEnabled = false
    }

    private fun showContent() {
        progressBar.visibility = View.GONE
        errorLayout.visibility = View.GONE
        scrollWeather.visibility = View.VISIBLE
        btnRetry.isEnabled = true
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        scrollWeather.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        tvError.text = message
        btnRetry.isEnabled = true
    }

    private fun formatNumber(value: Double?): String {
        if (value == null) return "--"
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }

    private fun dpToPx(): Int =
        (WEATHER_CONTENT_PADDING_DP * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        if (::requestQueue.isInitialized) {
            requestQueue.cancelAll(REQUEST_TAG)
        }
        super.onDestroy()
    }

    companion object {
        const val EXTRA_CITY_CODE = "extra_city_code"
        const val EXTRA_CITY_NAME = "extra_city_name"
        private const val DEFAULT_CITY_CODE = "101030100"
        private const val REQUEST_TAG = "weather_request"
        private const val REQUEST_TIMEOUT_MS = 10_000
        private const val REQUEST_MAX_RETRIES = 1
        private const val WEATHER_CONTENT_PADDING_DP = 16f
    }
}

package cn.edu.sicnu.cs.ys.exp_08

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var rootLayout: View
    private lateinit var etSearch: EditText
    private lateinit var tvStatus: TextView
    private lateinit var listView: ListView
    private lateinit var cityAdapter: CityAdapter

    private val allCities = mutableListOf<City>()
    private val visibleCities = mutableListOf<City>()
    private var provinceNames: Map<Int, String> = emptyMap()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        initViews()
        initListViewAndSearch()
        applyWindowInsets()
        loadCitiesFromRawInBackground()
    }

    private fun initViews() {
        rootLayout = findViewById(R.id.main)
        etSearch = findViewById(R.id.etSearch)
        tvStatus = findViewById(R.id.tvStatus)
        listView = findViewById(R.id.lvCities)
    }

    private fun initListViewAndSearch() {
        cityAdapter = CityAdapter(this, visibleCities, provinceNames)
        listView.adapter = cityAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val city = cityAdapter.getItem(position) ?: return@OnItemClickListener
            val cityCode = city.cityCode?.takeIf { it.isNotBlank() } ?: return@OnItemClickListener
            val intent = Intent(this, WeatherActivity::class.java).apply {
                putExtra(WeatherActivity.EXTRA_CITY_CODE, cityCode)
                putExtra(WeatherActivity.EXTRA_CITY_NAME, city.cityName ?: "天气详情")
            }
            startActivity(intent)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                filterCities(s?.toString().orEmpty())
            }
        })
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val basePadding = dpToPx(16)
            view.setPadding(
                systemBars.left + basePadding,
                systemBars.top + basePadding,
                systemBars.right + basePadding,
                systemBars.bottom + basePadding
            )
            insets
        }
    }

    /**
     * 实验要求：采用多线程读取本地城市列表 JSON 文件，同时使用 Gson 解析。
     */
    private fun loadCitiesFromRawInBackground() {
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = "正在使用子线程读取本地城市列表…"

        Thread({
            try {
                val json = resources.openRawResource(R.raw.citycode).use { inputStream ->
                    InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                        reader.readText()
                    }
                }

                val listType = object : TypeToken<List<City>>() {}.type
                val parsedCities: List<City> = Gson().fromJson(json, listType) ?: emptyList()

                provinceNames = parsedCities
                    .filter { it.pid == 0 && !it.cityName.isNullOrBlank() }
                    .associate { (it.id ?: 0) to it.cityName.orEmpty() }

                val citiesWithCode = parsedCities.filter {
                    !it.cityCode.isNullOrBlank() && !it.cityName.isNullOrBlank()
                }

                mainHandler.post {
                    allCities.clear()
                    allCities.addAll(citiesWithCode)
                    cityAdapter.updateProvinceNames(provinceNames)
                    filterCities(etSearch.text.toString())

                    tvStatus.visibility = if (citiesWithCode.isEmpty()) View.VISIBLE else View.GONE
                    if (citiesWithCode.isEmpty()) {
                        tvStatus.text = "未从 citycode.json 中解析到有效城市。"
                    }
                }
            } catch (e: Exception) {
                mainHandler.post {
                    tvStatus.visibility = View.VISIBLE
                    tvStatus.text = "城市列表读取或解析失败：${e.message}"
                }
            }
        }, "LoadCityJsonThread").start()
    }

    private fun filterCities(keyword: String) {
        val key = keyword.trim()
        val filtered = if (key.isBlank()) {
            allCities
        } else {
            allCities.filter { city ->
                city.cityName?.contains(key, ignoreCase = true) == true ||
                    city.cityCode?.contains(key) == true
            }
        }

        visibleCities.clear()
        visibleCities.addAll(filtered)
        cityAdapter.notifyDataSetChanged()

        if (allCities.isNotEmpty()) {
            tvStatus.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            tvStatus.text = "没有找到匹配“$key”的城市。"
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private inner class CityAdapter(
        context: Context,
        cities: List<City>,
        private var provinceNames: Map<Int, String>
    ) : ArrayAdapter<City>(context, android.R.layout.simple_list_item_2, android.R.id.text1, cities) {

        fun updateProvinceNames(names: Map<Int, String>) {
            provinceNames = names
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val itemView = super.getView(position, convertView, parent)
            val city = getItem(position)

            itemView.findViewById<TextView>(android.R.id.text1).text = city?.cityName ?: "未知城市"

            val provinceName = city?.pid?.let { provinceNames[it] }
                ?.takeIf { it.isNotBlank() && it != city.cityName }
            val code = city?.cityCode.orEmpty()
            itemView.findViewById<TextView>(android.R.id.text2).text =
                if (provinceName.isNullOrBlank()) {
                    "城市编码：$code"
                } else {
                    "$provinceName · 城市编码：$code"
                }

            return itemView
        }
    }
}

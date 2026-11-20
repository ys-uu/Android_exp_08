package cn.edu.sicnu.cs.ys.exp_08

import com.google.gson.annotations.SerializedName

/**
 * 本地 res/raw/citycode.json 中的城市数据。
 * 只保留列表展示和页面跳转需要的字段，Gson 会自动忽略 JSON 中的其他字段。
 */
data class City(
    val id: Int? = null,
    val pid: Int? = null,
    @SerializedName("city_code")
    val cityCode: String? = null,
    @SerializedName("city_name")
    val cityName: String? = null
)

data class WeatherResponse(
    val status: Int? = null,
    val message: String? = null,
    val date: String? = null,
    val time: String? = null,
    val cityInfo: WeatherCityInfo? = null,
    val data: WeatherData? = null
)

data class WeatherCityInfo(
    val city: String? = null,
    val citykey: String? = null,
    val parent: String? = null,
    val updateTime: String? = null
)

data class WeatherData(
    val shidu: String? = null,
    val pm25: Double? = null,
    val pm10: Double? = null,
    val quality: String? = null,
    val wendu: String? = null,
    val ganmao: String? = null,
    val forecast: List<Forecast>? = null
)

data class Forecast(
    val date: String? = null,
    val high: String? = null,
    val low: String? = null,
    val ymd: String? = null,
    val week: String? = null,
    val sunrise: String? = null,
    val sunset: String? = null,
    val aqi: Int? = null,
    val fx: String? = null,
    val fl: String? = null,
    val type: String? = null,
    val notice: String? = null
)

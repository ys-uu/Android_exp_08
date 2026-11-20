package cn.edu.sicnu.cs.ys.exp_08

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CityWeatherModelTest {

    @Test
    fun parseCityJsonFromRawResource() {
        val candidates = listOf(
            File("src/main/res/raw/citycode.json"),
            File("app/src/main/res/raw/citycode.json")
        )
        val jsonFile = candidates.firstOrNull { it.exists() }
        assertTrue("citycode.json should exist in res/raw", jsonFile != null)

        val json = jsonFile!!.readText(Charsets.UTF_8)
        val type = object : TypeToken<List<City>>() {}.type
        val cities: List<City> = Gson().fromJson(json, type)

        assertEquals(2532, cities.size)
        assertEquals("101010100", cities.first { it.cityName == "北京" }.cityCode)
        assertTrue(cities.count { !it.cityCode.isNullOrBlank() } > 2500)
    }

    @Test
    fun parseWeatherSampleJson() {
        val sample = """
            {
              "status": 200,
              "time": "2026-11-20 15:17:36",
              "cityInfo": {"city": "天津市", "citykey": "101030100", "updateTime": "11:23"},
              "data": {
                "shidu": "33%",
                "pm25": 15.0,
                "quality": "优",
                "wendu": "3.5",
                "ganmao": "各类人群可自由活动",
                "forecast": [
                  {"ymd": "2026-11-20", "week": "星期五", "type": "晴", "high": "高温 8℃", "low": "低温 0℃", "fx": "西北风", "fl": "2级"}
                ]
              }
            }
        """.trimIndent()

        val weather = Gson().fromJson(sample, WeatherResponse::class.java)

        assertEquals(200, weather.status)
        assertEquals("天津市", weather.cityInfo?.city)
        assertEquals("3.5", weather.data?.wendu)
        assertEquals("优", weather.data?.quality)
        assertEquals("晴", weather.data?.forecast?.firstOrNull()?.type)
        assertEquals("西北风", weather.data?.forecast?.firstOrNull()?.fx)
    }
}

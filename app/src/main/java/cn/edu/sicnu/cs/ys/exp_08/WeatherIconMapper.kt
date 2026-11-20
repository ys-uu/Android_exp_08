package cn.edu.sicnu.cs.ys.exp_08

import androidx.annotation.DrawableRes

/**
 * 将接口返回的天气文字映射到帮助材料中的本地图标。
 */
object WeatherIconMapper {

    @DrawableRes
    fun forType(weatherType: String?): Int {
        val type = weatherType.orEmpty()
        return when {
            type.contains("雷") -> R.drawable.thunder
            type.contains("雪") || type.contains("冰雹") -> R.drawable.snow
            type.contains("雨") -> R.drawable.rain
            type.contains("多云") -> R.drawable.mcloud
            type.contains("阴") -> R.drawable.cloud
            type.contains("晴") -> R.drawable.sun
            else -> R.drawable.mcloud
        }
    }
}

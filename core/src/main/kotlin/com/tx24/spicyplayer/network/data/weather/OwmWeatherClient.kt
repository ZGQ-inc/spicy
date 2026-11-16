package com.tx24.spicyplayer.network.data.weather

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WeatherApiState(
    val status: String = "Idle",
    val source: String = "None",
    val error: String? = null,
    val lastFetchTimeMs: Long = 0L,
)

/**
 * Clean data model representing current weather condition from OpenWeatherMap (OWM).
 */
data class CurrentWeather(
    val tempC: Float,
    val feelsLikeC: Float,
    val tempMinC: Float,
    val tempMaxC: Float,
    val humidity: Int,
    val condition: String,
    val description: String,
    val iconCode: String,
    val cityName: String,
    val updatedAtMs: Long = System.currentTimeMillis(),
) {
    val tempF: Float get() = (tempC * 9f / 5f) + 32f
    val tempMinF: Float get() = (tempMinC * 9f / 5f) + 32f
    val tempMaxF: Float get() = (tempMaxC * 9f / 5f) + 32f

    /**
     * Human-friendly emoji representation of OWM icon codes.
     */
    val emoji: String get() = when (iconCode.take(2)) {
        "01" -> if (iconCode.endsWith("n")) "🌙" else "☀️"
        "02" -> if (iconCode.endsWith("n")) "☁️" else "⛅"
        "03", "04" -> "☁️"
        "09" -> "🌧️"
        "10" -> "🌦️"
        "11" -> "⛈️"
        "13" -> "❄️"
        "50" -> "🌫️"
        else -> "⛅"
    }
}

/**
 * OpenWeatherMap 2.5 API client with smart caching and IP geolocation fallback.
 */
class OwmWeatherClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build(),
    private val gson: Gson = Gson(),
) {
    var logger: ((String) -> Unit)? = null

    private fun log(msg: String) {
        logger?.invoke(msg) ?: println("OwmWeather: $msg")
    }

    private var cachedWeather: CurrentWeather? = null
    var lastFetchTimeMs = 0L
        private set
    var lastSource: String = "None"
        private set
    var lastStatus: String = "Idle"
        private set
    var lastError: String? = null
        private set

    private val _weatherStateFlow = MutableStateFlow(WeatherApiState())
    val weatherStateFlow: StateFlow<WeatherApiState> = _weatherStateFlow.asStateFlow()

    fun setStatusConnecting() {
        lastStatus = "Fetching…"
        _weatherStateFlow.value = WeatherApiState(lastStatus, lastSource, lastError, lastFetchTimeMs)
    }

    private fun updateState(status: String, source: String, error: String?, timeMs: Long) {
        lastStatus = status
        lastSource = source
        lastError = error
        lastFetchTimeMs = timeMs
        _weatherStateFlow.value = WeatherApiState(status, source, error, timeMs)
    }

    /**
     * Cache duration in milliseconds (default 60 minutes to prevent API quota burn).
     */
    var cacheDurationMs: Long = 60 * 60 * 1000L

    /**
     * Fetches current weather. If [city] is blank, attempts IP-based geolocation.
     * Automatically falls back to Open-Meteo if OpenWeatherMap times out or fails.
     */
    fun getWeather(
        apiKey: String,
        city: String?,
        force: Boolean = false,
        lang: String = "zh_cn",
        allowFallback: Boolean = true,
    ): CurrentWeather? {
        val trimmedKey = apiKey.trim()
        val targetCity = city?.trim().orEmpty()
        val langParam = if (lang.equals("en", ignoreCase = true)) "en" else "zh_cn"
        val now = System.currentTimeMillis()
        if (!force && cachedWeather != null && (now - lastFetchTimeMs) < cacheDurationMs) {
            log("Returning cached weather: ${cachedWeather?.cityName} ${cachedWeather?.tempC}°C (source: $lastSource)")
            return cachedWeather
        }

        // 1. Try OpenWeatherMap if an API key is provided
        if (trimmedKey.isNotBlank()) {
            val url = if (targetCity.isNotBlank()) {
                val encodedCity = java.net.URLEncoder.encode(targetCity, "UTF-8").replace("+", "%20")
                "https://api.openweathermap.org/data/2.5/weather?q=${encodedCity}&appid=${trimmedKey}&units=metric&lang=$langParam"
            } else {
                val coords = getCoordinatesFromIp()
                if (coords != null) {
                    "https://api.openweathermap.org/data/2.5/weather?lat=${coords.first}&lon=${coords.second}&appid=${trimmedKey}&units=metric&lang=$langParam"
                } else null
            }

            if (url != null) {
                log("Requesting weather from OpenWeatherMap: $url")
                try {
                    val req = Request.Builder().url(url).build()
                    client.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val body = resp.body?.string()
                            if (!body.isNullOrBlank()) {
                                val raw = gson.fromJson(body, OwmResponse::class.java)
                                val condition = raw.weather?.firstOrNull()
                                val main = raw.main
                                if (main != null && condition != null) {
                                    val weather = CurrentWeather(
                                        tempC = main.temp,
                                        feelsLikeC = main.feelsLike,
                                        tempMinC = main.tempMin,
                                        tempMaxC = main.tempMax,
                                        humidity = main.humidity,
                                        condition = condition.main.orEmpty(),
                                        description = condition.description.orEmpty(),
                                        iconCode = condition.icon.orEmpty(),
                                        cityName = raw.name.orEmpty().ifBlank { targetCity },
                                        updatedAtMs = now,
                                    )
                                    cachedWeather = weather
                                    updateState("Connected (OWM)", "OpenWeatherMap", null, now)
                                    log("Weather updated via OWM: ${weather.cityName} ${weather.tempC}°C ${weather.description}")
                                    return weather
                                }
                            }
                        } else {
                            val err = resp.body?.string().orEmpty()
                            log("HTTP ${resp.code} from OWM: $err")
                            lastError = "OWM HTTP ${resp.code}"
                        }
                    }
                } catch (e: Exception) {
                    log("Exception fetching from OWM (${e.message}), attempting Open-Meteo fallback…")
                    lastError = "OWM: ${e.message}"
                }
            }
        }

        // 2. High-reliability fallback: Open-Meteo (No API key required, reliable worldwide / China)
        if (allowFallback) {
            log("Attempting weather lookup via Open-Meteo fallback for city '$targetCity'…")
            val fallbackResult = getWeatherFromOpenMeteo(targetCity, langParam)
            if (fallbackResult != null) {
                cachedWeather = fallbackResult
                updateState("Connected (Open-Meteo)", "Open-Meteo (Fallback)", null, now)
                log("Weather updated via Open-Meteo fallback: ${fallbackResult.cityName} ${fallbackResult.tempC}°C ${fallbackResult.description}")
                return fallbackResult
            }
        }

        updateState("Error: ${lastError ?: "Failed to fetch weather"}", lastSource, lastError, now)
        return cachedWeather
    }

    /**
     * Open-Meteo fallback implementation.
     */
    private fun getWeatherFromOpenMeteo(city: String, langParam: String = "zh_cn"): CurrentWeather? {
        val now = System.currentTimeMillis()
        var lat = 31.2304
        var lon = 121.4737
        var resolvedCityName = if (city.isNotBlank()) city else (if (langParam == "zh_cn") "上海" else "Shanghai")

        if (city.isNotBlank()) {
            try {
                val encCity = java.net.URLEncoder.encode(city, "UTF-8").replace("+", "%20")
                val geoLang = if (langParam == "en") "en" else "zh"
                val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=${encCity}&count=1&language=$geoLang&format=json"
                val req = Request.Builder().url(geoUrl).build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string()
                        if (!body.isNullOrBlank()) {
                            val geo = gson.fromJson(body, OpenMeteoGeoResponse::class.java)
                            val first = geo.results?.firstOrNull()
                            if (first != null && first.latitude != null && first.longitude != null) {
                                lat = first.latitude
                                lon = first.longitude
                                resolvedCityName = first.name.orEmpty().ifBlank { city }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                log("Open-Meteo geocoding exception: ${e.message}")
            }
        } else {
            val coords = getCoordinatesFromIp()
            if (coords != null) {
                lat = coords.first
                lon = coords.second
            }
        }

        val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code&daily=temperature_2m_max,temperature_2m_min&timezone=auto"
        return try {
            val req = Request.Builder().url(weatherUrl).build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    log("Open-Meteo HTTP ${resp.code}")
                    return null
                }
                val body = resp.body?.string() ?: return null
                val data = gson.fromJson(body, OpenMeteoWeatherResponse::class.java)
                val cur = data.current ?: return null
                val daily = data.daily
                val wmo = cur.weatherCode ?: 0
                val isDay = (cur.isDay ?: 1) == 1

                val (condition, zhDesc, icon) = mapWmoCode(wmo, isDay)
                val description = if (langParam == "zh_cn") zhDesc else condition
                val tempMin = daily?.tempMin?.firstOrNull() ?: (cur.temp - 3f)
                val tempMax = daily?.tempMax?.firstOrNull() ?: (cur.temp + 3f)

                CurrentWeather(
                    tempC = cur.temp,
                    feelsLikeC = cur.apparentTemp ?: cur.temp,
                    tempMinC = tempMin,
                    tempMaxC = tempMax,
                    humidity = cur.humidity ?: 50,
                    condition = condition,
                    description = description,
                    iconCode = icon,
                    cityName = resolvedCityName,
                    updatedAtMs = now,
                )
            }
        } catch (e: Exception) {
            log("Open-Meteo weather fetch exception: ${e.message}")
            null
        }
    }

    private fun mapWmoCode(code: Int, isDay: Boolean): Triple<String, String, String> {
        val suffix = if (isDay) "d" else "n"
        return when (code) {
            0 -> Triple("Clear", "晴", "01$suffix")
            1 -> Triple("Mainly Clear", "晴间多云", "02$suffix")
            2 -> Triple("Partly Cloudy", "多云", "02$suffix")
            3 -> Triple("Overcast", "阴", "04$suffix")
            45, 48 -> Triple("Fog", "雾", "50$suffix")
            51, 53, 55 -> Triple("Drizzle", "毛毛细雨", "09$suffix")
            56, 57 -> Triple("Freezing Drizzle", "冻雨", "09$suffix")
            61 -> Triple("Slight Rain", "小雨", "10$suffix")
            63 -> Triple("Moderate Rain", "中雨", "10$suffix")
            65 -> Triple("Heavy Rain", "大雨", "10$suffix")
            66, 67 -> Triple("Freezing Rain", "冻雨", "13$suffix")
            71 -> Triple("Slight Snow", "小雪", "13$suffix")
            73 -> Triple("Moderate Snow", "中雪", "13$suffix")
            75, 77 -> Triple("Heavy Snow", "暴雪", "13$suffix")
            80, 81, 82 -> Triple("Rain Showers", "阵雨", "09$suffix")
            85, 86 -> Triple("Snow Showers", "阵雪", "13$suffix")
            95 -> Triple("Thunderstorm", "雷暴", "11$suffix")
            96, 99 -> Triple("Thunderstorm with Hail", "雷暴伴有冰雹", "11$suffix")
            else -> Triple("Cloudy", "多云", "03$suffix")
        }
    }

    private data class OpenMeteoGeoResponse(
        val results: List<OpenMeteoGeoItem>?,
    )

    private data class OpenMeteoGeoItem(
        val name: String?,
        val latitude: Double?,
        val longitude: Double?,
    )

    private data class OpenMeteoWeatherResponse(
        val current: OpenMeteoCurrent?,
        val daily: OpenMeteoDaily?,
    )

    private data class OpenMeteoCurrent(
        @SerializedName("temperature_2m") val temp: Float,
        @SerializedName("relative_humidity_2m") val humidity: Int?,
        @SerializedName("apparent_temperature") val apparentTemp: Float?,
        @SerializedName("is_day") val isDay: Int?,
        @SerializedName("weather_code") val weatherCode: Int?,
    )

    private data class OpenMeteoDaily(
        @SerializedName("temperature_2m_max") val tempMax: List<Float>?,
        @SerializedName("temperature_2m_min") val tempMin: List<Float>?,
    )

    /**
     * IP Geolocation fallback when no city or GPS is configured on TV/speaker.
     */
    private fun getCoordinatesFromIp(): Pair<Double, Double>? {
        // Try HTTPS IP geolocation first
        try {
            val req = Request.Builder().url("https://ipapi.co/json/").build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string()
                    if (!body.isNullOrBlank()) {
                        val obj = gson.fromJson(body, IpapiCoResponse::class.java)
                        if (obj.latitude != null && obj.longitude != null) {
                            return Pair(obj.latitude, obj.longitude)
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // Fallback to ip-api
        return try {
            val req = Request.Builder().url("http://ip-api.com/json/?fields=status,lat,lon,city").build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string() ?: return null
                val ipInfo = gson.fromJson(body, IpApiResponse::class.java)
                if (ipInfo.status == "success" && ipInfo.lat != null && ipInfo.lon != null) {
                    Pair(ipInfo.lat, ipInfo.lon)
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private data class IpapiCoResponse(
        val latitude: Double?,
        val longitude: Double?,
        val city: String?,
    )

    private data class OwmResponse(
        val name: String?,
        val weather: List<OwmWeatherItem>?,
        val main: OwmMainItem?,
    )

    private data class OwmWeatherItem(
        val main: String?,
        val description: String?,
        val icon: String?,
    )

    private data class OwmMainItem(
        val temp: Float,
        @SerializedName("feels_like") val feelsLike: Float,
        @SerializedName("temp_min") val tempMin: Float,
        @SerializedName("temp_max") val tempMax: Float,
        val humidity: Int,
    )

    private data class IpApiResponse(
        val status: String?,
        val lat: Double?,
        val lon: Double?,
        val city: String?,
    )
}

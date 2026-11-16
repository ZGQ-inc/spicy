package com.tx24.spicyplayer.network.data.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OwmWeatherClientTest {

    @Test
    fun testCurrentWeatherConversionsAndEmojis() {
        val weather = CurrentWeather(
            tempC = 20.0f,
            feelsLikeC = 21.0f,
            tempMinC = 15.0f,
            tempMaxC = 25.0f,
            humidity = 60,
            condition = "Clear",
            description = "晴",
            iconCode = "01d",
            cityName = "Shanghai",
        )

        assertEquals(68.0f, weather.tempF, 0.1f)
        assertEquals(59.0f, weather.tempMinF, 0.1f)
        assertEquals(77.0f, weather.tempMaxF, 0.1f)
        assertEquals("☀️", weather.emoji)

        val nightRain = weather.copy(iconCode = "10n")
        assertEquals("🌦️", nightRain.emoji)

        val snow = weather.copy(iconCode = "13d")
        assertEquals("❄️", snow.emoji)
    }

    @Test
    fun testEmptyApiKeyReturnsNull() {
        val client = OwmWeatherClient()
        val result = client.getWeather("", "Beijing", allowFallback = false)
        assertTrue(result == null)
    }
}

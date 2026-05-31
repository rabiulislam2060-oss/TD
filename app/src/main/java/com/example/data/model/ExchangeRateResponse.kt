package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExchangeRateResponse(
    @Json(name = "result") val result: String,
    @Json(name = "base_code") val baseCode: String,
    @Json(name = "rates") val rates: Map<String, Double>,
    @Json(name = "time_last_update_utc") val timeLastUpdateUtc: String? = null
)

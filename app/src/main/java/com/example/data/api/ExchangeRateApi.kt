package com.example.data.api

import com.example.data.model.ExchangeRateResponse
import retrofit2.http.GET

interface ExchangeRateApi {
    @GET("v6/latest/USD")
    suspend fun getUsdRates(): ExchangeRateResponse
}

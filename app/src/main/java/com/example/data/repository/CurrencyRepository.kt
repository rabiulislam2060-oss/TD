package com.example.data.repository

import com.example.data.api.ExchangeRateApi
import com.example.data.db.ConversionDao
import com.example.data.db.ConversionRecord
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class CurrencyRepository(private val conversionDao: ConversionDao) {

    val allRecords: Flow<List<ConversionRecord>> = conversionDao.getAllRecords()

    private val api: ExchangeRateApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://open.er-api.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ExchangeRateApi::class.java)
    }

    suspend fun getLiveUsdBdtRate(): Double {
        val response = api.getUsdRates()
        if (response.result == "success") {
            return response.rates["BDT"] ?: throw Exception("BDT rate not found in response")
        } else {
            throw Exception("API returned error or failure")
        }
    }

    suspend fun insertRecord(record: ConversionRecord) {
        conversionDao.insertRecord(record)
    }

    suspend fun deleteRecordById(id: Int) {
        conversionDao.deleteRecordById(id)
    }

    suspend fun clearAllRecords() {
        conversionDao.clearAllRecords()
    }
}

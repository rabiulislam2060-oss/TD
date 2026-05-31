package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.ConversionRecord
import com.example.data.repository.CurrencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface RateState {
    object Loading : RateState
    data class Success(val rate: Double, val provider: String = "open.er-api.com") : RateState
    data class Error(val message: String) : RateState
}

class CurrencyViewModel(private val repository: CurrencyRepository) : ViewModel() {

    private val _rateState = MutableStateFlow<RateState>(RateState.Loading)
    val rateState: StateFlow<RateState> = _rateState.asStateFlow()

    private val _liveRate = MutableStateFlow(117.50) // default fallback
    val liveRate: StateFlow<Double> = _liveRate.asStateFlow()

    private val _isCustomRateEnabled = MutableStateFlow(false)
    val isCustomRateEnabled: StateFlow<Boolean> = _isCustomRateEnabled.asStateFlow()

    private val _customRate = MutableStateFlow(117.5)
    val customRate: StateFlow<Double> = _customRate.asStateFlow()

    private val _inputAmount = MutableStateFlow("1.00")
    val inputAmount: StateFlow<String> = _inputAmount.asStateFlow()

    private val _isUsdToBdt = MutableStateFlow(true)
    val isUsdToBdt: StateFlow<Boolean> = _isUsdToBdt.asStateFlow()

    val historyRecords: StateFlow<List<ConversionRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        fetchLiveRate()
    }

    fun fetchLiveRate() {
        viewModelScope.launch {
            _rateState.value = RateState.Loading
            try {
                val rate = repository.getLiveUsdBdtRate()
                _liveRate.value = rate
                if (!_isCustomRateEnabled.value) {
                    _customRate.value = rate
                }
                _rateState.value = RateState.Success(rate)
            } catch (e: Exception) {
                _rateState.value = RateState.Error(e.message ?: "Failed to fetch live rates")
            }
        }
    }

    fun setInputAmount(amount: String) {
        val clean = amount.filter { it.isDigit() || it == '.' }
        if (clean.count { it == '.' } <= 1) {
            _inputAmount.value = clean
        }
    }

    fun toggleDirection() {
        val currentInput = _inputAmount.value.toDoubleOrNull() ?: 0.0
        val currentRate = if (_isCustomRateEnabled.value) _customRate.value else _liveRate.value
        
        if (_isUsdToBdt.value) {
            val convertedBdt = currentInput * currentRate
            _inputAmount.value = if (convertedBdt == 0.0) "" else String.format("%.2f", convertedBdt)
        } else {
            val convertedUsd = if (currentRate > 0) currentInput / currentRate else 0.0
            _inputAmount.value = if (convertedUsd == 0.0) "" else String.format("%.2f", convertedUsd)
        }
        _isUsdToBdt.value = !_isUsdToBdt.value
    }

    fun setCustomRateEnabled(enabled: Boolean) {
        _isCustomRateEnabled.value = enabled
        if (!enabled) {
            _customRate.value = _liveRate.value
        }
    }

    fun setCustomRate(rate: Double) {
        _customRate.value = rate
    }

    fun saveCurrentConversion() {
        val input = _inputAmount.value.toDoubleOrNull() ?: return
        if (input <= 0.0) return
        
        val rate = if (_isCustomRateEnabled.value) _customRate.value else _liveRate.value
        val isUsdToBdtValue = _isUsdToBdt.value
        
        viewModelScope.launch {
            val amountUsd: Double
            val amountBdt: Double
            if (isUsdToBdtValue) {
                amountUsd = input
                amountBdt = input * rate
            } else {
                amountBdt = input
                amountUsd = if (rate > 0) input / rate else 0.0
            }
            
            repository.insertRecord(
                ConversionRecord(
                    amountUSD = amountUsd,
                    amountBDT = amountBdt,
                    isUsdToBdt = isUsdToBdtValue,
                    rate = rate
                )
            )
        }
    }

    fun deleteRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteRecordById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllRecords()
        }
    }

    fun loadRecord(record: ConversionRecord) {
        if (record.isUsdToBdt) {
            _isUsdToBdt.value = true
            _inputAmount.value = String.format("%.2f", record.amountUSD)
        } else {
            _isUsdToBdt.value = false
            _inputAmount.value = String.format("%.2f", record.amountBDT)
        }
        
        if (Math.abs(record.rate - _liveRate.value) > 0.001) {
            _isCustomRateEnabled.value = true
            _customRate.value = record.rate
        } else {
            _isCustomRateEnabled.value = false
        }
    }
}

class CurrencyViewModelFactory(private val repository: CurrencyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CurrencyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CurrencyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

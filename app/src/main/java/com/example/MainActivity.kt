package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.data.db.AppDatabase
import com.example.data.repository.CurrencyRepository
import com.example.ui.screens.ConverterScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CurrencyViewModel
import com.example.ui.viewmodel.CurrencyViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Thread-safe initialized database and repository setup
        val database = AppDatabase.getDatabase(this)
        val repository = CurrencyRepository(database.conversionDao())
        
        val viewModel: CurrencyViewModel by viewModels {
            CurrencyViewModelFactory(repository)
        }

        setContent {
            MyApplicationTheme {
                ConverterScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

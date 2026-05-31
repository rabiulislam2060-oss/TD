package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.ConversionRecord
import com.example.ui.theme.*
import com.example.ui.viewmodel.CurrencyViewModel
import com.example.ui.viewmodel.RateState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ConverterScreen(
    viewModel: CurrencyViewModel,
    modifier: Modifier = Modifier
) {
    val rateState by viewModel.rateState.collectAsStateWithLifecycle()
    val liveRate by viewModel.liveRate.collectAsStateWithLifecycle()
    val isCustomRateEnabled by viewModel.isCustomRateEnabled.collectAsStateWithLifecycle()
    val customRate by viewModel.customRate.collectAsStateWithLifecycle()
    val inputAmount by viewModel.inputAmount.collectAsStateWithLifecycle()
    val isUsdToBdt by viewModel.isUsdToBdt.collectAsStateWithLifecycle()
    val historyList by viewModel.historyRecords.collectAsStateWithLifecycle()

    val currentRate = if (isCustomRateEnabled) customRate else liveRate
    val parsedInput = inputAmount.toDoubleOrNull() ?: 0.0

    // Compute converted output amount
    val convertedAmount = if (isUsdToBdt) {
        parsedInput * currentRate
    } else {
        if (currentRate > 0) parsedInput / currentRate else 0.0
    }

    val focusManager = LocalFocusManager.current
    var isRateSectionExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable { focusManager.clearFocus() } // Tap to dismiss keyboard
    ) {
        Scaffold(
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "App Logo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Converter",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Dollar to Taka",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(
                            onClick = { viewModel.fetchLiveRate() },
                            modifier = Modifier.testTag("refresh_rate_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync indices",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("converter_main_scroll"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // LIVE RATE BRIEF pill
                item {
                    LiveRatePill(rateState = rateState, liveRate = liveRate)
                }

                // FROM CARD (Input card in clean grey)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                if (isSystemInDarkTheme()) CleanCardInputDark else CleanCardInputLight
                            )
                            .padding(20.dp)
                            .testTag("input_amount_row")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FROM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )

                            // Tag label info
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = MaterialTheme.colorScheme.background,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (isUsdToBdt) "USD" else "BDT",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Drop Menu",
                                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isUsdToBdt) "$ " else "৳ ",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )

                            BasicTextField(
                                value = inputAmount,
                                onValueChange = { viewModel.setInputAmount(it) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("currency_text_field"),
                                singleLine = true
                            )
                        }
                    }
                }

                // SWAP BUTTON (Overlapping aesthetics)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            onClick = { viewModel.toggleDirection() },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSystemInDarkTheme()) CleanPrimaryDark else CleanPrimaryLight,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("swap_currency_button")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CanvasVerticalSwap(
                                    modifier = Modifier.size(24.dp),
                                    color = if (isSystemInDarkTheme()) Color(0xFF001D36) else Color.White
                                )
                            }
                        }
                    }
                }

                // TO CARD (Output Card in clean soft indigo)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                if (isSystemInDarkTheme()) CleanCardOutputDark else CleanCardOutputLight
                            )
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                            .testTag("output_amount_row")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )

                            // Tag label info
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = MaterialTheme.colorScheme.background,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (isUsdToBdt) "BDT" else "USD",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Drop Menu",
                                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isUsdToBdt) "৳ " else "$ ",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Light,
                                color = if (isSystemInDarkTheme()) CleanOutputTextDark.copy(alpha = 0.6f) else CleanOutputTextLight.copy(alpha = 0.6f)
                            )

                            Text(
                                text = if (convertedAmount == 0.0) "0.00" else String.format("%.2f", convertedAmount),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Light,
                                color = if (isSystemInDarkTheme()) CleanOutputTextDark else CleanOutputTextLight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Rate Details",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "1 USD = ${String.format("%.2f", currentRate)} BDT • Refreshed live",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // SAVE IN LOGS CTA
                item {
                    Button(
                        onClick = {
                            viewModel.saveCurrentConversion()
                            focusManager.clearFocus()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSystemInDarkTheme()) CleanPrimaryDark else CleanPrimaryLight,
                            contentColor = if (isSystemInDarkTheme()) Color(0xFF001D36) else Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_record_button")
                    ) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "Log", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save to Conversion History",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // QUICK CHIPS / PRESETS TITLE
                item {
                    Text(
                        text = "Quick Input Presets",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                // QUICK CHIPS VALUES
                item {
                    val presets = if (isUsdToBdt) {
                        listOf(10.0, 50.0, 100.0, 500.0)
                    } else {
                        listOf(1000.0, 5000.0, 10000.0, 50000.0)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { quantity ->
                            PresetValueChip(
                                amount = quantity,
                                symbol = if (isUsdToBdt) "$" else "৳",
                                onClick = { viewModel.setInputAmount(quantity.toString()) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // REMITTANCE RATE OVERRIDE CARD
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isRateSectionExpanded = !isRateSectionExpanded }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Tune Override",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Custom Remittance Rate Override",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = if (isRateSectionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Collapse/Expand",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            AnimatedVisibility(visible = isRateSectionExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 18.dp)
                                ) {
                                    Text(
                                        text = "Modify your calculation based on local over-the-counter remitters (e.g. bKash agent networks, bank custom exchange corridors).",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        lineHeight = 15.sp
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (isCustomRateEnabled) "Custom Rate ACTIVE" else "Using Interbank Live Index",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isCustomRateEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Switch(
                                            checked = isCustomRateEnabled,
                                            onCheckedChange = { viewModel.setCustomRateEnabled(it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier.testTag("custom_rate_switch")
                                        )
                                    }

                                    if (isCustomRateEnabled) {
                                        Spacer(modifier = Modifier.height(14.dp))

                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Custom Rate Adjustment",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "${String.format("%.2f", customRate)} BDT per USD",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            Slider(
                                                value = customRate.toFloat(),
                                                onValueChange = { viewModel.setCustomRate(it.toDouble()) },
                                                valueRange = 100f..140f,
                                                steps = 399,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = MaterialTheme.colorScheme.primary,
                                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                                ),
                                                modifier = Modifier.testTag("custom_rate_slider")
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("100 BDT", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                                Text("120 BDT", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                                Text("140 BDT", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // HISTORIC LOGS HEADER
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, start = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saved History",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (historyList.isNotEmpty()) {
                            TextButton(
                                onClick = { viewModel.clearHistory() },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Wipe Logs", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Wipe All Logs", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // RECENT CONVERSIONS RECORD
                if (historyList.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.02f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Empty",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ready for conversions",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "Perform a calculation above and log it to save offline.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                } else {
                    items(historyList) { record ->
                        MinimalistHistoryItem(
                            record = record,
                            onItemClick = { viewModel.loadRecord(record) },
                            onDelete = { viewModel.deleteRecord(record.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun LiveRatePill(
    rateState: RateState,
    liveRate: Double
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (rateState is RateState.Error) MaterialTheme.colorScheme.error else Color(0xFF10B981)
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            when (rateState) {
                is RateState.Loading -> {
                    Text(
                        text = "Updating live exchange reference...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is RateState.Success -> {
                    Text(
                        text = "Interbank Exchange Rate: 1.00 USD = ${String.format("%.2f", rateState.rate)} BDT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is RateState.Error -> {
                    Text(
                        text = "Offline Index Mode: 1.00 USD = ${String.format("%.2f", liveRate)} BDT (Using cached)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun PresetValueChip(
    amount: Double,
    symbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)),
        modifier = modifier.height(38.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${symbol}${if (amount == amount.toInt().toDouble()) amount.toInt().toString() else amount.toString()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MinimalistHistoryItem(
    record: ConversionRecord,
    onItemClick: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    val dateStr = formatter.format(Date(record.timestamp))

    Surface(
        onClick = onItemClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (record.isUsdToBdt) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Conversion Direction Indicator",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$${String.format("%.2f", record.amountUSD)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "to",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "৳${String.format("%.2f", record.amountBDT)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Rate: 1 USD = ${String.format("%.2f", record.rate)} • $dateStr",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete conversion record",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun CanvasVerticalSwap(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Custom vertical double arrows
        // Left arrow points up
        val leftX = w * 0.35f
        drawLine(
            color = color,
            start = Offset(leftX, h * 0.8f),
            end = Offset(leftX, h * 0.2f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(leftX, h * 0.2f),
            end = Offset(leftX - 4.dp.toPx(), h * 0.35f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(leftX, h * 0.2f),
            end = Offset(leftX + 4.dp.toPx(), h * 0.35f),
            strokeWidth = 2.dp.toPx()
        )

        // Right arrow points down
        val rightX = w * 0.65f
        drawLine(
            color = color,
            start = Offset(rightX, h * 0.2f),
            end = Offset(rightX, h * 0.8f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(rightX, h * 0.8f),
            end = Offset(rightX - 4.dp.toPx(), h * 0.65f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(rightX, h * 0.8f),
            end = Offset(rightX + 4.dp.toPx(), h * 0.65f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

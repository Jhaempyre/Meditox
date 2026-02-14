package com.example.meditox.screens.sell

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.meditox.models.viewModel.SellMedicineViewModel
import com.example.meditox.models.viewModel.SellItemDetails
import com.example.meditox.models.viewModel.SellStockUiModel
import com.example.meditox.ui.theme.primaryGreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellMedicineScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: SellMedicineViewModel = viewModel(
        factory = SellMedicineViewModel.provideFactory(context)
    )
    val searchQuery by viewModel.searchQuery.collectAsState()
    val stocks by viewModel.filteredStocks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val detailsByBatchId by viewModel.detailsByBatchId.collectAsState()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.PartiallyExpanded }
    )
    var selectedDetails by remember { mutableStateOf<SellItemDetails?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Medicine Inventory",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryGreen,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { navController.navigate("add_medicine") }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Medicine",
                    tint = primaryGreen,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            label = { Text("Search medicines...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryGreen,
                focusedLabelColor = primaryGreen,
                focusedTextColor = primaryGreen


            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryGreen)
            }
        } else if (stocks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No stock found",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stocks, key = { it.batchStockId }) { item ->
                    SellStockRow(
                        item = item,
                        onClick = { selectedDetails = detailsByBatchId[item.batchStockId] },
                        onAdd = { println("cart added") }
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = Color(0xFFE8E8E8)
                    )
                }
            }
        }
    }

    if (selectedDetails != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedDetails = null },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            SellItemDetailsSheet(details = selectedDetails!!)
        }
    }
}

@Composable
private fun SellStockRow(
    item: SellStockUiModel,
    onClick: () -> Unit,
    onAdd: () -> Unit
) {
    val sellingPrice = item.sellingPrice?.let { "₹%.2f".format(it) } ?: "—"
    val mrp = item.mrp?.let { "₹%.2f".format(it) } ?: "—"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                if (item.strength != "...") {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.strength,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryGreen
                    )
                }
                if (item.isOldestBatch) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Older",
                            color = Color(0xFFD32F2F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = item.genericName,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Selling: $sellingPrice",
                    fontSize = 12.sp,
                    color = Color(0xFF555555)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "MRP: $mrp",
                    fontSize = 12.sp,
                    color = Color(0xFF555555)
                )
            }
            if (!item.batchNumber.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Batch: ${item.batchNumber}",
                    fontSize = 11.sp,
                    color = Color(0xFF888888)
                )
            }
        }

        IconButton(onClick = onAdd) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add to cart",
                tint = primaryGreen
            )
        }
    }
}

@Composable
private fun SellItemDetailsSheet(details: SellItemDetails) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp, max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = details.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Surface(
            color = primaryGreen.copy(alpha = 0.12f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = details.category,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = primaryGreen,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        details.details.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color(0xFF7A7A7A),
                    modifier = Modifier.weight(0.45f)
                )
                Text(
                    text = value,
                    fontSize = 12.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(0.55f)
                )
            }
            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color(0xFFF0F0F0)
            )
        }
    }
}

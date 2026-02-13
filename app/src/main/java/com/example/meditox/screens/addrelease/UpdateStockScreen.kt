package com.example.meditox.screens.addrelease

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.widget.Toast
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.meditox.enums.StockMonth
import com.example.meditox.enums.StockYear
import com.example.meditox.models.chemist.AddStockRequest
import com.example.meditox.models.viewModel.AddStockViewModel
import com.example.meditox.models.viewModel.AddStockResult
import com.example.meditox.models.viewModel.ProductUiModel
import com.example.meditox.models.viewModel.UpdateStockViewModel
import com.example.meditox.ui.theme.primaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateStockScreen(navController: NavController, wholesalerId: Long) {
    val context = LocalContext.current
    val viewModel: UpdateStockViewModel = viewModel(
        factory = UpdateStockViewModel.provideFactory(context)
    )
    val addStockViewModel: AddStockViewModel = viewModel(
        factory = AddStockViewModel.provideFactory(context)
    )
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isAddingStock by addStockViewModel.isLoading.collectAsState()
    val addStockResult by addStockViewModel.resultMessage.collectAsState()
    val addStockSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddStockSheet by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductUiModel?>(null) }

    // Filter Menu State
    var showFilterMenu by remember { mutableStateOf(false) }
    val categories = listOf(
        "None",
        "Drug",
        "Cosmetic",
        "FMCG",
        "Device",
        "Supplement",
        "Surgical"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Update Stock", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (showAddStockSheet && selectedProduct != null) {
            AddStockBottomSheet(
                sheetState = addStockSheetState,
                product = selectedProduct!!,
                wholesalerId = wholesalerId,
                isLoading = isAddingStock,
                result = addStockResult,
                onDismiss = {
                    addStockViewModel.clearResultMessage()
                    showAddStockSheet = false
                },
                onSubmit = { request ->
                    addStockViewModel.addStock(selectedProduct!!, request)
                },
                onSuccess = {
                    Toast.makeText(context, "Stock added successfully", Toast.LENGTH_SHORT).show()
                    addStockViewModel.clearResultMessage()
                    showAddStockSheet = false
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text("Search products...", color = Color.Gray)
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = primaryGreen
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryGreen,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    cursorColor = primaryGreen,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF9F9F9)
                ),
                singleLine = true
            )

            // Filter Button & Product Count Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box {
                    Button(
                        onClick = { showFilterMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCategory != null) primaryGreen.copy(alpha = 0.1f) else Color.Transparent,
                            contentColor = if (selectedCategory != null) primaryGreen else Color.Gray
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = if (selectedCategory == null) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)) else null
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Filter",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedCategory ?: "Filter by Category",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = category,
                                        color = if (category == (selectedCategory ?: "None")) primaryGreen else Color.Black,
                                        fontWeight = if (category == (selectedCategory ?: "None")) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.onCategorySelected(if (category == "None") null else category)
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "${filteredProducts.size} products",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }


            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryGreen)
                }
            } else if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No products found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        if (searchQuery.isNotEmpty() || selectedCategory != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your filters",
                                fontSize = 13.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    items(filteredProducts, key = { it.chemistProductId }) { product ->
                        ProductListItem(
                            product = product,
                            onClick = {
                                selectedProduct = product
                                showAddStockSheet = true
                            }
                        )
                        HorizontalDivider(
                            color = Color(0xFFE0E0E0),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductListItem(product: ProductUiModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Product Name + Strength
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = product.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                if (product.strength != "...") {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = product.strength,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = primaryGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Generic Name
            Text(
                text = product.genericName,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Category Badge
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = primaryGreen.copy(alpha = 0.1f),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = product.category,
                fontSize = 11.sp,
                color = primaryGreen,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddStockBottomSheet(
    sheetState: SheetState,
    product: ProductUiModel,
    wholesalerId: Long,
    isLoading: Boolean,
    result: AddStockResult?,
    onDismiss: () -> Unit,
    onSubmit: (AddStockRequest) -> Unit,
    onSuccess: () -> Unit
) {
    var batchNumber by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var mrp by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }

    var mfgDay by remember { mutableStateOf("1") }
    var mfgMonth by remember { mutableStateOf(StockMonth.JAN) }
    var mfgYear by remember { mutableStateOf(StockYear.Y2025) }

    var expDay by remember { mutableStateOf("1") }
    var expMonth by remember { mutableStateOf(StockMonth.JAN) }
    var expYear by remember { mutableStateOf(StockYear.Y2025) }

    LaunchedEffect(product.chemistProductId) {
        batchNumber = ""
        quantity = ""
        purchasePrice = ""
        mrp = ""
        sellingPrice = ""
        mfgDay = "1"
        mfgMonth = StockMonth.JAN
        mfgYear = StockYear.Y2025
        expDay = "1"
        expMonth = StockMonth.JAN
        expYear = StockYear.Y2025
    }

    LaunchedEffect(result) {
        if (result?.isSuccess == true) {
            onSuccess()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add Stock",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            OutlinedTextField(
                value = product.name,
                onValueChange = {},
                label = { Text("Product") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )

            OutlinedTextField(
                value = wholesalerId.toString(),
                onValueChange = {},
                label = { Text("Wholesaler ID") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )

            OutlinedTextField(
                value = batchNumber,
                onValueChange = { batchNumber = it },
                label = { Text("Batch Number") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )

            OutlinedTextField(
                value = purchasePrice,
                onValueChange = { purchasePrice = it },
                label = { Text("Purchase Price") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
            )

            DateSelectorRow(
                label = "MFG Date",
                day = mfgDay,
                onDayChange = { mfgDay = it },
                month = mfgMonth,
                onMonthChange = { mfgMonth = it },
                year = mfgYear,
                onYearChange = { mfgYear = it }
            )

            DateSelectorRow(
                label = "Expiry Date",
                day = expDay,
                onDayChange = { expDay = it },
                month = expMonth,
                onMonthChange = { expMonth = it },
                year = expYear,
                onYearChange = { expYear = it }
            )

            OutlinedTextField(
                value = mrp,
                onValueChange = { mrp = it },
                label = { Text("MRP") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = sellingPrice,
                onValueChange = { sellingPrice = it },
                label = { Text("Selling Price") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Button(
                onClick = {
                    val request = AddStockRequest(
                        chemistProductId = product.chemistProductId,
                        wholesalerId = wholesalerId,
                        batchNumber = batchNumber.ifBlank { null },
                        quantity = quantity.toIntOrNull() ?: 0,
                        purchasePrice = purchasePrice.toBigDecimalOrNull(),
                        expiryDate = formatDateForRequest(expDay, expMonth, expYear),
                        mfgDate = formatDateForRequest(mfgDay, mfgMonth, mfgYear),
                        mrp = mrp.toBigDecimalOrNull(),
                        sellingPrice = sellingPrice.toBigDecimalOrNull()
                    )
                    onSubmit(request)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoading) "Adding..." else "Add Stock", fontWeight = FontWeight.Bold)
            }

            if (result != null && !result.isSuccess) {
                Text(
                    text = result.message,
                    color = Color(0xFFD32F2F),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun DateSelectorRow(
    label: String,
    day: String,
    onDayChange: (String) -> Unit,
    month: StockMonth,
    onMonthChange: (StockMonth) -> Unit,
    year: StockYear,
    onYearChange: (StockYear) -> Unit
) {
    var showMonthMenu by remember { mutableStateOf(false) }
    var showYearMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = day,
                onValueChange = onDayChange,
                label = { Text("Day") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )

            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = month.label,
                    onValueChange = {},
                    label = { Text("Month") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMonthMenu = true },
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Month")
                    }
                )
                DropdownMenu(
                    expanded = showMonthMenu,
                    onDismissRequest = { showMonthMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    StockMonth.values().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                onMonthChange(option)
                                showMonthMenu = false
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = year.year.toString(),
                    onValueChange = {},
                    label = { Text("Year") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showYearMenu = true },
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Year")
                    }
                )
                DropdownMenu(
                    expanded = showYearMenu,
                    onDismissRequest = { showYearMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    StockYear.values().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.year.toString()) },
                            onClick = {
                                onYearChange(option)
                                showYearMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatDateForRequest(
    dayInput: String,
    month: StockMonth,
    year: StockYear
): String {
    val day = dayInput.toIntOrNull()?.coerceIn(1, 31) ?: 1
    return String.format("%04d-%02d-%02d", year.year, month.monthNumber, day)
}

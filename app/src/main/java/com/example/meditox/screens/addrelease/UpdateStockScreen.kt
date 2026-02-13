package com.example.meditox.screens.addrelease

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
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
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
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
                            imageVector = Icons.Default.Search,
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
                        ProductListItem(product = product)
                        HorizontalDivider(
                            color = Color(0xFFE0E0E0), // Darker separator
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
private fun ProductListItem(product: ProductUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
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

            Spacer(modifier = Modifier.height(2.dp))

            // Generic Name
            Text(
                text = product.genericName,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

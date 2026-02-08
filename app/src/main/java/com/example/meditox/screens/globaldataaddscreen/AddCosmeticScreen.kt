package com.example.meditox.screens.globaldataaddscreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.meditox.database.entity.GlobalCosmeticEntity
import com.example.meditox.models.viewModel.AddCosmeticViewModel
import com.example.meditox.models.viewModel.CosmeticFormState
import com.example.meditox.ui.theme.primaryGreen
import com.example.meditox.utils.ApiResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCosmeticScreen(
    navController: NavController,
    viewModel: AddCosmeticViewModel = viewModel(
        factory = AddCosmeticViewModel.provideFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current

    // Collect states from ViewModel
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val createResult by viewModel.createCosmeticResult.collectAsState()
    val selectedCosmetic by viewModel.selectedCosmetic.collectAsState()

    // Bottom sheet state for Add to Catalog
    var showAddToCatalogSheet by remember { mutableStateOf(false) }
    val addToCatalogSheetState = rememberModalBottomSheetState()

    // Handle API result
    LaunchedEffect(createResult) {
        when (val result = createResult) {
            is ApiResult.Success -> {
                Toast.makeText(context, "Cosmetic added successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateResult()
                // Instead of popping back, show the Add to Catalog sheet for the new cosmetic (which is already selected)
                showAddToCatalogSheet = true
            }
            is ApiResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                viewModel.resetCreateResult()
            }
            else -> { /* Loading or null - do nothing */ }
        }
    }
    
    // Handle manual selection from search results (optional if we want to add to catalog from search)
    // For now, simpler to just trigger sheet manually on click

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Cosmetic",
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
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
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Search Cosmetic") },
                placeholder = { Text("Enter product name or brand") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = primaryGreen) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryGreen,
                    focusedLabelColor = primaryGreen,
                    cursorColor = primaryGreen,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (searchQuery.isNotEmpty()) {
                if (searchResults.isEmpty() && searchQuery.length >= 2) {
                    // Show Add New Cosmetic Form
                    AddCosmeticForm(
                        formState = formState,
                        onUpdateField = viewModel::updateFormField,
                        onSubmit = viewModel::createCosmetic,
                        isLoading = uiState.isLoading
                    )
                } else {
                    // Show Search Results
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults) { cosmetic ->
                            CosmeticSearchResultCard(
                                cosmetic = cosmetic,
                                onClick = {
                                    viewModel.onCosmeticSelected(cosmetic)
                                    showAddToCatalogSheet = true
                                }
                            )
                        }
                    }
                }
            } else {
                // Initial State Hint
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Search for a cosmetic to add",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    // Add to Catalog Bottom Sheet
    if (showAddToCatalogSheet && selectedCosmetic != null) {
        AddToCatalogBottomSheet(
            cosmetic = selectedCosmetic!!,
            onDismiss = {
                showAddToCatalogSheet = false
                viewModel.clearSelectedCosmetic()
            },
            onSuccess = {
                showAddToCatalogSheet = false
                viewModel.clearSelectedCosmetic()
                navController.popBackStack()
            },
            sheetState = addToCatalogSheetState
        )
    }
}

@Composable
fun AddCosmeticForm(
    formState: CosmeticFormState,
    onUpdateField: (CosmeticFormState.() -> CosmeticFormState) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Product Not Found? Add New Cosmetic",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        // Fields based on CreateGlobalCosmeticRequest
        
        // Product Name & Brand
        CosmeticTextField(
            value = formState.productName,
            onValueChange = { onUpdateField { copy(productName = it) } },
            label = "Product Name *"
        )
        
        CosmeticTextField(
            value = formState.brand,
            onValueChange = { onUpdateField { copy(brand = it) } },
            label = "Brand *"
        )
        
        CosmeticTextField(
            value = formState.manufacturer,
            onValueChange = { onUpdateField { copy(manufacturer = it) } },
            label = "Manufacturer *"
        )

        // Details Row 1
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CosmeticTextField(
                value = formState.form,
                onValueChange = { onUpdateField { copy(form = it) } },
                label = "Form (e.g. Cream, Gel) *",
                modifier = Modifier.weight(1f)
            )
            CosmeticTextField(
                value = formState.variant,
                onValueChange = { onUpdateField { copy(variant = it) } },
                label = "Variant *",
                modifier = Modifier.weight(1f)
            )
        }

        // Details Row 2
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CosmeticTextField(
                value = formState.baseUnit,
                onValueChange = { onUpdateField { copy(baseUnit = it) } },
                label = "Base Unit (e.g. ml, g) *",
                modifier = Modifier.weight(1f)
            )
            CosmeticTextField(
                value = formState.unitsPerPack,
                onValueChange = { onUpdateField { copy(unitsPerPack = it) } },
                label = "Units/Pack *",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }

        // Use & Skin Type
        CosmeticTextField(
            value = formState.intendedUse,
            onValueChange = { onUpdateField { copy(intendedUse = it) } },
            label = "Intended Use *"
        )
        
        CosmeticTextField(
            value = formState.skinType,
            onValueChange = { onUpdateField { copy(skinType = it) } },
            label = "Skin Type *"
        )

        // Regulatory
        CosmeticTextField(
            value = formState.cosmeticLicenseNumber,
            onValueChange = { onUpdateField { copy(cosmeticLicenseNumber = it) } },
            label = "License Number *"
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CosmeticTextField(
                value = formState.hsnCode,
                onValueChange = { onUpdateField { copy(hsnCode = it) } },
                label = "HSN Code *",
                modifier = Modifier.weight(1f)
            )
            CosmeticTextField(
                value = formState.gstRate,
                onValueChange = { onUpdateField { copy(gstRate = it) } },
                label = "GST Rate (%) *",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = "Add Cosmetic", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CosmeticTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primaryGreen,
            focusedLabelColor = primaryGreen,
            cursorColor = primaryGreen,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true
    )
}

@Composable
fun CosmeticSearchResultCard(
    cosmetic: GlobalCosmeticEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cosmetic.productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "${cosmetic.form} • ${cosmetic.brand}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryGreen
                )
            }
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = primaryGreen
            )
        }
    }
}
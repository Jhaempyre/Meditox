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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.meditox.database.entity.GlobalSupplementEntity
import com.example.meditox.models.viewModel.AddSupplementViewModel
import com.example.meditox.models.viewModel.SupplementFormState
import com.example.meditox.ui.theme.primaryGreen
import com.example.meditox.utils.ApiResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSupplementScreen(
    navController: NavController,
    viewModel: AddSupplementViewModel = viewModel(
        factory = AddSupplementViewModel.provideFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current

    // Collect states
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val createResult by viewModel.createSupplementResult.collectAsState()
    val selectedSupplement by viewModel.selectedSupplement.collectAsState()

    // Bottom sheet state
    var showAddToCatalogSheet by remember { mutableStateOf(false) }
    val addToCatalogSheetState = rememberModalBottomSheetState()

    // Handle API result
    LaunchedEffect(createResult) {
        when (val result = createResult) {
            is ApiResult.Success -> {
                Toast.makeText(context, "Supplement added successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateResult()
                showAddToCatalogSheet = true
            }
            is ApiResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                viewModel.resetCreateResult()
            }
            else -> { }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Supplement",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
                label = { Text("Search Supplement") },
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
                    // Show Add New Form
                    AddSupplementForm(
                        formState = formState,
                        onUpdateField = viewModel::updateFormField,
                        onSubmit = viewModel::createSupplement,
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
                        items(searchResults) { supplement ->
                            SupplementSearchResultCard(
                                supplement = supplement,
                                onClick = {
                                    viewModel.onSupplementSelected(supplement)
                                    showAddToCatalogSheet = true
                                }
                            )
                        }
                    }
                }
            } else {
                // Initial Hint
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Search for a supplement to add",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    // Add to Catalog Bottom Sheet
    if (showAddToCatalogSheet && selectedSupplement != null) {
        AddToCatalogBottomSheet(
            supplement = selectedSupplement!!,
            onDismiss = {
                showAddToCatalogSheet = false
                viewModel.clearSelectedSupplement()
            },
            onSuccess = {
                showAddToCatalogSheet = false
                viewModel.clearSelectedSupplement()
                navController.popBackStack()
            },
            sheetState = addToCatalogSheetState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSupplementForm(
    formState: SupplementFormState,
    onUpdateField: (SupplementFormState.() -> SupplementFormState) -> Unit,
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
            text = "Supplement Not Found? Add New",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        SupplementTextField(
            value = formState.productName,
            onValueChange = { onUpdateField { copy(productName = it) } },
            label = "Product Name *"
        )

        SupplementTextField(
            value = formState.brand,
            onValueChange = { onUpdateField { copy(brand = it) } },
            label = "Brand *"
        )

        SupplementTextField(
            value = formState.manufacturer,
            onValueChange = { onUpdateField { copy(manufacturer = it) } },
            label = "Manufacturer *"
        )

        SupplementTextField(
            value = formState.supplementType,
            onValueChange = { onUpdateField { copy(supplementType = it) } },
            label = "Supplement Type (e.g. Vitamin, Protein) *"
        )

        SupplementTextField(
            value = formState.compositionSummary,
            onValueChange = { onUpdateField { copy(compositionSummary = it) } },
            label = "Composition Summary *"
        )

        // Age Group Dropdown
        var ageGroupExpanded by remember { mutableStateOf(false) }
        val ageGroupOptions = listOf("ADULT", "CHILDREN", "OLD")

        ExposedDropdownMenuBox(
            expanded = ageGroupExpanded,
            onExpandedChange = { ageGroupExpanded = !ageGroupExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = formState.ageGroup,
                onValueChange = {},
                readOnly = true,
                label = { Text("Age Group *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ageGroupExpanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryGreen,
                    focusedLabelColor = primaryGreen,
                    cursorColor = primaryGreen,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = ageGroupExpanded,
                onDismissRequest = { ageGroupExpanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                ageGroupOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option, color = Color.Black) },
                        onClick = {
                            onUpdateField { copy(ageGroup = option) }
                            ageGroupExpanded = false
                        }
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SupplementTextField(
                value = formState.baseUnit,
                onValueChange = { onUpdateField { copy(baseUnit = it) } },
                label = "Base Unit *",
                modifier = Modifier.weight(1f)
            )
            SupplementTextField(
                value = formState.unitsPerPack,
                onValueChange = { onUpdateField { copy(unitsPerPack = it) } },
                label = "Units/Pack *",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }

        SupplementTextField(
            value = formState.fssaiLicense,
            onValueChange = { onUpdateField { copy(fssaiLicense = it) } },
            label = "FSSAI License"
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SupplementTextField(
                value = formState.hsnCode,
                onValueChange = { onUpdateField { copy(hsnCode = it) } },
                label = "HSN Code *",
                modifier = Modifier.weight(1f)
            )
            SupplementTextField(
                value = formState.gstRate,
                onValueChange = { onUpdateField { copy(gstRate = it) } },
                label = "GST Rate (%) *",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f)
            )
        }

        // Loose Sale Allowed Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Loose Sale Allowed", fontSize = 14.sp, color = Color.Black)
            Switch(
                checked = formState.isLooseSaleAllowed,
                onCheckedChange = { onUpdateField { copy(isLooseSaleAllowed = it) } },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = primaryGreen
                )
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
                Text(text = "Add Supplement", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SupplementTextField(
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
fun SupplementSearchResultCard(
    supplement: GlobalSupplementEntity,
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
                    text = supplement.productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "${supplement.supplementType} • ${supplement.brand}",
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
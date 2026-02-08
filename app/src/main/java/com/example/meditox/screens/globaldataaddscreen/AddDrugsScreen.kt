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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
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
import com.example.meditox.database.entity.GlobalDrugEntity
import com.example.meditox.enums.DosageForm
import com.example.meditox.enums.DrugSchedule
import com.example.meditox.enums.RouteOfAdministration
import com.example.meditox.enums.SystemOfMedicine
import com.example.meditox.models.viewModel.AddDrugViewModel
import com.example.meditox.models.viewModel.DrugFormState
import com.example.meditox.ui.theme.primaryGreen
import com.example.meditox.utils.ApiResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDrugScreen(
    navController: NavController,
    viewModel: AddDrugViewModel = viewModel(
        factory = AddDrugViewModel.provideFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current

    // Collect states from ViewModel
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val createResult by viewModel.createDrugResult.collectAsState()

    // Bottom sheet state for Add to Catalog
    var showAddToCatalogSheet by remember { mutableStateOf(false) }
    var selectedDrug by remember { mutableStateOf<GlobalDrugEntity?>(null) }
    val addToCatalogSheetState = rememberModalBottomSheetState()

    // Handle API result
    LaunchedEffect(createResult) {
        when (val result = createResult) {
            is ApiResult.Success -> {
                Toast.makeText(context, "Drug added successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateResult()
                // Instead of popping back, show the Add to Catalog sheet for the new drug
                selectedDrug = result.data
                showAddToCatalogSheet = true
            }
            is ApiResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                viewModel.resetCreateResult()
            }
            else -> { /* Loading or null - do nothing */ }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Drug",
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
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search drug by brand name...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = primaryGreen
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryGreen,
                    cursorColor = primaryGreen,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Search Results OR Form
            when {
                searchQuery.isNotBlank() && searchResults.isNotEmpty() -> {
                    // Show search results
                    Text(
                        text = "Found ${searchResults.size} matches",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults) { drug ->
                            DrugSearchResultCard(
                                drug = drug,
                                onClick = {
                                    selectedDrug = drug
                                    showAddToCatalogSheet = true
                                }
                            )
                        }
                    }
                }
                searchQuery.isNotBlank() && searchResults.isEmpty() -> {
                    // No match found - show add new form
                    Text(
                        text = "No drugs found. Add a new drug below:",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Form Section
                    DrugFormContent(
                        formState = formState,
                        onFormUpdate = { update -> viewModel.updateFormField(update) },
                        isLoading = uiState.isLoading,
                        isFormValid = viewModel.isFormValid(),
                        onSubmit = { viewModel.createDrug() },
                        modifier = Modifier.weight(1f)
                    )
                }
                else -> {
                    // No search yet - show instruction
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Start typing to search for drugs",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Add to Catalog Bottom Sheet
    if (showAddToCatalogSheet && selectedDrug != null) {
        AddToCatalogBottomSheet(
            drug = selectedDrug!!,
            onDismiss = {
                showAddToCatalogSheet = false
                selectedDrug = null
            },
            onSuccess = {
                showAddToCatalogSheet = false
                selectedDrug = null
                navController.popBackStack()
            },
            sheetState = addToCatalogSheetState
        )
    }
}

@Composable
private fun DrugFormContent(
    formState: DrugFormState,
    onFormUpdate: ((DrugFormState) -> DrugFormState) -> Unit,
    isLoading: Boolean,
    isFormValid: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Brand Name
        FormTextField(
            value = formState.brandName,
            onValueChange = { onFormUpdate { it.copy(brandName = it.brandName) }; onFormUpdate { form -> form.copy(brandName = it) } },
            label = "Brand Name *",

        )

        // Generic Name
        FormTextField(
            value = formState.genericName,
            onValueChange = { onFormUpdate { form -> form.copy(genericName = it) } },
            label = "Generic Name *"
        )

        // Manufacturer
        FormTextField(
            value = formState.manufacturer,
            onValueChange = { onFormUpdate { form -> form.copy(manufacturer = it) } },
            label = "Manufacturer *"
        )

        // Strength Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FormTextField(
                value = formState.strengthValue,
                onValueChange = { onFormUpdate { form -> form.copy(strengthValue = it) } },
                label = "Strength *",
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Decimal
            )
            FormTextField(
                value = formState.strengthUnit,
                onValueChange = { onFormUpdate { form -> form.copy(strengthUnit = it) } },
                label = "Unit",
                modifier = Modifier.weight(1f)
            )
        }

        // System of Medicine Dropdown
        EnumDropdown(
            label = "System of Medicine",
            selectedValue = formState.systemOfMedicine.name,
            options = SystemOfMedicine.values().map { it.name },
            onOptionSelected = { onFormUpdate { form -> form.copy(systemOfMedicine = SystemOfMedicine.valueOf(it)) } }
        )

        // Dosage Form Dropdown
        EnumDropdown(
            label = "Dosage Form",
            selectedValue = formState.dosageForm.name,
            options = DosageForm.values().map { it.name },
            onOptionSelected = { onFormUpdate { form -> form.copy(dosageForm = DosageForm.valueOf(it)) } }

        )

        // Route of Administration Dropdown
        EnumDropdown(
            label = "Route of Administration",
            selectedValue = formState.routeOfAdministration.name,
            options = RouteOfAdministration.values().map { it.name },
            onOptionSelected = { onFormUpdate { form -> form.copy(routeOfAdministration = RouteOfAdministration.valueOf(it)) } }
        )

        // Drug Schedule Dropdown
        EnumDropdown(
            label = "Drug Schedule",
            selectedValue = formState.drugSchedule.name,
            options = DrugSchedule.values().map { it.name },
            onOptionSelected = { onFormUpdate { form -> form.copy(drugSchedule = DrugSchedule.valueOf(it)) } }
        )

        // Base Unit & Units per Pack
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FormTextField(
                value = formState.baseUnit,
                onValueChange = { onFormUpdate { form -> form.copy(baseUnit = it) } },
                label = "Base Unit",
                modifier = Modifier.weight(1f)
            )
            FormTextField(
                value = formState.unitsPerPack,
                onValueChange = { onFormUpdate { form -> form.copy(unitsPerPack = it) } },
                label = "Units/Pack",
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Number
            )
        }

        // HSN Code & Regulatory Authority
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FormTextField(
                value = formState.hsnCode,
                onValueChange = { onFormUpdate { form -> form.copy(hsnCode = it) } },
                label = "HSN Code",
                modifier = Modifier.weight(1f)
            )
            FormTextField(
                value = formState.regulatoryAuthority,
                onValueChange = { onFormUpdate { form -> form.copy(regulatoryAuthority = it) } },
                label = "Reg. Authority",
                modifier = Modifier.weight(1f)
            )
        }

        // Boolean Switches
        SwitchOption(
            label = "Loose Sale Allowed",
            checked = formState.looseSaleAllowed,
            onCheckedChange = { onFormUpdate { form -> form.copy(looseSaleAllowed = it) } }
        )
        SwitchOption(
            label = "Prescription Required",
            checked = formState.prescriptionRequired,
            onCheckedChange = { onFormUpdate { form -> form.copy(prescriptionRequired = it) } }
        )
        SwitchOption(
            label = "Narcotic Drug",
            checked = formState.narcoticDrug,
            onCheckedChange = { onFormUpdate { form -> form.copy(narcoticDrug = it) } }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading && isFormValid,
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Add Drug", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DrugSearchResultCard(
    drug: GlobalDrugEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = drug.brandName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = "${drug.strengthValue}${drug.strengthUnit}",
                    fontSize = 14.sp,
                    color = primaryGreen
                )
            }
        }
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primaryGreen,
            focusedLabelColor = primaryGreen,
            cursorColor = primaryGreen,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnumDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = primaryGreen
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryGreen,
                focusedLabelColor = primaryGreen,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SwitchOption(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Black)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = primaryGreen
            )
        )
    }
}
package com.example.meditox.screens.globaldataaddscreen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meditox.database.entity.GlobalDrugEntity
import com.example.meditox.enums.ProductCategory
import com.example.meditox.models.viewModel.AddToCatalogViewModel
import com.example.meditox.ui.theme.primaryGreen
import com.example.meditox.utils.ApiResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCatalogBottomSheet(
    drug: GlobalDrugEntity,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit = {},
    sheetState: SheetState,
    viewModel: AddToCatalogViewModel = viewModel(
        factory = AddToCatalogViewModel.provideFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val addResult by viewModel.addResult.collectAsState()

    // Handle result
    LaunchedEffect(addResult) {
        when (val result = addResult) {
            is ApiResult.Success -> {
                Toast.makeText(context, "Product added to catalog!", Toast.LENGTH_SHORT).show()
                viewModel.resetResult()
                viewModel.resetForm()
                onSuccess()
                onDismiss()
            }
            is ApiResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                viewModel.resetResult()
            }
            else -> {}
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.resetForm()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        AddToCatalogContent(
            title = drug.brandName,
            subtitle = "${drug.strengthValue}${drug.strengthUnit} • ${drug.manufacturer}",
            formState = formState,
            uiState = uiState,
            onUpdateField = viewModel::updateFormField,
            onSubmit = { viewModel.addProductToCatalog(drug.globalDrugId, "DRUG") }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCatalogBottomSheet(
    cosmetic: com.example.meditox.database.entity.GlobalCosmeticEntity,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit = {},
    sheetState: SheetState,
    viewModel: AddToCatalogViewModel = viewModel(
        factory = AddToCatalogViewModel.provideFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val addResult by viewModel.addResult.collectAsState()

    // Handle result
    LaunchedEffect(addResult) {
        when (val result = addResult) {
            is ApiResult.Success -> {
                Toast.makeText(context, "Product added to catalog!", Toast.LENGTH_SHORT).show()
                viewModel.resetResult()
                viewModel.resetForm()
                onSuccess()
                onDismiss()
            }
            is ApiResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                viewModel.resetResult()
            }
            else -> {}
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.resetForm()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        AddToCatalogContent(
            title = cosmetic.productName,
            subtitle = "${cosmetic.form} • ${cosmetic.brand}",
            formState = formState,
            uiState = uiState,
            onUpdateField = viewModel::updateFormField,
            onSubmit = { viewModel.addProductToCatalog(cosmetic.globalCosmeticId, "COSMETIC") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCatalogBottomSheet(
    fmcg: com.example.meditox.database.entity.GlobalGeneralFmcgEntity,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit = {},
    sheetState: SheetState,
    viewModel: AddToCatalogViewModel = viewModel(
        factory = AddToCatalogViewModel.provideFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val addResult by viewModel.addResult.collectAsState()

    // Handle result
    LaunchedEffect(addResult) {
        when (val result = addResult) {
            is ApiResult.Success -> {
                Toast.makeText(context, "Product added to catalog!", Toast.LENGTH_SHORT).show()
                viewModel.resetResult()
                viewModel.resetForm()
                onSuccess()
                onDismiss()
            }
            is ApiResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                viewModel.resetResult()
            }
            else -> {}
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.resetForm()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        AddToCatalogContent(
            title = fmcg.productName,
            subtitle = "${fmcg.categoryLabel} • ${fmcg.brand}",
            formState = formState,
            uiState = uiState,
            onUpdateField = viewModel::updateFormField,
            onSubmit = { viewModel.addProductToCatalog(fmcg.globalFmcgId, ProductCategory.GENERAL_FMCG.toString()) }
        )
    }
}

@Composable
fun AddToCatalogContent(
    title: String,
    subtitle: String,
    formState: com.example.meditox.models.viewModel.CatalogFormState,
    uiState: com.example.meditox.models.viewModel.AddToCatalogUiState,
    onUpdateField: (com.example.meditox.models.viewModel.CatalogFormState.() -> com.example.meditox.models.viewModel.CatalogFormState) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "Add to Catalog",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Configure stock parameters for this product",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Product Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    color = primaryGreen,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Form Fields
        Text(
            text = "Stock Management",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = formState.minimumStockLevel,
            onValueChange = { onUpdateField { copy(minimumStockLevel = it) } },
            label = { Text("Minimum Stock Level *") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryGreen,
                focusedLabelColor = primaryGreen,
                cursorColor = primaryGreen,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = formState.maximumStockLevel,
            onValueChange = { onUpdateField { copy(maximumStockLevel = it) } },
            label = { Text("Maximum Stock Level *") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryGreen,
                focusedLabelColor = primaryGreen,
                cursorColor = primaryGreen,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = formState.reorderQuantity,
            onValueChange = { onUpdateField { copy(reorderQuantity = it) } },
            label = { Text("Reorder Quantity *") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryGreen,
                focusedLabelColor = primaryGreen,
                cursorColor = primaryGreen,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Add to Catalog", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

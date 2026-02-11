package com.example.meditox.screens.wholesaler

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.meditox.models.viewModel.AddWholesalerViewModel
import com.example.meditox.models.viewModel.WholesalerFormState
import com.example.meditox.ui.theme.darkGreen
import com.example.meditox.ui.theme.lighterGreen
import com.example.meditox.ui.theme.primaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWholesalerScreen(navController: NavController, wholesalerId: Long? = null) {
    val context = LocalContext.current
    val viewModel: AddWholesalerViewModel = viewModel(
        factory = AddWholesalerViewModel.provideFactory(context)
    )

    LaunchedEffect(wholesalerId) {
        if (wholesalerId != null) {
            viewModel.loadWholesaler(wholesalerId)
        }
    }

    val formState by viewModel.formState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val resultMessage by viewModel.resultMessage.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()

    // Handle result messages
    LaunchedEffect(resultMessage) {
        resultMessage?.let { result ->
            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            if (result.isSuccess) {
                navController.popBackStack()
            }
            viewModel.clearResultMessage()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(primaryGreen, darkGreen)
                            )
                        )
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = if (isEditMode) "Edit Wholesaler" else "Add Wholesaler",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Section: Business Info ──
            SectionCard(title = "Business Information", icon = Icons.Default.AccountBox) {
                FormTextField(
                    value = formState.wholesalerName,
                    onValueChange = { v -> viewModel.updateFormField { it.copy(wholesalerName = v) } },
                    label = "Wholesaler Name *",
                    icon = Icons.Default.AccountBox
                )
                FormTextField(
                    value = formState.gstin,
                    onValueChange = { v -> viewModel.updateFormField { it.copy(gstin = v) } },
                    label = "GSTIN (15 chars) *",
                    icon = Icons.Default.Info,
                    maxLength = 15
                )
                FormTextField(
                    value = formState.drugLicenseNumber,
                    onValueChange = { v -> viewModel.updateFormField { it.copy(drugLicenseNumber = v) } },
                    label = "Drug License Number *",
                    icon = Icons.Default.Create
                )
                FormTextField(
                    value = formState.stateCode,
                    onValueChange = { v -> viewModel.updateFormField { it.copy(stateCode = v) } },
                    label = "State Code (2 digits) *",
                    icon = Icons.Default.Star,
                    keyboardType = KeyboardType.Number,
                    maxLength = 2
                )
            }

            // ── Section: Contact Info ──
            SectionCard(title = "Contact Information", icon = Icons.Default.Person) {
                FormTextField(
                    value = formState.contactPerson,
                    onValueChange = { v -> viewModel.updateFormField { it.copy(contactPerson = v) } },
                    label = "Contact Person *",
                    icon = Icons.Default.Person
                )
                FormTextField(
                    value = formState.phoneNumber,
                    onValueChange = { v -> viewModel.updateFormField { it.copy(phoneNumber = v) } },
                    label = "Phone Number *",
                    icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone
                )
                FormTextField(
                    value = formState.email,
                    onValueChange = { v -> viewModel.updateFormField { it.copy(email = v) } },
                    label = "Email *",
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email
                )
            }

            // ── Section: Address ──
            SectionCard(title = "Address", icon = Icons.Default.LocationOn) {
                FormTextField(
                    value = formState.addressLine1,
                    onValueChange = { v -> viewModel.updateFormField { it.copy(addressLine1 = v) } },
                    label = "Address Line 1 *",
                    icon = Icons.Default.Home
                )
                FormTextField(
                    value = formState.addressLine2,
                    onValueChange = { v -> viewModel.updateFormField { it.copy(addressLine2 = v) } },
                    label = "Address Line 2",
                    icon = Icons.Default.Home
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormTextField(
                        value = formState.city,
                        onValueChange = { v -> viewModel.updateFormField { it.copy(city = v) } },
                        label = "City *",
                        icon = Icons.Default.LocationOn,
                        modifier = Modifier.weight(1f)
                    )
                    FormTextField(
                        value = formState.state,
                        onValueChange = { v -> viewModel.updateFormField { it.copy(state = v) } },
                        label = "State *",
                        icon = Icons.Default.Place,
                        modifier = Modifier.weight(1f)
                    )
                }
                FormTextField(
                    value = formState.pincode,
                    onValueChange = { v -> viewModel.updateFormField { it.copy(pincode = v) } },
                    label = "Pincode (6 digits) *",
                    icon = Icons.Default.Place,
                    keyboardType = KeyboardType.Number,
                    maxLength = 6
                )
            }

            // ── Section: Credit Terms ──
            SectionCard(title = "Credit Terms", icon = Icons.Default.ShoppingCart) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormTextField(
                        value = formState.creditDays,
                        onValueChange = { v -> viewModel.updateFormField { it.copy(creditDays = v) } },
                        label = "Credit Days *",
                        icon = Icons.Default.DateRange,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    FormTextField(
                        value = formState.creditLimit,
                        onValueChange = { v -> viewModel.updateFormField { it.copy(creditLimit = v) } },
                        label = "Credit Limit (₹) *",
                        icon = Icons.Default.ShoppingCart,
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Submit Button ──
            val isFormValid = formState.wholesalerName.isNotBlank() &&
                    formState.contactPerson.isNotBlank() &&
                    formState.phoneNumber.isNotBlank() &&
                    formState.email.isNotBlank() &&
                    formState.gstin.isNotBlank() &&
                    formState.drugLicenseNumber.isNotBlank() &&
                    formState.stateCode.isNotBlank() && formState.stateCode.length == 2 &&
                    formState.addressLine1.isNotBlank() &&
                    formState.city.isNotBlank() &&
                    formState.state.isNotBlank() &&
                    formState.pincode.isNotBlank() && formState.pincode.length == 6 &&
                    formState.creditDays.toIntOrNull() != null &&
                    formState.creditLimit.toDoubleOrNull() != null

            Button(
                onClick = { viewModel.saveWholesaler() },
                enabled = isFormValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryGreen,
                    disabledContainerColor = primaryGreen.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Creating...", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEditMode) "Update Wholesaler" else "Add Wholesaler", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Reusable section card (grouping fields visually)
// ──────────────────────────────────────────────────────────────
@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen,
                    letterSpacing = 0.3.sp
                )
            }
            content()
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Custom styled text field
// ──────────────────────────────────────────────────────────────
@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLength: Int? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (maxLength == null || newValue.length <= maxLength) {
                onValueChange(newValue)
            }
        },
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF8696A0),
                modifier = Modifier.size(18.dp)
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primaryGreen,
            unfocusedBorderColor = Color(0xFFE0E0E0),
            cursorColor = primaryGreen,
            focusedLabelColor = primaryGreen,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}

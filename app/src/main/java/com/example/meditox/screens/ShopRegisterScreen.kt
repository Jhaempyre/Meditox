package com.example.meditox.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.meditox.Routes
import com.example.meditox.models.businessRegistration.BusinessRegistrationRequest
import com.example.meditox.models.viewModel.BusinessRegistrationViewModel
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import com.example.meditox.utils.LocationUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.util.UUID

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ShopRegisterScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: BusinessRegistrationViewModel = viewModel()
) {
    val context = LocalContext.current
    val TAG = "BusinessFormDebug"

    // State variables for form fields
    var shopName by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var gstNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }

    // Observe ViewModel states
    val registrationResult by viewModel.registrationResult.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val locationError by viewModel.locationError.collectAsState()
    val isLocationLoading by viewModel.isLocationLoading.collectAsState()
    val user by viewModel.user.collectAsState()

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = true // Makes icons like battery/time black for visibility
        )
    }

    // Location permissions
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Handle registration result
    LaunchedEffect(registrationResult) {
        when (registrationResult) {
            is ApiResult.Success -> {
                navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.REGISTER_SHOP) { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> { /* Handle other states in UI */ }
        }
    }

    // Custom green color palette
    val primaryGreen = Color(0xFF2E7D32)
    val lightGreen = Color(0xFF4CAF50)
    val lighterGreen = Color(0xFFE8F5E9)
    val darkGreen = Color(0xFF1B5E20)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Text(
            text = "Business Registration",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = primaryGreen,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Business Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = lighterGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Business Information",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = darkGreen
                    )
                )

                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Shop Name *", color = primaryGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = primaryGreen,
                        focusedLabelColor = primaryGreen,
                        focusedTextColor = Color(0xFF2E7D32),
                        unfocusedTextColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = licenseNumber,
                    onValueChange = { licenseNumber = it },
                    label = { Text("Drug License Number", color = primaryGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = primaryGreen,
                        focusedLabelColor = primaryGreen,
                        focusedTextColor = Color(0xFF2E7D32),
                        unfocusedTextColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = gstNumber,
                    onValueChange = { gstNumber = it },
                    label = { Text("GST Number", color = primaryGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = primaryGreen,
                        focusedLabelColor = primaryGreen,
                        focusedTextColor = Color(0xFF2E7D32),
                        unfocusedTextColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Address Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = lighterGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Address Details",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = darkGreen
                    )
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address *", color = primaryGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = primaryGreen,
                        focusedLabelColor = primaryGreen,
                        focusedTextColor = Color(0xFF2E7D32),
                        unfocusedTextColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City *", color = primaryGreen) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = primaryGreen,
                            focusedLabelColor = primaryGreen,
                            focusedTextColor = Color(0xFF2E7D32),
                            unfocusedTextColor = Color(0xFF2E7D32)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State *", color = primaryGreen) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = primaryGreen,
                            focusedLabelColor = primaryGreen,
                            focusedTextColor = Color(0xFF2E7D32),
                            unfocusedTextColor = Color(0xFF2E7D32)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = pinCode,
                    onValueChange = { pinCode = it },
                    label = { Text("Pin Code *", color = primaryGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = primaryGreen,
                        focusedLabelColor = primaryGreen,
                        focusedTextColor = Color(0xFF2E7D32),
                        unfocusedTextColor = Color(0xFF2E7D32)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Contact Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = lighterGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Contact Information",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = darkGreen
                    )
                )

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Contact Phone", color = primaryGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = primaryGreen,
                        focusedLabelColor = primaryGreen,
                        focusedTextColor = Color(0xFF2E7D32),
                        unfocusedTextColor = Color(0xFF2E7D32)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = contactEmail,
                    onValueChange = { contactEmail = it },
                    label = { Text("Contact Email", color = primaryGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = primaryGreen,
                        focusedLabelColor = primaryGreen,
                        focusedTextColor = Color(0xFF2E7D32),
                        unfocusedTextColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Location Section (Moved to after contact details)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = lighterGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Location Information",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = darkGreen
                    )
                )

                if (currentLocation != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryGreen.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Current Location: ${LocationUtils.formatLocation(currentLocation!!)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = darkGreen
                        )
                    }
                }

                if (locationError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = locationError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

                    if (locationError!!.contains("Location services disabled") ||
                        locationError!!.contains("location services")) {
                        OutlinedButton(
                            onClick = {
                                val intent = LocationUtils.createLocationSettingsIntent()
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = primaryGreen
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp,
                                brush = SolidColor(primaryGreen)
                            )
                        ) {
                            Text("Open Location Settings")
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            when {
                                !locationPermissions.allPermissionsGranted -> {
                                    locationPermissions.launchMultiplePermissionRequest()
                                }
                                !LocationUtils.isLocationServicesEnabled(context) -> {
                                    viewModel.resetLocationState()
                                }
                                else -> {
                                    viewModel.fetchCurrentLocation()
                                }
                            }
                        },
                        enabled = !isLocationLoading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryGreen,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLocationLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when {
                                !locationPermissions.allPermissionsGranted -> "Grant Location Permission"
                                !LocationUtils.isLocationServicesEnabled(context) -> "Enable Location Services"
                                else -> "Get Current Location"
                            }
                        )
                    }

                    if (currentLocation != null) {
                        OutlinedButton(
                            onClick = { viewModel.resetLocationState() },
                            modifier = Modifier.weight(0.5f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = primaryGreen
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp,
                                brush = SolidColor(primaryGreen)

                            )
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Registration Button
            Button(
                onClick = {
                    val businessRequest = BusinessRegistrationRequest(
                        user_id = user?.id ?: UUID.randomUUID(),
                        shopName = shopName.takeIf { it.isNotBlank() },
                        licenseNumber = licenseNumber.takeIf { it.isNotBlank() },
                        gstNumber = gstNumber.takeIf { it.isNotBlank() },
                        address = address.takeIf { it.isNotBlank() },
                        city = city.takeIf { it.isNotBlank() },
                        state = state.takeIf { it.isNotBlank() },
                        pinCode = pinCode.takeIf { it.isNotBlank() },
                        contactPhone = contactPhone.takeIf { it.isNotBlank() },
                        contactEmail = contactEmail.takeIf { it.isNotBlank() },
                        latitude = currentLocation?.latitude?.toString(),
                        longitude = currentLocation?.longitude?.toString(),
                        shopStatus = true
                    )

                    viewModel.registerBusinessWithLocation(businessRequest)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = registrationResult !is ApiResult.Loading &&
                        shopName.isNotBlank() &&
                        address.isNotBlank() &&
                        city.isNotBlank() &&
                        state.isNotBlank() &&
                        pinCode.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryGreen,
                    contentColor = Color.White
                )
            ) {
                when (registrationResult) {
                    is ApiResult.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registering...")
                    }
                    else -> {
                        Text("Register Business")
                    }
                }

                LaunchedEffect(registrationResult) {
                   when(val result = registrationResult) {
                       is ApiResult.Success -> {
                           val responseShop = result.data.data
                           if (responseShop != null) {
                               DataStoreManager.saveShopDetails(context, responseShop)
                               DataStoreManager.setIsBusinessRegistered(context, true)
                               Log.d("From_business", "Success: $responseShop")
                           }
                           navController.navigate(Routes.DASHBOARD) {
                               popUpTo(Routes.REGISTER_SHOP) { inclusive = true }
                               launchSingleTop = true
                           }
                       }
                       is ApiResult.Error->{
                           Log.d("From_business", "Error: ${result.message}")
                           Toast.makeText(
                               context,
                               result.message ?: "Something went wrong",
                               Toast.LENGTH_SHORT
                           ).show()
                           viewModel.resetRegistrationState()
                       }
                       else -> {}
                   }

                }
            }

//            // Preview Button
//            OutlinedButton(
//                onClick = {
//                    val previewRequest = BusinessRegistrationRequest(
//                        user_id = user?.id ?: UUID.randomUUID(),
//                        shopName = shopName.takeIf { it.isNotBlank() },
//                        licenseNumber = licenseNumber.takeIf { it.isNotBlank() },
//                        gstNumber = gstNumber.takeIf { it.isNotBlank() },
//                        address = address.takeIf { it.isNotBlank() },
//                        city = city.takeIf { it.isNotBlank() },
//                        state = state.takeIf { it.isNotBlank() },
//                        pinCode = pinCode.takeIf { it.isNotBlank() },
//                        contactPhone = contactPhone.takeIf { it.isNotBlank() },
//                        contactEmail = contactEmail.takeIf { it.isNotBlank() },
//                        latitude = currentLocation?.latitude?.toString(),
//                        longitude = currentLocation?.longitude?.toString(),
//                        shopStatus = true
//                    )
//                    viewModel.registerBusinessWithLocation(previewRequest)
//
//                    Log.d(TAG, "BusinessRequest Preview: $previewRequest")
//                },
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.outlinedButtonColors(
//                    containerColor = Color.Transparent,
//                    contentColor = primaryGreen
//                ),
//                border = ButtonDefaults.outlinedButtonBorder.copy(
//                    width = 1.dp,
//                    brush = SolidColor(primaryGreen)
//                )
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Info,
//                    contentDescription = "Preview",
//                    modifier = Modifier.size(18.dp)
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Preview Business Data")
//            }
        }

        // Error Display
        registrationResult?.let { result ->
            if (result is ApiResult.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = result.message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
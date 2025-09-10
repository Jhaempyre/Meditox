package com.example.meditox.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.meditox.Routes
import com.example.meditox.models.businessRegistration.BusinessRegistrationRequest
import com.example.meditox.models.viewModel.BusinessRegistrationViewModel
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.LocationUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.util.UUID

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ShopRegisterScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: BusinessRegistrationViewModel = viewModel()
) {
    val context = LocalContext.current

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
                // Navigate to dashboard after successful business registration
                navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.REGISTER_SHOP) { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> { /* Handle other states in UI */ }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Business Registration",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Location Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Location Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (currentLocation != null) {
                    Text(
                        text = "Current Location: ${LocationUtils.formatLocation(currentLocation!!)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (locationError != null) {
                    Text(
                        text = locationError!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    // Show location settings button if location services are disabled
                    if (locationError!!.contains("Location services disabled") || 
                        locationError!!.contains("location services")) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val intent = LocationUtils.createLocationSettingsIntent()
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Open Location Settings")
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            when {
                                !locationPermissions.allPermissionsGranted -> {
                                    locationPermissions.launchMultiplePermissionRequest()
                                }
                                !LocationUtils.isLocationServicesEnabled(context) -> {
                                    // Show error message, button to open settings is already shown above
                                    viewModel.resetLocationState()
                                    // This will trigger the error message to show
                                }
                                else -> {
                                    viewModel.fetchCurrentLocation()
                                }
                            }
                        },
                        enabled = !isLocationLoading
                    ) {
                        if (isLocationLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                when {
                                    !locationPermissions.allPermissionsGranted -> "Grant Location Permission"
                                    !LocationUtils.isLocationServicesEnabled(context) -> "Enable Location Services"
                                    else -> "Get Location"
                                }
                            )
                        }
                    }

                    if (currentLocation != null) {
                        OutlinedButton(
                            onClick = { viewModel.resetLocationState() }
                        ) {
                            Text("Clear Location")
                        }
                    }
                }
            }
        }

        // Business Details Form
        OutlinedTextField(
            value = shopName,
            onValueChange = { shopName = it },
            label = { Text("Shop Name *") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = licenseNumber,
            onValueChange = { licenseNumber = it },
            label = { Text("Drug License Number") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = gstNumber,
            onValueChange = { gstNumber = it },
            label = { Text("GST Number") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City *") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state,
                onValueChange = { state = it },
                label = { Text("State *") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = pinCode,
            onValueChange = { pinCode = it },
            label = { Text("Pin Code *") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = contactPhone,
            onValueChange = { contactPhone = it },
            label = { Text("Contact Phone") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = contactEmail,
            onValueChange = { contactEmail = it },
            label = { Text("Contact Email") },
            modifier = Modifier.fillMaxWidth()
        )

        // Registration Button
        Button(
            onClick = {
                val businessRequest = BusinessRegistrationRequest(
                    user_id = user?.id ?: UUID.randomUUID(), // This will be set by ViewModel
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

                // Use the new method that automatically fetches location if needed
                viewModel.registerBusinessWithLocation(businessRequest)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = registrationResult !is ApiResult.Loading &&
                    shopName.isNotBlank() &&
                    address.isNotBlank() &&
                    city.isNotBlank() &&
                    state.isNotBlank() &&
                    pinCode.isNotBlank()
        ) {
            when (registrationResult) {
                is ApiResult.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registering...")
                }
                else -> {
                    Text("Register Business")
                }
            }
        }

        // Error Display
        registrationResult?.let { result ->
            if (result is ApiResult.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = result.message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
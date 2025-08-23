package com.example.meditox.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.meditox.Routes
import com.example.meditox.models.EmergencyContact
import com.example.meditox.models.userRegistration.UserRegistrationRequest
import com.example.meditox.models.viewModel.RegistrationViewModel
import com.example.meditox.utils.ApiResult
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterUserScreen(modifier: Modifier, navController: NavController) {
    // Green color palette
    val greenPrimary = Color(0xFF2E7D32)
    val greenLight = Color(0xFF4CAF50)
    val greenDark = Color(0xFF1B5E20)
    val greenGradient = Brush.horizontalGradient(listOf(greenLight, greenPrimary))
    val backgroundGrey = Color(0xFFF8F9FA)

    val viewModel: RegistrationViewModel = viewModel()
    val registrationResult by viewModel.registrationResult.collectAsState()
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var chronicConditions by remember { mutableStateOf("") }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContactPhone by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }


    val genderOptions = listOf("Male", "Female", "Other")
    val bloodGroupOptions = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val roleOptions = listOf("Chemist")

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    //only way
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = true // Makes icons like battery/time black for visibility
        )
    }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year: Int, month: Int, dayOfMonth: Int ->
                // month is 0-based, so we add 1
                dob = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(backgroundGrey)
            .padding(16.dp)
    ) {
        // Header with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 9.dp)
                .height(120.dp)
                .background(
                    brush = greenGradient,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    "Create Account",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Join our Chemists Community",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Form in a card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Personal Information Section
                Text(
                    "Personal Information",
                    style = MaterialTheme.typography.titleLarge,
                    color = greenDark,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                CustomOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date of Birth field - Fixed with proper click handling
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dob,
                        onValueChange = {},
                        readOnly = true,

                        label = { Text("Date of Birth") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Date of Birth",
                                tint = greenPrimary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = greenPrimary,
                            unfocusedBorderColor = Color(0xFF4CAF50),
                            focusedTextColor = Color(0xFF4CAF50),
                            unfocusedTextColor = Color(0xFF4CAF50)

                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // This invisible box handles the click event
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { datePickerDialog.show() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Gender dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        CustomDropdownMenu(
                            label = "Gender",
                            options = genderOptions,
                            selectedOption = gender,
                            onOptionSelected = { gender = it }
                        )
                    }

                    // Blood Group dropdown
                    Box(modifier = Modifier.weight(1f)
                        ) {
                        CustomDropdownMenu(
                            label = "Blood Group",
                            options = bloodGroupOptions,
                            selectedOption = bloodGroup,
                            onOptionSelected = { bloodGroup = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Health Information Section
                Text(
                    "Health Information",
                    style = MaterialTheme.typography.titleLarge,
                    color = greenDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CustomOutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = "Allergies (comma separated)",
                    singleLine = false,
                    modifier = Modifier.height(80.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomOutlinedTextField(
                    value = chronicConditions,
                    onValueChange = { chronicConditions = it },
                    label = "Chronic Conditions (comma separated)",
                    singleLine = false,
                    modifier = Modifier.height(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Emergency Contact Section
                Text(
                    "Emergency Contact",
                    style = MaterialTheme.typography.titleLarge,
                    color = greenDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CustomOutlinedTextField(
                    value = emergencyContactName,
                    onValueChange = { emergencyContactName = it },
                    label = "Contact Name"
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomOutlinedTextField(
                    value = emergencyContactPhone,
                    onValueChange = { emergencyContactPhone = it },
                    label = "Contact Phone",
                    keyboardType = KeyboardType.Phone
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomOutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = "Relation"

                )

                Spacer(modifier = Modifier.height(16.dp))

                // Role Selection
                Text(
                    "Account Type",
                    style = MaterialTheme.typography.titleLarge,
                    color = greenDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CustomDropdownMenu(
                    label = "Select Role",
                    options = roleOptions,
                    selectedOption = role,
                    onOptionSelected = { role = "0301" }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "By registering, you agree to our Terms and Privacy Policy",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
            when(registrationResult) {
                is ApiResult.Loading -> {
                    CircularProgressIndicator(color = greenDark)
                }
                else -> {

                    // Register Button
                    Button(
                        onClick = {

                            val localDate = org.threeten.bp.LocalDate.parse(dob)
                            val dobDateTime = localDate.atStartOfDay()
                            viewModel.registerUser(
                                UserRegistrationRequest(
                                    abhaId="",
                                    phone = "1234",
                                    name = name,
                                    dob = dobDateTime,
                                    gender = gender,
                                    bloodGroup = bloodGroup,
                                    allergies = allergies,
                                    chronicConditions = chronicConditions,
                                    emergencyContact = EmergencyContact(
                                        name = emergencyContactName,
                                        phone = emergencyContactPhone,
                                        relationship = relationship
                                    ),
                                    role = role

                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = greenPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Create Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
                LaunchedEffect(registrationResult) {
                    when(val result = registrationResult) {
                        is ApiResult.Success -> {
                            // Handle success
                            Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo("register") { inclusive = true }
                            }
                            viewModel.resetRegistrationState()
                        }

                        is ApiResult.Error -> {
                            // Handle error
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = label,
                    tint = Color(0xFF2E7D32)
                )
            }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        // Use the newer OutlinedTextFieldDefaults instead
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2E7D32),
            unfocusedBorderColor = Color.LightGray,
            focusedTextColor = Color(0xFF2E7D32),
            unfocusedTextColor = Color(0xFF2E7D32)
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = singleLine,
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownMenu(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = Color(0xFF2E7D32)
                )
            },
            // Use the newer OutlinedTextFieldDefaults instead
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2E7D32),
                unfocusedBorderColor = Color.LightGray,
                focusedTextColor = Color(0xFF2E7D32),
                unfocusedTextColor = Color(0xFF2E7D32)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = {
                        Text(
                            selectionOption,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4CAF50)
                        )
                    },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    }
//                    colors = MenuItemDefaults.itemColors(
//                        textColor = Color.DarkGray)

                )
            }
        }
    }
}
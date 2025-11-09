package com.example.meditox.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.meditox.models.viewModel.EditUserProfileViewModel
import com.example.meditox.ui.theme.*
import com.example.meditox.utils.ApiResult
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: EditUserProfileViewModel
) {
    val context = LocalContext.current
    val userData = viewModel.userData.collectAsState()
    val updateResult = viewModel.updateResult.collectAsState()
    var isLoading by remember { mutableStateOf(false) }

    // Form states
    var name by remember { mutableStateOf("") }
    var abhaId by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var chronicConditions by remember { mutableStateOf("") }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContactPhone by remember { mutableStateOf("") }
    var emergencyContactRelationship by remember { mutableStateOf("") }
    var selectedBloodGroup by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }
    var showBloodGroupDropdown by remember { mutableStateOf(false) }
    var showGenderDropdown by remember { mutableStateOf(false) }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val genders = listOf("Male", "Female", "Other")

    // Colors
    val primaryGreen = Color(0xFF2E7D32)
    val lightGreen = Color(0xFF4CAF50)
    val lighterGreen = Color(0xFFE8F5E9)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            lighterGreen,
            Color.White,
            lighterGreen.copy(alpha = 0.3f)
        )
    )

    // Initialize form data when user data is loaded
    LaunchedEffect(userData.value) {
        userData.value?.let { user ->
            name = user.name
            abhaId = user.abhaId
            allergies = user.allergies
            chronicConditions = user.chronicConditions
            emergencyContactName = user.emergencyContact.name
            emergencyContactPhone = user.emergencyContact.phone
            emergencyContactRelationship = user.emergencyContact.relationship
            selectedBloodGroup = user.bloodGroup
            selectedGender = user.gender
        }
    }

    // Handle update result
    LaunchedEffect(updateResult.value) {
        when (updateResult.value) {
            is ApiResult.Loading -> isLoading = true
            is ApiResult.Success -> {
                isLoading = false
                // Show success animation and navigate back
                delay(1000)
                navController.popBackStack()
            }
            is ApiResult.Error -> {
                isLoading = false
            }
            null -> isLoading = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header with profile avatar
                ProfileHeader(
                    name = name.ifBlank { "User" },
                    onBackClick = { navController.popBackStack() }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Personal Information Section
            item {
                SectionCard(
                    title = "Personal Information",
                    icon = Icons.Default.Person
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StyledTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Full Name",
                            icon = Icons.Default.Person
                        )

                        StyledTextField(
                            value = abhaId,
                            onValueChange = { abhaId = it },
                            label = "ABHA ID",
                            icon = Icons.Default.Create,
                            keyboardType = KeyboardType.Number
                        )

                        // Gender Dropdown
                        StyledDropdown(
                            value = selectedGender,
                            onValueChange = { selectedGender = it },
                            label = "Gender",
                            icon = Icons.Default.Person,
                            options = genders,
                            expanded = showGenderDropdown,
                            onExpandedChange = { showGenderDropdown = it }
                        )

                        // Blood Group Dropdown
                        StyledDropdown(
                            value = selectedBloodGroup,
                            onValueChange = { selectedBloodGroup = it },
                            label = "Blood Group",
                            icon = Icons.Default.KeyboardArrowDown,
                            options = bloodGroups,
                            expanded = showBloodGroupDropdown,
                            onExpandedChange = { showBloodGroupDropdown = it }
                        )
                    }
                }
            }

            // Medical Information Section
            item {
                SectionCard(
                    title = "Medical Information",
                    icon = Icons.Default.AccountBox
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StyledTextField(
                            value = allergies,
                            onValueChange = { allergies = it },
                            label = "Allergies",
                            icon = Icons.Default.Warning,
                            minLines = 2,
                            placeholder = "List any known allergies..."
                        )

                        StyledTextField(
                            value = chronicConditions,
                            onValueChange = { chronicConditions = it },
                            label = "Chronic Conditions",
                            icon = Icons.Default.AddCircle,
                            minLines = 2,
                            placeholder = "List any chronic conditions..."
                        )
                    }
                }
            }

            // Emergency Contact Section
            item {
                SectionCard(
                    title = "Emergency Contact",
                    icon = Icons.Default.Star
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StyledTextField(
                            value = emergencyContactName,
                            onValueChange = { emergencyContactName = it },
                            label = "Contact Name",
                            icon = Icons.Default.Build
                        )

                        StyledTextField(
                            value = emergencyContactPhone,
                            onValueChange = { emergencyContactPhone = it },
                            label = "Contact Phone",
                            icon = Icons.Default.Phone,
                            keyboardType = KeyboardType.Phone
                        )

                        StyledTextField(
                            value = emergencyContactRelationship,
                            onValueChange = { emergencyContactRelationship = it },
                            label = "Relationship",
                            icon = Icons.Default.Person,
                            keyboardType = KeyboardType.Text
                        )
                    }
                }
            }

            // Save Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                SaveButton(
                    onClick = {
                        viewModel.updateUserProfile(
                            name = name,
                            abhaId = abhaId,
                            gender = selectedGender,
                            bloodGroup = selectedBloodGroup,
                            allergies = allergies,
                            chronicConditions = chronicConditions,
                            emergencyContactName = emergencyContactName,
                            emergencyContactPhone = emergencyContactPhone,
                            emergencyContactRelationship = ""
                        )
                    },
                    isLoading = isLoading,
                    enabled = name.isNotBlank() && 
                             selectedGender.isNotBlank() && 
                             emergencyContactName.isNotBlank() &&
                             emergencyContactPhone.isNotBlank()
                )
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Error Snackbar
        updateResult.value?.let { result ->
            if (result is ApiResult.Error) {
                LaunchedEffect(result) {
                    // Show snackbar error
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    onBackClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(
                        Color(0xFFE8F5E9),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4CAF50),
                                Color(0xFF2E7D32)
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "U",
                    color = Color.Blue,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Edit Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = name.ifBlank { "Update your information" },
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32)
                )
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    placeholder: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF2E7D32)
            )
        },
        placeholder = placeholder?.let { { Text(it, color = Color.Gray) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2E7D32),
            focusedLabelColor = Color(0xFF2E7D32),
            cursorColor = Color(0xFF2E7D32),
            unfocusedTextColor =Color(0xFF2E7D25)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StyledDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32)
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2E7D32),
                focusedLabelColor = Color(0xFF2E7D32),
                unfocusedTextColor =Color(0xFF2E7D25)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun SaveButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean
) {
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (enabled) Color(0xFF2E7D32) else Color.Gray,
        animationSpec = tween(300)
    )

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedBackgroundColor,
            disabledContainerColor = Color.Gray
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}
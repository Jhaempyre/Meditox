package com.example.meditox.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.meditox.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(modifier: Modifier = Modifier, navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDropdownMenu by remember { mutableStateOf(false) }

    val tabs = listOf("Sell Medicine", "Reports", "Add/Release")
    val tabIcons = listOf(
        Icons.Default.Favorite,
        Icons.Default.AccountBox,
        Icons.Default.Add
    )

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = tabs[selectedTab],
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            actions = {
                Box {
                    // Avatar with dropdown
                    IconButton(
                        onClick = { showDropdownMenu = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Dropdown Menu
                    DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = Color(0xFF128C7E)
                                    )
                                    Text(
                                        text = "Edit Profile",
                                        color = Color.Black,
                                        fontSize = 16.sp
                                    )
                                }
                            },
                            onClick = {
                                showDropdownMenu = false
                                // Handle edit profile
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircle,
                                        contentDescription = "Edit Shop",
                                        tint = Color(0xFF128C7E)
                                    )
                                    Text(
                                        text = "Edit Shop Details",
                                        color = Color.Black,
                                        fontSize = 16.sp
                                    )
                                }
                            },
                            onClick = {
                                showDropdownMenu = false
                                // Handle edit shop details
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Logout",
                                        tint = Color.Red
                                    )
                                    Text(
                                        text = "Logout",
                                        color = Color.Red,
                                        fontSize = 16.sp
                                    )
                                }
                            },
                            onClick = {
                                showDropdownMenu = false
                                // Handle logout
                            }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF128C7E) // WhatsApp green
            )
        )

        // Main Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
        ) {
            when (selectedTab) {
                0 -> SellMedicineScreen()
                1 -> ReportsScreen()
                2 -> AddReleaseScreen()
            }
        }

        // Bottom Navigation
        NavigationBar(
            containerColor = Color.White,
            modifier = Modifier.shadow(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = tabIcons[index],
                            contentDescription = title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF128C7E),
                        selectedTextColor = Color(0xFF128C7E),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF128C7E).copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}

@Composable
fun SellMedicineScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Medicine Inventory",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF128C7E),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(10) { index ->
            MedicineCard(
                medicineName = "Medicine ${index + 1}",
                stock = (50..200).random(),
                price = (10..100).random()
            )
        }
    }
}

@Composable
fun ReportsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Sales Reports",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF128C7E),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            ReportCard(
                title = "Today's Sales",
                value = "₹12,450",
                icon = Icons.Default.AddCircle
            )
        }

        item {
            ReportCard(
                title = "This Month",
                value = "₹3,45,600",
                icon = Icons.Default.Person
            )
        }

        item {
            ReportCard(
                title = "Total Orders",
                value = "1,234",
                icon = Icons.Default.Build
            )
        }
    }
}

@Composable
fun AddReleaseScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Add/Release Medicine",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF128C7E),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            ActionCard(
                title = "Add New Medicine",
                description = "Add new medicine to inventory",
                icon = Icons.Default.Add,
                onClick = { /* Handle add medicine */ }
            )
        }

        item {
            ActionCard(
                title = "Release Medicine",
                description = "Release medicine from stock",
                icon = Icons.Default.Create,
                onClick = { /* Handle release medicine */ }
            )
        }

        item {
            ActionCard(
                title = "Update Stock",
                description = "Update existing medicine stock",
                icon = Icons.Default.Edit,
                onClick = { /* Handle update stock */ }
            )
        }
    }
}

@Composable
fun MedicineCard(
    medicineName: String,
    stock: Int,
    price: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle medicine click */ },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = medicineName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Stock: $stock units",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "₹$price per unit",
                    fontSize = 16.sp,
                    color = Color(0xFF128C7E),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Medicine",
                tint = Color(0xFF128C7E),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun ReportCard(
    title: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF128C7E)
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF128C7E),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFF128C7E).copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF128C7E),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
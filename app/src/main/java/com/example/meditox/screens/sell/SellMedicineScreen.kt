package com.example.meditox.screens.sell

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.meditox.ui.theme.primaryGreen

@Composable
fun SellMedicineScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Medicine Inventory",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { navController.navigate("add_medicine") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Medicine",
                        tint = primaryGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        item {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search medicines...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryGreen,
                    focusedLabelColor = primaryGreen
                )
            )
        }

        // Quick Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionChip(
                    text = "All Medicines",
                    onClick = { /* Filter all */ },
                    modifier = Modifier.weight(1f)
                )
                QuickActionChip(
                    text = "Low Stock",
                    onClick = { navController.navigate("low_stock") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionChip(
                    text = "Categories",
                    onClick = { navController.navigate("categories") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        items(15) { index ->
            MedicineCard(
                medicineName = "Medicine ${index + 1}",
                stock = (50..200).random(),
                price = (10..100).random(),
                onClick = { navController.navigate("medicine_detail/$index") }
            )
        }
    }
}

@Composable
fun QuickActionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text, fontSize = 12.sp) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = primaryGreen.copy(alpha = 0.1f),
            labelColor = primaryGreen
        ),
        modifier = modifier
    )
}

@Composable
fun MedicineCard(
    medicineName: String,
    stock: Int,
    price: Int,
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
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicineName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Stock: $stock units",
                    fontSize = 14.sp,
                    color = if (stock < 50) Color.Red else Color.Gray
                )
                Text(
                    text = "₹$price per unit",
                    fontSize = 16.sp,
                    color = primaryGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Medicine",
                tint = primaryGreen,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
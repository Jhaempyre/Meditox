package com.example.meditox.screens.addrelease

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit

import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import com.example.meditox.Routes
import com.example.meditox.ui.theme.primaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReleaseScreen(navController: NavController) {
    var showAddMedicineSheet by remember { mutableStateOf(false) }
    val addMedicineSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inventory Management",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen
                )

                IconButton(
                    onClick = { navController.navigate("inventory_history") }
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBox,
                        contentDescription = "History",
                        tint = primaryGreen
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    text = "Quick Add",
                    icon = Icons.Default.Add,
                    onClick = { navController.navigate("quick_add") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    text = "Bulk Update",
                    icon = Icons.Default.Refresh,
                    onClick = { navController.navigate("bulk_update") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            ActionCard(
                title = "Add New Medicine",
                description = "Add new medicine to inventory with details",
                icon = Icons.Default.Add,
                onClick = { showAddMedicineSheet = true }
            )
        }

        item {
            ActionCard(
                title = "Release Medicine",
                description = "Release medicine from stock for orders",
                icon = Icons.Default.Create,
                onClick = { navController.navigate("release_medicine") }
            )
        }

        item {
            ActionCard(
                title = "Update Stock",
                description = "Update existing medicine stock quantities",
                icon = Icons.Default.Edit,
                onClick = { navController.navigate("update_stock") }
            )
        }

        item {
            ActionCard(
                title = "Batch Management",
                description = "Manage medicine batches and expiry dates",
                icon = Icons.Default.AccountBox,
                onClick = { navController.navigate("batch_management") }
            )
        }

        // Recent Activity Section
        item {
            Text(
                text = "Recent Activity",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(5) { index ->
            RecentActivityItem(
                action = if (index % 2 == 0) "Added" else "Updated",
                medicine = "Medicine ${index + 1}",
                quantity = "+${(10..50).random()}",
                time = "${(1..12).random()}h ago"
            )
        }
    }

    if (showAddMedicineSheet) {
        AddInventoryBottomSheet(
            onDismiss = { showAddMedicineSheet = false },
            sheetState = addMedicineSheetState,
            navController = navController
        )
    }
}

data class InventoryCategory(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBackground: Color,
    val iconTint: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInventoryBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    navController: NavController
) {
    val categories = listOf(
        InventoryCategory("Add Drugs", "Prescription medicines", Icons.Default.Check, Color(0xFFE8F5E9), Color(0xFF4CAF50)),
        InventoryCategory("Cosmetic", "Skincare & beauty", Icons.Default.Face, Color(0xFFFFF3E0), Color(0xFFFB8C00)),
        InventoryCategory("FMCG", "Consumer goods", Icons.Default.ShoppingCart, Color(0xFFE3F2FD), Color(0xFF2196F3)),
        InventoryCategory("Device", "Medical equipment", Icons.Default.AddCircle, Color(0xFFF3E5F5), Color(0xFF9C27B0)),
        InventoryCategory("Supplement", "Vitamins & health", Icons.Default.Person, Color(0xFFFFFDE7), Color(0xFFFBC02D)),
        InventoryCategory("Surgical", "Surgical instruments", Icons.Default.Build , Color(0xFFFFEBEE), Color(0xFFE91E63))
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Add to Inventory",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select a category to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(Color(0xFFF5F5F5), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(categories) { category ->
                    CategoryCard(
                        category = category,
                        onClick = {
                            if (category.title == "Add Drugs") {
                                onDismiss()
                                navController.navigate(Routes.ADD_DRUG_SCREEN)
                            }
                            // TODO: Add navigation for other categories
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Buttons
            }
    }
}

@Composable
fun CategoryCard(
    category: InventoryCategory,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF9F9F9),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(category.iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = category.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = category.subtitle,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
    @Composable
    fun QuickActionButton(
        text: String,
        icon: ImageVector,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryGreen.copy(alpha = 0.1f),
                contentColor = primaryGreen
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
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
                            color = primaryGreen.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = primaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
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

    @Composable
    fun RecentActivityItem(
        action: String,
        medicine: String,
        quantity: String,
        time: String
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$action $medicine",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = time,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = quantity,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (action == "Added") Color.Green else primaryGreen
                )
            }
        }
    }


package com.example.meditox.screens.wholesaler

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.meditox.database.entity.WholesalerEntity
import com.example.meditox.models.viewModel.WholesalerViewModel
import com.example.meditox.ui.theme.darkGreen
import com.example.meditox.ui.theme.lighterGreen
import com.example.meditox.ui.theme.primaryGreen
import kotlin.math.absoluteValue

// Predefined avatar colors — vibrant and distinct
private val avatarColors = listOf(
    Color(0xFF1B5E20), // Dark Green
    Color(0xFF0D47A1), // Dark Blue
    Color(0xFF4A148C), // Deep Purple
    Color(0xFFBF360C), // Deep Orange
    Color(0xFF006064), // Dark Teal
    Color(0xFF880E4F), // Dark Pink
    Color(0xFF33691E), // Lime Dark
    Color(0xFF4E342E), // Brown
    Color(0xFF1A237E), // Indigo
    Color(0xFFE65100), // Orange
)

private fun getInitials(name: String): String {
    return name
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { "W" }
}

private fun getAvatarColor(name: String): Color {
    return avatarColors[name.hashCode().absoluteValue % avatarColors.size]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesalerListScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: WholesalerViewModel = viewModel(
        factory = WholesalerViewModel.provideFactory(context)
    )

    val wholesalers by viewModel.wholesalers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()

    // Bottom sheet state
    var selectedWholesaler by remember { mutableStateOf<WholesalerEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    Column {
                        // Main top bar row
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

                            AnimatedVisibility(
                                visible = !isSearchActive,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp)
                                ) {
                                    Text(
                                        text = "Wholesalers",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${wholesalers.size} contacts",
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = isSearchActive,
                                enter = expandHorizontally() + fadeIn(),
                                exit = shrinkHorizontally() + fadeOut()
                            ) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                                    placeholder = {
                                        Text(
                                            "Search wholesalers...",
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = Color.White,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.White.copy(alpha = 0.4f),
                                        unfocusedIndicatorColor = Color.Transparent
                                    )
                                )
                            }

                            if (!isSearchActive) {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            IconButton(onClick = { viewModel.toggleSearch() }) {
                                Icon(
                                    imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = if (isSearchActive) "Close search" else "Search",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (wholesalers.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(lighterGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🏪",
                            fontSize = 40.sp
                        )
                    }
                    Text(
                        text = if (searchQuery.isNotBlank()) "No matches found" else "No wholesalers yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF37474F)
                    )
                    Text(
                        text = if (searchQuery.isNotBlank())
                            "Try a different search term"
                        else
                            "Wholesalers will appear here once synced",
                        fontSize = 14.sp,
                        color = Color(0xFF78909C)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(
                    items = wholesalers,
                    key = { it.wholesalerId }
                ) { wholesaler ->
                    WholesalerListItem(
                        wholesaler = wholesaler,
                        onClick = { selectedWholesaler = wholesaler },
                        onPhoneClick = { phoneNumber ->
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$phoneNumber")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    // Wholesaler Detail Bottom Sheet
    selectedWholesaler?.let { wholesaler ->
        WholesalerDetailBottomSheet(
            wholesaler = wholesaler,
            sheetState = sheetState,
            onDismiss = { selectedWholesaler = null },
            onPhoneClick = { phoneNumber ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                context.startActivity(intent)
            }
        )
    }
}


@Composable
private fun WholesalerListItem(
    wholesaler: WholesalerEntity,
    onClick: () -> Unit,
    onPhoneClick: (String) -> Unit
) {
    val initials = getInitials(wholesaler.wholesalerName)
    val avatarColor = getAvatarColor(wholesaler.wholesalerName)

    // Build subtitle: contact person + city
    val subtitleParts = mutableListOf<String>()
    wholesaler.contactPerson?.takeIf { it.isNotBlank() }?.let { subtitleParts.add(it) }
    wholesaler.city?.takeIf { it.isNotBlank() }?.let { subtitleParts.add(it) }
    val subtitle = subtitleParts.joinToString(" · ").ifEmpty { "No details available" }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with initials
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Name + subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = wholesaler.wholesalerName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1B1B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF8696A0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Phone icon — tappable to open dialer
            wholesaler.phoneNumber?.takeIf { it.isNotBlank() }?.let { phone ->
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { onPhoneClick(phone) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call $phone",
                        tint = primaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // WhatsApp-style thin divider — indented to start after avatar
        HorizontalDivider(
            modifier = Modifier.padding(start = 82.dp),
            thickness = 0.5.dp,
            color = Color(0xFFE8E8E8)
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Wholesaler Detail Bottom Sheet
// ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WholesalerDetailBottomSheet(
    wholesaler: WholesalerEntity,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onPhoneClick: (String) -> Unit
) {
    val initials = getInitials(wholesaler.wholesalerName)
    val avatarColor = getAvatarColor(wholesaler.wholesalerName)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // ── Header with avatar + name ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryGreen.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = wholesaler.wholesalerName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1B)
                )

                // Status badge
                wholesaler.isActive?.let { active ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (active) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = if (active) "● Active" else "● Inactive",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (active) primaryGreen else Color(0xFFE53935),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                // Quick action buttons under header
                wholesaler.phoneNumber?.takeIf { it.isNotBlank() }?.let { phone ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onPhoneClick(phone) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryGreen
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Call Now",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color(0xFFE8E8E8),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Details Section ──
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Contact Details
                SectionHeader(title = "Contact Details")

                DetailRow(
                    icon = Icons.Default.Person,
                    label = "Contact Person",
                    value = wholesaler.contactPerson
                )
                DetailRow(
                    icon = Icons.Default.Phone,
                    label = "Phone Number",
                    value = wholesaler.phoneNumber
                )
                DetailRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = wholesaler.email
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Business Details
                SectionHeader(title = "Business Details")

                DetailRow(
                    icon = Icons.Default.Info,
                    label = "GSTIN",
                    value = wholesaler.gstin
                )
                DetailRow(
                    icon = Icons.Default.Create,
                    label = "Drug License",
                    value = wholesaler.drugLicenseNumber
                )
                DetailRow(
                    icon = Icons.Default.Star,
                    label = "State Code",
                    value = wholesaler.stateCode
                )
                DetailRow(
                    icon = Icons.Default.DateRange,
                    label = "Credit Days",
                    value = wholesaler.creditDays?.toString()
                )
                DetailRow(
                    icon = Icons.Default.ShoppingCart,
                    label = "Credit Limit",
                    value = wholesaler.creditLimit?.let { "₹%.2f".format(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Address
                SectionHeader(title = "Address")

                val addressParts = listOfNotNull(
                    wholesaler.addressLine1,
                    wholesaler.addressLine2,
                    wholesaler.city,
                    wholesaler.state,
                    wholesaler.pincode
                ).filter { it.isNotBlank() }

                if (addressParts.isNotEmpty()) {
                    DetailRow(
                        icon = Icons.Default.LocationOn,
                        label = "Full Address",
                        value = addressParts.joinToString(", ")
                    )
                } else {
                    DetailRow(
                        icon = Icons.Default.LocationOn,
                        label = "Full Address",
                        value = null
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = primaryGreen,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF8696A0),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF8696A0)
            )
            Text(
                text = value?.takeIf { it.isNotBlank() } ?: "—",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (value.isNullOrBlank()) Color(0xFFBDBDBD) else Color(0xFF1B1B1B)
            )
        }
    }
}

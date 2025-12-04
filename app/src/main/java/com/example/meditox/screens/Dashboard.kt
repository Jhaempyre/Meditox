package com.example.meditox.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.meditox.Routes
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meditox.models.viewModel.AuthViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import kotlinx.coroutines.launch
import com.example.meditox.screens.sell.SellMedicineScreen
import com.example.meditox.screens.report.ReportsScreen
import com.example.meditox.screens.addrelease.AddReleaseScreen
import com.example.meditox.ui.theme.primaryGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Dashboard(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    var showDropdownMenu by remember { mutableStateOf(false) }
    
    // Observe subscription status and backend sync status
    val hasActiveSubscription by DataStoreManager.hasActiveSubscription(context).collectAsState(initial = false)
    val backendSyncStatus by DataStoreManager.getBackendSyncStatus(context).collectAsState(initial = false)
    
    // Observe logout result
    val logoutResult by authViewModel.logoutResult.collectAsState()
    
    // Handle logout result
    LaunchedEffect(logoutResult) {
        when (logoutResult) {
            is ApiResult.Success -> {
                Log.d("Logoutse","yes i am here")
                authViewModel.resetLogoutState()
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is ApiResult.Error -> {
                // Even on error, we cleared local data, so navigate to login
                Log.d("Logoutse","mae yaha bhi hu laadle")
                authViewModel.resetLogoutState()
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> { /* Loading or null, do nothing */ }
        }
    }

    val tabs = listOf("Sell Medicine", "Reports", "Add/Release")
    val tabIcons = listOf(
        Icons.Default.Favorite,
        Icons.Default.AccountBox,
        Icons.Default.Add
    )

    // Pager state for swipe navigation
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = tabs[pagerState.currentPage],
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
                                        tint = primaryGreen
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
                                navController.navigate(Routes.EDIT_PROFILE)
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
                                        tint = primaryGreen
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
                                navController.navigate("edit_shop")
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (hasActiveSubscription && backendSyncStatus) Icons.Default.CheckCircle else Icons.Default.CheckCircle,
                                        contentDescription = if (hasActiveSubscription && backendSyncStatus) "Manage subscription" else "Subscribe",
                                        tint = if (hasActiveSubscription && backendSyncStatus) primaryGreen else primaryGreen
                                    )
                                    Text(
                                        text = if (hasActiveSubscription && backendSyncStatus) "Manage Subscription" else "Subscribe",
                                        color = Color.Black,
                                        fontSize = 16.sp
                                    )
                                }
                            },
                            onClick = {
                                showDropdownMenu = false
                                if (hasActiveSubscription && backendSyncStatus) {
                                    navController.navigate(Routes.SUCCEEDED_SUBSCRIPTION)
                                } else {
                                    navController.navigate(Routes.SUBSCRIPTION)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (logoutResult is ApiResult.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.Red
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = "Logout",
                                            tint = Color.Red
                                        )
                                    }
                                    Text(
                                        text = if (logoutResult is ApiResult.Loading) "Logging out..." else "Logout",
                                        color = Color.Red,
                                        fontSize = 16.sp
                                    )
                                }
                            },
                            onClick = {
                                if (logoutResult !is ApiResult.Loading) {
                                    showDropdownMenu = false
                                    authViewModel.logout()
                                }
                            }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = primaryGreen
            )
        )

        // Swipeable Content Area
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
            ) {
                when (page) {
                    0 -> SellMedicineScreen(navController = navController)
                    1 -> ReportsScreen(navController = navController)
                    2 -> AddReleaseScreen(navController = navController)
                }
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
                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryGreen,
                        selectedTextColor = primaryGreen,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = primaryGreen.copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}
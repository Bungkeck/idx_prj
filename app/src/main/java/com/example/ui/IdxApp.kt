package com.example.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ApiExplorerScreen
import com.example.ui.screens.AuthSandboxScreen
import com.example.ui.screens.DocsScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateDarkBg
import com.example.ui.viewmodel.IdxViewModel

enum class NavigationTab(val title: String) {
    MARKET("Pasar BEI"),
    EXPLORER("API Explorer"),
    DOCS("Dokumentasi"),
    AUTH("Auth & Sandbox")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdxApp(viewModel: IdxViewModel) {
    var currentTab by remember { mutableStateOf(NavigationTab.MARKET) }

    Scaffold(
        containerColor = SlateDarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "IDX API Wrapper",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    Text(
                        text = "BEI v1.4",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateDarkBg)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SlateCardBg,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == NavigationTab.MARKET,
                    onClick = { currentTab = NavigationTab.MARKET },
                    icon = { Icon(imageVector = Icons.Default.ShowChart, contentDescription = "Market") },
                    label = { Text(NavigationTab.MARKET.title, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = SlateDarkBg
                    )
                )

                NavigationBarItem(
                    selected = currentTab == NavigationTab.EXPLORER,
                    onClick = { currentTab = NavigationTab.EXPLORER },
                    icon = { Icon(imageVector = Icons.Default.Api, contentDescription = "Explorer") },
                    label = { Text(NavigationTab.EXPLORER.title, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = SlateDarkBg
                    )
                )

                NavigationBarItem(
                    selected = currentTab == NavigationTab.DOCS,
                    onClick = { currentTab = NavigationTab.DOCS },
                    icon = { Icon(imageVector = Icons.Default.Description, contentDescription = "Docs") },
                    label = { Text(NavigationTab.DOCS.title, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = SlateDarkBg
                    )
                )

                NavigationBarItem(
                    selected = currentTab == NavigationTab.AUTH,
                    onClick = { currentTab = NavigationTab.AUTH },
                    icon = { Icon(imageVector = Icons.Default.Security, contentDescription = "Auth") },
                    label = { Text(NavigationTab.AUTH.title, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = SlateDarkBg
                    )
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = SlateDarkBg
        ) {
            when (currentTab) {
                NavigationTab.MARKET -> MarketScreen(viewModel = viewModel)
                NavigationTab.EXPLORER -> ApiExplorerScreen(viewModel = viewModel)
                NavigationTab.DOCS -> DocsScreen(
                    viewModel = viewModel,
                    onNavigateToExplorer = { endpoint ->
                        viewModel.selectEndpoint(endpoint)
                        currentTab = NavigationTab.EXPLORER
                    }
                )
                NavigationTab.AUTH -> AuthSandboxScreen(viewModel = viewModel)
            }
        }
    }
}

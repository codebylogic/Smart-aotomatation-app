package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.MainViewModel
import com.example.ui.components.NavTab
import com.example.ui.components.NexusBottomNavBar
import com.example.ui.screens.CodeEditorScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.SystemLogsScreen
import com.example.ui.screens.VisualCanvasScreen
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(NavTab.CANVAS) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CyberBackground,
        bottomBar = {
            NexusBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    if (selectedTab == NavTab.CANVAS && tab == NavTab.EDITOR) {
                        viewModel.syncVisualToCode()
                    } else if (selectedTab == NavTab.EDITOR && tab == NavTab.CANVAS) {
                        viewModel.syncCodeToVisual()
                    }
                    selectedTab = tab
                }
            )
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)
        when (selectedTab) {
            NavTab.CANVAS -> {
                VisualCanvasScreen(
                    viewModel = viewModel,
                    onNavigateToCode = {
                        viewModel.syncVisualToCode()
                        selectedTab = NavTab.EDITOR
                    },
                    modifier = screenModifier
                )
            }
            NavTab.EDITOR -> {
                CodeEditorScreen(
                    viewModel = viewModel,
                    onNavigateToCanvas = {
                        viewModel.syncCodeToVisual()
                        selectedTab = NavTab.CANVAS
                    },
                    modifier = screenModifier
                )
            }
            NavTab.PERMISSIONS -> {
                PermissionsScreen(
                    viewModel = viewModel,
                    onProceedToCanvas = {
                        selectedTab = NavTab.CANVAS
                    },
                    modifier = screenModifier
                )
            }
            NavTab.LOGS -> {
                SystemLogsScreen(
                    viewModel = viewModel,
                    onNavigateToCanvas = {
                        selectedTab = NavTab.CANVAS
                    },
                    modifier = screenModifier
                )
            }
        }
    }
}


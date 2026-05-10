package dev.moonsetter.shengcat

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.moonsetter.shengcat.screens.main.*
import dev.moonsetter.shengcat.screens.practice.*
import dev.moonsetter.shengcat.screens.settings.*
import dev.moonsetter.shengcat.screens.translator.*

sealed class Screen(val route: String) {
    object Main : Screen("main_screen")
//    object Phrasebook : Screen("phrasebook_screen")
    object Translator : Screen("translator_screen")
    object TranslatorHistory : Screen("translator_history_screen")
    object Practice : Screen("practice_screen")
    object PracticeRecognition : Screen("practice_recognition_screen")
    object PracticePronunciation : Screen("practice_pronunciation_screen")
    object PracticeHistory : Screen("practice_history_screen")
    object Settings : Screen("settings_screen")
}

data class NavItem(
    val route: String,
    val icon: ImageVector,
    val label: Int,
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val navItems = listOf(
        NavItem(Screen.Main.route, Icons.Outlined.Home, R.string.main_screen_name),
//        NavItem(Screen.Phrasebook.route, Icons.Outlined.ChatBubbleOutline, R.string.phrasebook_screen_name),
        NavItem(Screen.Translator.route, Icons.Outlined.Translate, R.string.translator_screen_name),
        NavItem(Screen.Practice.route, Icons.Outlined.MusicNote, R.string.practice_screen_name),
        NavItem(Screen.Settings.route, Icons.Outlined.Settings, R.string.settings_screen_name)
    )

    val hideBottomBarRoutes = listOf(
        Screen.TranslatorHistory.route,
        Screen.PracticeRecognition.route,
        Screen.PracticePronunciation.route,
        Screen.PracticeHistory.route
    )

    val showBottomBar = currentRoute !in hideBottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = stringResource(item.label)) },
                            label = { Text(stringResource(item.label)) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = Screen.Main.route, modifier = Modifier.padding(paddingValues))
        {
            composable(route = Screen.Main.route){ MainScreen() }
//            composable(route = Screen.Phrasebook.route){ PhrasebookScreen() }
            composable(route = Screen.Translator.route){ 
                TranslatorScreen(
                    onNavigateToHistory = { navController.navigate(Screen.TranslatorHistory.route) }
                )
            }
            composable(route = Screen.TranslatorHistory.route){
                TranslatorHistoryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(route = Screen.Practice.route) {
                PracticeScreen(
                    onNavigateToPronunciation = { navController.navigate(Screen.PracticePronunciation.route) },
                    onNavigateToRecognition = { navController.navigate(Screen.PracticeRecognition.route) },
                    onNavigateToHistory = { navController.navigate(Screen.PracticeHistory.route) }
                )
            }
            composable(route = Screen.PracticePronunciation.route) {
                PracticePronunciationScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(route = Screen.PracticeRecognition.route) {
                PracticeRecognitionScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(route = Screen.PracticeHistory.route) {
                PracticeHistoryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(route = Screen.Settings.route){ SettingsScreen() }
        }
    }
}
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.remote.configureAppCheck
import com.example.ui.components.GlobalSyncStatusBanner
import com.example.ui.screens.ApplicantProfileScreen
import com.example.ui.screens.AssessmentScreen
import com.example.ui.screens.CertificateScreen
import com.example.ui.screens.DailyChallengeScreen
import com.example.ui.screens.DiagnosticAtlasScreen
import com.example.ui.screens.ExamHistoryScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.FlashcardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MistakesScreen
import com.example.ui.screens.ProgressDashboardScreen
import com.example.ui.screens.PrometricExamScreen
import com.example.ui.screens.PrometricSetupScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.ReviewAnswersScreen
import com.example.ui.screens.SearchQuestionsScreen
import com.example.ui.screens.StageLadderScreen
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: AssessmentViewModel = viewModel()
                    MainAppScaffold(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        configureAppCheck(this, intent)
    }
}

@Composable
fun MainAppScaffold(viewModel: AssessmentViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val authState by viewModel.authUiState.collectAsState()
    val syncState by viewModel.syncUiState.collectAsState()
    val isSyncBannerVisible by viewModel.isSyncBannerVisible.collectAsState()
    val dashboardLanguage by viewModel.dashboardLanguage.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.attemptSilentAutoSignIn(context)
    }

    val isTopLevel = currentScreen is ScreenDestination.Home ||
            currentScreen is ScreenDestination.Dashboard ||
            currentScreen is ScreenDestination.DailyChallenge ||
            currentScreen is ScreenDestination.Search ||
            currentScreen is ScreenDestination.Certificate

    Scaffold(
        bottomBar = {
            if (isTopLevel) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen is ScreenDestination.Home,
                        onClick = { viewModel.navigateTo(ScreenDestination.Home) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("الرئيسية", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MedicalBluePrimary,
                            indicatorColor = MedicalBluePrimary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = currentScreen is ScreenDestination.Dashboard,
                        onClick = { viewModel.navigateTo(ScreenDestination.Dashboard) },
                        icon = { Icon(Icons.Default.QueryStats, contentDescription = "Dashboard") },
                        label = { Text("التقدم", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ClinicalTeal,
                            indicatorColor = ClinicalTeal.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentScreen is ScreenDestination.DailyChallenge,
                        onClick = { viewModel.navigateTo(ScreenDestination.DailyChallenge) },
                        icon = { Icon(Icons.Default.Today, contentDescription = "Daily") },
                        label = { Text("التحدي", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFF9800),
                            indicatorColor = Color(0xFFFF9800).copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_item_daily")
                    )

                    NavigationBarItem(
                        selected = currentScreen is ScreenDestination.Search,
                        onClick = { viewModel.navigateTo(ScreenDestination.Search) },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        label = { Text("البحث", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MedicalBluePrimary,
                            indicatorColor = MedicalBluePrimary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_item_search")
                    )

                    NavigationBarItem(
                        selected = currentScreen is ScreenDestination.Certificate,
                        onClick = { viewModel.navigateTo(ScreenDestination.Certificate) },
                        icon = { Icon(Icons.Default.MilitaryTech, contentDescription = "License") },
                        label = { Text("الترخيص", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldAccent,
                            indicatorColor = GoldAccent.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_cert")
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                GlobalSyncStatusBanner(
                    authState = authState,
                    syncState = syncState,
                    visible = isSyncBannerVisible,
                    language = dashboardLanguage,
                    onRetryClick = { viewModel.syncNow() },
                    onDismissClick = { viewModel.dismissSyncBanner() }
                )
                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (val screen = currentScreen) {
                        is ScreenDestination.Home -> HomeScreen(viewModel = viewModel)
                is ScreenDestination.Dashboard -> ProgressDashboardScreen(viewModel = viewModel)
                is ScreenDestination.DailyChallenge -> DailyChallengeScreen(viewModel = viewModel)
                is ScreenDestination.Search -> SearchQuestionsScreen(viewModel = viewModel)
                is ScreenDestination.Favorites -> FavoritesScreen(viewModel = viewModel)
                is ScreenDestination.Mistakes -> MistakesScreen(viewModel = viewModel)
                is ScreenDestination.Certificate -> CertificateScreen(viewModel = viewModel)
                is ScreenDestination.Profile -> ApplicantProfileScreen(viewModel = viewModel)
                is ScreenDestination.Atlas -> DiagnosticAtlasScreen(viewModel = viewModel)
                is ScreenDestination.Flashcards -> FlashcardScreen(viewModel = viewModel)
                is ScreenDestination.PrometricSetup -> PrometricSetupScreen(viewModel = viewModel)
                is ScreenDestination.PrometricExam -> PrometricExamScreen(viewModel = viewModel)
                is ScreenDestination.ExamHistory -> ExamHistoryScreen(viewModel = viewModel)
                is ScreenDestination.StageLadder -> StageLadderScreen(
                    specialtyId = screen.specialtyId,
                    initialDegree = screen.academicDegree,
                    viewModel = viewModel
                )
                is ScreenDestination.Assessment -> AssessmentScreen(
                    specialtyId = screen.specialtyId,
                    stageLevel = screen.stageLevel,
                    academicDegree = screen.academicDegree,
                    viewModel = viewModel
                )
                is ScreenDestination.Result -> ResultScreen(
                    specialtyId = screen.specialtyId,
                    stageLevel = screen.stageLevel,
                    isPassed = screen.isPassed,
                    academicDegree = screen.academicDegree,
                    isPrometricTimed = screen.isPrometricTimed,
                    viewModel = viewModel
                )
                is ScreenDestination.Review -> ReviewAnswersScreen(
                    specialtyId = screen.specialtyId,
                    stageLevel = screen.stageLevel,
                    academicDegree = screen.academicDegree,
                    sessionId = screen.sessionId,
                    fromHistory = screen.fromHistory,
                    isPrometricTimed = screen.isPrometricTimed,
                    viewModel = viewModel
                )
                    }
                }
            }
        }
    }
}

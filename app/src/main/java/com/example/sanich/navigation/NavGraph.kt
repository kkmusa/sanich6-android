package com.example.sanich.navigation

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sanich.screens.ManualDebitSession
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sanich.CalculationHistoryViewModel
import com.example.sanich.CalculationHistoryViewModelFactory
import com.example.sanich.PedViewModel
import com.example.sanich.PedViewModelFactory
import com.example.sanich.data.PedRepository
import com.example.sanich.data.HistoryDao
import com.example.sanich.screens.AppBottomBar
import com.example.sanich.screens.AboutScreen
import com.example.sanich.screens.FrequencyScreen
import com.example.sanich.screens.HistoryScreen
import com.example.sanich.screens.HomeScreen
import com.example.sanich.screens.TechnologistScreen
import com.example.sanich.screens.SpiritLevelScreen
import com.example.sanich.screens.LevelScreen
import com.example.sanich.screens.ManualDebitScreen
import com.example.sanich.screens.OperatingModeConversion
import com.example.sanich.screens.OperatingModeScreen
import com.example.sanich.screens.OperatorAssistantScreen
import com.example.sanich.screens.PedScreen
import com.example.sanich.screens.VnrReglamentScreen
import com.example.sanich.screens.ReferenceScreen
import com.example.sanich.screens.PrivacyNoticeScreen
import com.example.sanich.screens.PrivacyPolicyScreen
import com.example.sanich.screens.SplashScreen
import com.example.sanich.screens.SkoCalculationScreen
import com.example.sanich.screens.SkoReferenceScreen
import com.example.sanich.screens.TapCalculationScreen
import com.example.sanich.screens.UnifiedFlowScreen
import com.example.sanich.screens.MotorCoefficientScreen
import com.example.sanich.screens.StationModesScreen
import com.example.sanich.screens.OperatorModeFlowScreen
import com.example.sanich.screens.TemperatureSetpointScreen
import com.example.sanich.screens.WellPotentialScreen
import com.example.sanich.screens.RpdScreen
import com.example.sanich.screens.CasingGostReferenceScreen
import com.example.sanich.screens.TubingGostReferenceScreen

@Composable
fun AppNavigation(repository: PedRepository, historyDao: HistoryDao) {
    val navController = rememberNavController()
    val historyViewModel: CalculationHistoryViewModel = viewModel(factory = CalculationHistoryViewModelFactory(historyDao))
    val pedViewModel: PedViewModel = viewModel(factory = PedViewModelFactory(repository))
    val context = LocalContext.current
    val privacyPreferences = context.getSharedPreferences("privacy_preferences", android.content.Context.MODE_PRIVATE)

    fun navigateTab(route: String) {
        navController.navigate(route) {
            popUpTo(Screen.Home.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openFreshCalculator(route: String) {
        navController.navigate(route) {
            // A calculator must always start with fresh input values.
            // Remove any old calculator entry that could remain above Home.
            popUpTo(Screen.Home.route) { inclusive = false }
            launchSingleTop = false
            restoreState = false
        }
    }

    @Composable
    fun BottomBar(route: String) {
        AppBottomBar(currentRoute = route, onNavigate = ::navigateTab)
    }

    // Restore the persisted running session before showing any destination, including Home.
    LaunchedEffect(Unit) { ManualDebitSession.restore(context) }
    val activeEntry by navController.currentBackStackEntryAsState()
    var liveElapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            liveElapsed = ManualDebitSession.elapsedMs()
            delay(500L)
        }
    }
    Box(Modifier.fillMaxSize()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen {
                val privacyNoticeShown = privacyPreferences.getBoolean("privacy_notice_shown", false)
                val destination = if (privacyNoticeShown) Screen.Home.route else Screen.PrivacyNotice.route
                navController.navigate(destination) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }

        composable(Screen.PrivacyNotice.route) {
            PrivacyNoticeScreen(
                onOpenPolicy = { navController.navigate(Screen.PrivacyPolicy.route) },
                onContinue = {
                    privacyPreferences.edit().putBoolean("privacy_notice_shown", true).apply()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.PrivacyNotice.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onLevelClick = { openFreshCalculator(Screen.Level.route) },
                onSpiritLevelClick = { navController.navigate(Screen.SpiritLevel.route) },
                onFrequencyClick = { openFreshCalculator(Screen.Frequency.route) },
                onFlowClick = { openFreshCalculator(Screen.Flow.route) },
                onManualDebitClick = { openFreshCalculator(Screen.ManualDebit.route) },
                onTapCalculationClick = { openFreshCalculator(Screen.TapCalculation.route) },
                onPrToUprClick = { openFreshCalculator(Screen.PrToUpr.route) },
                onUprToApvClick = { openFreshCalculator(Screen.UprToApv.route) },
                onSkoClick = { openFreshCalculator(Screen.SkoCalculation.route) },
                onStationModesClick = { navController.navigate(Screen.StationModes.route) },
                onRampClick = { navController.navigate("operatorRamp") },
                onRichClick = { navController.navigate("operatorRich") },
                onMotorCoefficientClick = { openFreshCalculator(Screen.MotorCoefficient.route) },
                onTemperatureSetpointClick = { openFreshCalculator(Screen.TemperatureSetpoint.route) },
                onWellPotentialClick = { openFreshCalculator(Screen.WellPotential.route) },
                onRpdClick = { openFreshCalculator(Screen.Rpd.route) },
                bottomBar = { BottomBar(Screen.Home.route) }
            )
        }

        composable(Screen.Technologist.route) {
            val activity = LocalContext.current as? Activity
            BackHandler { activity?.finish() }
            TechnologistScreen(
                onFlowClick = { navController.navigate(Screen.Flow.route) },
                onPrToUprClick = { navController.navigate(Screen.PrToUpr.route) },
                onUprToApvClick = { navController.navigate(Screen.UprToApv.route) },
                onSkoClick = { navController.navigate(Screen.SkoCalculation.route) },
                onTemperatureSetpointClick = { navController.navigate(Screen.TemperatureSetpoint.route) },
                onRpdClick = { navController.navigate(Screen.Rpd.route) },
                bottomBar = { BottomBar(Screen.Technologist.route) }
            )
        }

        composable(Screen.MotorCoefficient.route) {
            MotorCoefficientScreen(historyViewModel = historyViewModel, onBack = { navController.popBackStack() })
        }
        composable("operatorRamp") {
            OperatorModeFlowScreen(isRamp = true, onBack = { navController.popBackStack() })
        }
        composable("operatorRich") {
            OperatorModeFlowScreen(isRamp = false, onBack = { navController.popBackStack() })
        }
        composable(Screen.StationModes.route) {
            StationModesScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.TemperatureSetpoint.route) {
            TemperatureSetpointScreen(historyViewModel = historyViewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.WellPotential.route) {
            WellPotentialScreen(historyViewModel = historyViewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.Rpd.route) { RpdScreen(historyViewModel = historyViewModel, onBack = { navController.popBackStack() }) }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = historyViewModel,
                bottomBar = { BottomBar(Screen.History.route) }
            )
        }

        composable(Screen.Reference.route) {
            ReferenceScreen(
                onVnrClick = { navController.navigate(Screen.VnrReglament.route) },
                onPedReferenceClick = { navController.navigate(Screen.PedReference.route) },
                onEmergencyClick = { navController.navigate(Screen.OperatorAssistant.route) },
                onSkoReferenceClick = { navController.navigate(Screen.SkoReference.route) },
                onCasingGostClick = { navController.navigate(Screen.CasingGostReference.route) },
                onTubingGostClick = { navController.navigate(Screen.TubingGostReference.route) },
                onAboutClick = { navController.navigate(Screen.About.route) },
                bottomBar = { BottomBar(Screen.Reference.route) }
            )
        }

        composable(Screen.VnrReglament.route) { VnrReglamentScreen(onBack = { navController.popBackStack() }) }

        composable(Screen.SpiritLevel.route) {
            SpiritLevelScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Level.route) {
            LevelScreen(
                historyViewModel = historyViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Frequency.route) {
            FrequencyScreen(
                historyViewModel = historyViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Flow.route) {
            UnifiedFlowScreen(
                historyViewModel = historyViewModel,
                onBack = { navController.popBackStack() }
            )
        }


        composable(Screen.ManualDebit.route) {
            ManualDebitScreen(
                historyViewModel = historyViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TapCalculation.route) {
            TapCalculationScreen(
                historyViewModel = historyViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PrToUpr.route) {
            OperatingModeScreen(
                conversion = OperatingModeConversion.PR_TO_UPR,
                historyViewModel = historyViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.UprToApv.route) {
            OperatingModeScreen(
                conversion = OperatingModeConversion.UPR_TO_APV,
                historyViewModel = historyViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.OperatorAssistant.route) {
            OperatorAssistantScreen(
                onBack = { navController.popBackStack() }
            )
        }


        composable(Screen.SkoCalculation.route) {
            SkoCalculationScreen(
                historyViewModel = historyViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SkoReference.route) {
            SkoReferenceScreen(onBack = { navController.popBackStack() })
        }


        composable(Screen.CasingGostReference.route) { CasingGostReferenceScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.TubingGostReference.route) { TubingGostReferenceScreen(onBack = { navController.popBackStack() }) }

        composable(Screen.PedReference.route) {
            PedScreen(
                viewModel = pedViewModel,
                historyViewModel = historyViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBack = { navController.popBackStack() },
                onPrivacyPolicyClick = { navController.navigate(Screen.PrivacyPolicy.route) }
            )
        }
    }
    if (ManualDebitSession.running && activeEntry?.destination?.route != Screen.ManualDebit.route) {
        Surface(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 14.dp, bottom = 82.dp)
                .clickable { navController.navigate(Screen.ManualDebit.route) { launchSingleTop = true } },
            shape = CircleShape,
            color = androidx.compose.ui.graphics.Color(0xFF164C87),
            shadowElevation = 6.dp
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("● Замер", color = androidx.compose.ui.graphics.Color.White)
                val value = if (ManualDebitSession.timeInMinutes) liveElapsed / 60000.0 else liveElapsed / 1000.0
                Text(String.format(java.util.Locale.US, "%.2f %s", value, if (ManualDebitSession.timeInMinutes) "мин" else "с"), color = androidx.compose.ui.graphics.Color.White)
            }
        }
    }
    }

}

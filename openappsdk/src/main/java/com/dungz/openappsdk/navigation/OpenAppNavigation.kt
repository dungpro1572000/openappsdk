package com.dungz.openappsdk.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dungz.openappsdk.ui.language.LanguageScreen
import com.dungz.openappsdk.ui.onboarding.OnboardingScreen
import com.dungz.openappsdk.ui.prepare.PrepareDataScreen
import com.dungz.openappsdk.ui.splash.SplashScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// ============================================================================
// Routes - Type-safe navigation with @Serializable
// ============================================================================

@Serializable
sealed interface OpenAppRoute {
    @Serializable
    data object Splash : OpenAppRoute

    @Serializable
    data object Language : OpenAppRoute

    @Serializable
    data object OnBoarding : OpenAppRoute

    @Serializable
    data object PrepareData : OpenAppRoute
}

// ============================================================================
// NavBackStack
// ============================================================================

class NavBackStack(
    initialRoute: OpenAppRoute
) {
    private val _backStack: SnapshotStateList<OpenAppRoute> = mutableStateListOf(initialRoute)

    val current: OpenAppRoute get() = _backStack.last()
    val size: Int get() = _backStack.size
    val canPop: Boolean get() = _backStack.size > 1

    fun navigate(route: OpenAppRoute) {
        _backStack.add(route)
    }

    fun navigateAndClear(route: OpenAppRoute) {
        _backStack.clear()
        _backStack.add(route)
    }

    fun pop(): Boolean {
        return if (_backStack.size > 1) {
            _backStack.removeAt(_backStack.lastIndex)
            true
        } else {
            false
        }
    }

    fun toList(): List<OpenAppRoute> = _backStack.toList()

    companion object {
        fun fromList(routes: List<OpenAppRoute>): NavBackStack {
            val stack = NavBackStack(routes.first())
            routes.drop(1).forEach { stack.navigate(it) }
            return stack
        }
    }
}

val NavBackStackSaver: Saver<NavBackStack, List<String>> = Saver(
    save = { stack ->
        stack.toList().map { route: OpenAppRoute ->
            Json.encodeToString<OpenAppRoute>(route)
        }
    },
    restore = { savedList ->
        val routes = savedList.map { json ->
            Json.decodeFromString<OpenAppRoute>(json)
        }
        NavBackStack.fromList(routes)
    }
)

@Composable
fun rememberNavBackStack(initialRoute: OpenAppRoute): NavBackStack {
    return rememberSaveable(saver = NavBackStackSaver) {
        NavBackStack(initialRoute)
    }
}

// ============================================================================
// OpenAppNavigation - Main navigation composable
// ============================================================================

@Composable
fun OpenAppNavigation(
    onNavigateToMain: () -> Unit
) {
    val backStack = rememberNavBackStack(OpenAppRoute.Splash)

    BackHandler(enabled = backStack.canPop) {
        backStack.pop()
    }

    AnimatedContent(
        targetState = backStack.current,
        transitionSpec = {
            when {
                // Splash transitions - fade only
                targetState is OpenAppRoute.Splash || initialState is OpenAppRoute.Splash -> {
                    fadeIn() togetherWith fadeOut()
                }
                // PrepareData transitions - fade only
                targetState is OpenAppRoute.PrepareData || initialState is OpenAppRoute.PrepareData -> {
                    fadeIn() togetherWith fadeOut()
                }
                // Forward navigation - slide in from right
                else -> {
                    (slideInHorizontally { fullWidth -> fullWidth } + fadeIn()) togetherWith
                            (slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut())
                }
            }
        },
        label = "OpenAppNavigation"
    ) { route ->
        when (route) {
            is OpenAppRoute.Splash -> {
                SplashScreen(
                    onNavigateToMain = onNavigateToMain,
                    onNavigateToLanguage = {
                        backStack.navigateAndClear(OpenAppRoute.Language)
                    }
                )
            }

            is OpenAppRoute.Language -> {
                LanguageScreen(
                    onNavigateToOnBoarding1 = {
                        backStack.navigateAndClear(OpenAppRoute.OnBoarding)
                    }
                )
            }

            is OpenAppRoute.OnBoarding -> {
                OnboardingScreen(
                    onNavigateToPrepareData = {
                        backStack.navigateAndClear(OpenAppRoute.PrepareData)
                    }
                )
            }

            is OpenAppRoute.PrepareData -> {
                PrepareDataScreen(
                    onNavigateToMain = onNavigateToMain
                )
            }
        }
    }
}

package com.dungz.openappsdk.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import com.dungz.openappsdk.model.Language
import com.dungz.openappsdk.ui.language.Language1Screen
import com.dungz.openappsdk.ui.language.Language2Screen
import com.dungz.openappsdk.ui.nativefull.NativeFullScreen
import com.dungz.openappsdk.ui.onboarding.OnBoarding1Screen
import com.dungz.openappsdk.ui.onboarding.OnBoarding2Screen
import com.dungz.openappsdk.ui.prepare.PrepareDataScreen
import com.dungz.openappsdk.ui.splash.SplashScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// ============================================================================
// Routes - Type-safe navigation với @Serializable (Navigation3 style)
// ============================================================================

@Serializable
sealed interface OpenAppRoute {
    @kotlinx.serialization.Serializable
    data object Splash : OpenAppRoute

    @kotlinx.serialization.Serializable
    data object Language1 : OpenAppRoute

    @kotlinx.serialization.Serializable
    data class Language2(val selectedCode: String) : OpenAppRoute

    @kotlinx.serialization.Serializable
    data object OnBoarding1 : OpenAppRoute

    @kotlinx.serialization.Serializable
    data object OnBoarding2 : OpenAppRoute

    @Serializable
    data class NativeFullScreen(
        val adId: String,
        val showAd: Boolean
    ) : OpenAppRoute

    @Serializable
    data object PrepareData : OpenAppRoute
}

// ============================================================================
// NavBackStack - Quản lý navigation state (Navigation3 style)
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

// Saver để lưu trữ NavBackStack qua configuration changes
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
    splashContent: (@Composable () -> Unit)? = null,
    languageItem: (@Composable (language: Language, isSelected: Boolean) -> Unit)? = null,
    onBoarding1Content: (@Composable () -> Unit)? = null,
    onBoarding2Content: (@Composable () -> Unit)? = null,
    prepareDataContent: (@Composable () -> Unit)? = null,
    nativeFullScreenRoute: OpenAppRoute.NativeFullScreen? = null,
    onNavigateToMain: () -> Unit
) {
    val backStack = rememberNavBackStack(OpenAppRoute.Splash)

    // Handle system back button
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
                // NativeFullScreen transitions - fade only
                targetState is OpenAppRoute.NativeFullScreen || initialState is OpenAppRoute.NativeFullScreen -> {
                    fadeIn() togetherWith fadeOut()
                }
                // PrepareData transitions - fade only
                targetState is OpenAppRoute.PrepareData || initialState is OpenAppRoute.PrepareData -> {
                    fadeIn() togetherWith fadeOut()
                }
                // Language1 -> Language2: instant transition (no animation)
                initialState is OpenAppRoute.Language1 && targetState is OpenAppRoute.Language2 -> {
                    EnterTransition.None togetherWith ExitTransition.None
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
                    content = splashContent,
                    onNavigateToMain = onNavigateToMain,
                    onNavigateToLanguage1 = {
                        backStack.navigateAndClear(OpenAppRoute.Language1)
                    }
                )
            }

            is OpenAppRoute.Language1 -> {
                Language1Screen(
                    languageItem = languageItem,
                    onLanguageSelected = { languageCode ->
                        backStack.navigate(OpenAppRoute.Language2(languageCode))
                    }
                )
            }

            is OpenAppRoute.Language2 -> {
                Language2Screen(
                    initialSelectedCode = route.selectedCode,
                    languageItem = languageItem,
                    onSaveAndContinue = {
                        backStack.navigateAndClear(OpenAppRoute.OnBoarding1)
                    }
                )
            }

            is OpenAppRoute.OnBoarding1 -> {
                OnBoarding1Screen(
                    content = onBoarding1Content,
                    onNext = {
                        backStack.navigate(OpenAppRoute.OnBoarding2)
                    }
                )
            }

            is OpenAppRoute.OnBoarding2 -> {
                OnBoarding2Screen(
                    content = onBoarding2Content,
                    onStart = {
                        backStack.navigateAndClear(
                            nativeFullScreenRoute ?: OpenAppRoute.PrepareData
                        )
                    }
                )
            }

            is OpenAppRoute.NativeFullScreen -> {
                NativeFullScreen(
                    adId = route.adId,
                    showAd = route.showAd,
                    onNext = {
                        backStack.navigateAndClear(OpenAppRoute.PrepareData)
                    }
                )
            }

            is OpenAppRoute.PrepareData -> {
                PrepareDataScreen(
                    content = prepareDataContent,
                    onNavigateToMain = onNavigateToMain
                )
            }
        }
    }
}

package com.artgen.app.ui.screen.navigation

import android.content.Intent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.artgen.app.ui.MainActivity
import com.artgen.app.ui.OnboardingActivity
import com.artgen.app.ui.screen.cropphoto.CropPhotoScreen
import com.artgen.app.ui.screen.genart.ArtGeneratorScreen
import com.artgen.app.ui.screen.genart.GenArtSuccessScreen
import com.artgen.app.ui.screen.imagepicker.ImagePickerScreen
import com.artgen.app.ui.screen.language.EditLanguageScreen
import com.artgen.app.ui.screen.language.LanguageSelectionScreen
import com.artgen.app.ui.screen.main.AllStylesScreen
import com.artgen.app.ui.screen.main.StylePickerScreen
import com.artgen.app.ui.screen.onboarding.OnboardingScreen
import com.artgen.app.ui.screen.premium.PremiumScreen
import com.artgen.app.ui.screen.result.ResultScreen
import com.artgen.app.ui.screen.settings.SettingScreen
import com.artgen.app.ui.screen.splash.SplashScreen
import com.artgen.app.utils.LocalRemoteConfig
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainNavigationHost(
    navController: NavHostController,
    onExitApp: () -> Unit
) {
    val context = LocalContext.current
    AnimatedNavHost(
        navController = navController,
        startDestination = MainRoute.SPLASH,
        enterTransition = { defaultEnterTransition() },
        exitTransition = { defaultExitTransition() },
        popEnterTransition = { defaultPopEnterTransition() },
        popExitTransition = { defaultPopExitTransition() },
    ) {
        composable(MainRoute.SPLASH, deepLinks = listOf(navDeepLink {
            uriPattern =
                DeeplinkRoute.IMAGE_PICKER
        })) {
            SplashScreen(
                onNavigateNext = { route ->
                    val deeplinkIntent = it.arguments?.get(
                        "android-support-nav:controller:deepLinkIntent"
                    ) as? Intent
                    val uri = deeplinkIntent?.data?.toString()
                    if (route == MainRoute.LANGUAGE_SELECTION){
                        (context as? MainActivity)?.hideNavigationBar()
                    }

                    navController.navigate(route) {
                        popUpTo(MainRoute.SPLASH) { inclusive = true }
                    }

                    if (route == MainRoute.STYLE_PICKER && uri != null) {
                        navController.navigate(MainRoute.IMAGE_PICKER)
                    }
                }
            )
        }

        composable(MainRoute.LANGUAGE_SELECTION) {
            LanguageSelectionScreen(
                goNext = { route ->
                    val intent = Intent(context, OnboardingActivity::class.java)
                    context.startActivity(intent)
//                    if (remoteConfig.onboardingXml) {
//                        val intent = Intent(context, OnboardingActivity::class.java)
//                        context.startActivity(intent)
//                    } else {
//                        navController.navigate(route) {
//                            popUpTo(MainRoute.LANGUAGE_SELECTION) { inclusive = true }
//                        }
//                    }
                },
                onNavigateUp = { onExitApp() }
            )
        }

        composable(MainRoute.ONBOARDING) {
            OnboardingScreen(onNavigateToMain = {
                navController.navigate(MainRoute.STYLE_PICKER) {
                    popUpTo(MainRoute.ONBOARDING) { inclusive = true }
                }
            }, onNavigateUp = { onExitApp() }, onPageChanged = {

            })
        }

        composable(MainRoute.STYLE_PICKER, deepLinks = listOf(NavDeepLink(DeeplinkRoute.HOME))) {
            StylePickerScreen(
                onNavigateToImagePicker = {
                    navController.navigate(MainRoute.IMAGE_PICKER)
                },
                onNavigateToSetting = {
                    navController.navigate(MainRoute.SETTING)
                },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
                onNavigateUp = {
                    onExitApp()
                }
            )
        }

        composable(
            route = "${MainRoute.CROP_PHOTO}?uri={uri}",
            arguments = listOf(navArgument("uri") {
                type = NavType.StringType
            }) // Define the URI argument
        ) { backStackEntry ->

            val uriString = backStackEntry.arguments?.getString("uri")
            val uri = uriString?.toUri()

            if (uri != null) {
                CropPhotoScreen(
                    uri = uri,
                    onNavigateUp = {
                        navController.navigateUp()
                    },
                    onNavigateToGenArt = {
                        navController.navigate(MainRoute.GEN_ART) {
                            popUpTo(MainRoute.IMAGE_PICKER) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(MainRoute.IMAGE_PICKER) {
            ImagePickerScreen(
                canSkipable = true,
                onNavigateUp = { navController.navigateUp() },
                onSkipClick = {
                    navController.navigate(MainRoute.GEN_ART) {
                        popUpTo(MainRoute.IMAGE_PICKER) { inclusive = true }
                    }
                },
                onNavigateToCrop = {
                    navController.navigate("${MainRoute.CROP_PHOTO}?uri=$it")
                },
            )
        }

        composable(
            route = "${MainRoute.EDIT_CROP_PHOTO}?uri={uri}",
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("uri")
            val uri = uriString?.toUri()

            if (uri != null) {
                CropPhotoScreen(
                    uri = uri,
                    onNavigateUp = {
                        navController.navigateUp()
                    },
                    onNavigateToGenArt = {
                        navController.popBackStack(
                            route = MainRoute.GEN_ART,
                            inclusive = false
                        )
                    }
                )
            }
        }

        composable(MainRoute.EDIT_IMAGE_PICKER) {
            ImagePickerScreen(
                canSkipable = false,
                onNavigateUp = { navController.navigateUp() },
                onSkipClick = {},
                onNavigateToCrop = {
                    navController.navigate("${MainRoute.EDIT_CROP_PHOTO}?uri=$it")
                },
            )
        }

        composable(MainRoute.SETTING) {
            SettingScreen(
                onNavigateUp = { navController.navigateUp() },
                onLanguageClick = {
                    navController.navigate(MainRoute.LANGUAGE_UPDATE)
                }
            )
        }

        composable(MainRoute.LANGUAGE_UPDATE) {
            EditLanguageScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(MainRoute.GEN_ART) {
            ArtGeneratorScreen(
                onNavigateToAllStyle = {
                    navController.navigate(MainRoute.ALL_STYLES)
                },
                onSelectAnotherImage = {
                    navController.navigate(MainRoute.EDIT_IMAGE_PICKER)
                },
                onNavigateToCrop = {
                    navController.navigate("${MainRoute.EDIT_CROP_PHOTO}?uri=$it")
                },
                onNavigateToSuccess = {
                    navController.navigate("${MainRoute.GENERATE_ART_SUCCESS}?uri=$it")
                },
                onNavigateToSetting = {
                    navController.navigate(MainRoute.SETTING)
                },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                }
            )
        }

        composable(MainRoute.ALL_STYLES) {
            AllStylesScreen(
                onNavigateUp = { navController.navigateUp() },
                onDoneClick = {
                    navController.popBackStack(
                        route = MainRoute.GEN_ART,
                        inclusive = false
                    )
                }
            )
        }

        composable(
            route = "${MainRoute.RESULT}?uri={uri}",
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) {
            ResultScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
                onNavigateToAllStyles = {
                    navController.navigate(MainRoute.ALL_STYLES)
                }
            )
        }

        composable(
            route = "${MainRoute.GENERATE_ART_SUCCESS}?uri={uri}",
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) {
            it.arguments?.getString("uri")?.toUri()?.let { uri ->
                GenArtSuccessScreen(
                    uri = uri,
                    onNavigateUp = {
                        navController.navigateUp()
                    },
                    onNavigateToResult = {
                        navController.navigate("${MainRoute.RESULT}?uri=$uri") {
                            popUpTo(MainRoute.GEN_ART) { inclusive = false }
                        }
                    }
                )
            }
        }

        composable(
            route = MainRoute.PREMIUM
        ) {
            PremiumScreen(onNavigateUp = {
                navController.navigateUp()
            })
        }
    }
}

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultEnterTransition(): EnterTransition {
    return slideIntoContainer(AnimatedContentScope.SlideDirection.Start)
}

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultExitTransition(): ExitTransition {
    return slideOutOfContainer(AnimatedContentScope.SlideDirection.Start)
}

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultPopEnterTransition(): EnterTransition {
    return slideIntoContainer(AnimatedContentScope.SlideDirection.End)
}

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultPopExitTransition(): ExitTransition {
    return slideOutOfContainer(AnimatedContentScope.SlideDirection.End)
}

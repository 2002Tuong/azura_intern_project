package com.bloodpressure.app.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun PaddingValues.copy(
    copyStart: Boolean = true,
    copyTop: Boolean = true,
    copyEnd: Boolean = true,
    copyBottom: Boolean = true
): PaddingValues {
    return remember(this) {
        derivedStateOf {
            PaddingValues(
                start = if (copyStart) calculateStartPadding(LayoutDirection.Ltr) else 0.dp,
                top = if (copyTop) calculateTopPadding() else 0.dp,
                end = if (copyEnd) calculateEndPadding(LayoutDirection.Ltr) else 0.dp,
                bottom = if (copyBottom) calculateBottomPadding() else 0.dp
            )
        }
    }.value
}

operator fun PaddingValues.plus(plus: PaddingValues): PaddingValues = PaddingValues(
    start = this.calculateStartPadding(LayoutDirection.Ltr) +
            plus.calculateStartPadding(LayoutDirection.Ltr),
    top = this.calculateTopPadding() + plus.calculateTopPadding(),
    end = this.calculateEndPadding(LayoutDirection.Ltr) +
            plus.calculateEndPadding(LayoutDirection.Ltr),
    bottom = this.calculateBottomPadding() + plus.calculateBottomPadding()
)

fun Modifier.navigationBarsPaddingIfNeed(): Modifier {
    return composed {
        if (LocalRemoteConfig.current.shouldHideNavigationBar) {
            this
        } else {
            navigationBarsPadding()
        }
    }
}

fun Context.getActivity(): AppCompatActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is AppCompatActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

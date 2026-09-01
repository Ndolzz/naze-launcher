package com.naze.launcher.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Six steps, matching the spec exactly. Every step is skippable except the final
 * "Finish" — in particular, location permission is never forced (step 3 has a clear
 * "Not now" path that routes to manual-location entry in Settings later).
 */
@Composable
fun OnboardingScreen(
    onRequestDefaultLauncher: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onFinish: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (step) {
            0 -> OnboardingStep(
                title = "Welcome to Naze Launcher",
                body = "A calm home screen that quietly adapts to your day.",
                primaryLabel = "Get started",
                onPrimary = { step = 1 }
            )
            1 -> OnboardingStep(
                title = "Set as default launcher",
                body = "Make Naze your Home app so it opens whenever you tap Home.",
                primaryLabel = "Set default",
                onPrimary = { onRequestDefaultLauncher(); step = 2 },
                secondaryLabel = "Later",
                onSecondary = { step = 2 }
            )
            2 -> OnboardingStep(
                title = "Location (optional)",
                body = "Lets Naze show local weather automatically. You can also enter your city manually at any time.",
                primaryLabel = "Allow",
                onPrimary = { onRequestLocationPermission(); step = 3 },
                secondaryLabel = "Not now",
                onSecondary = { step = 3 }
            )
            3 -> OnboardingStep(
                title = "Weather",
                body = "Naze checks weather when you open the home screen, and caches it for offline use.",
                primaryLabel = "Continue",
                onPrimary = { step = 4 }
            )
            4 -> OnboardingStep(
                title = "Clock style",
                body = "You can switch between 12/24-hour format and clock size anytime in Settings.",
                primaryLabel = "Continue",
                onPrimary = { step = 5 }
            )
            5 -> OnboardingStep(
                title = "You're all set",
                body = "Welcome home.",
                primaryLabel = "Finish",
                onPrimary = onFinish
            )
        }
    }
}

@Composable
private fun OnboardingStep(
    title: String,
    body: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null
) {
    Text(title, fontSize = 26.sp, fontWeight = FontWeight.Medium)
    Text(body, fontSize = 15.sp, modifier = Modifier.padding(top = 12.dp, bottom = 28.dp))
    Button(onClick = onPrimary) { Text(primaryLabel) }
    if (secondaryLabel != null && onSecondary != null) {
        OutlinedButton(onClick = onSecondary, modifier = Modifier.padding(top = 10.dp)) {
            Text(secondaryLabel)
        }
    }
}

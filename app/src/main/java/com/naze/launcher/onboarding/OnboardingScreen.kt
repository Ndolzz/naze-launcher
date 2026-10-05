package com.naze.launcher.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naze.launcher.theme.LocalNazeDisplayFont
import com.naze.launcher.theme.LocalNazeUiFont
import com.naze.launcher.ui.pressScale
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text

private val OnboardingBackgroundTop = Color(0xFF0A0E1A)
private val OnboardingBackgroundBottom = Color(0xFF151D33)
private val OnboardingAccent = Color(0xFF7DA2FF)
private val OnboardingText = Color(0xFFEAF0FF)

/**
 * Six steps, matching the original spec. Every step is skippable except the final
 * "Finish" — in particular, location permission is never forced (step 3 has a clear
 * "Not now" path that routes to manual-location entry in Settings later).
 *
 * Restyled to match the Naze design system: ambient navy gradient, wordmark,
 * display-font headings, pill buttons and step dots with smooth transitions.
 */
@Composable
fun OnboardingScreen(
    onRequestDefaultLauncher: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onFinish: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(OnboardingBackgroundTop, OnboardingBackgroundBottom))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp)
        ) {
            // Wordmark
            Text(
                text = "NAZE",
                color = OnboardingText.copy(alpha = 0.92f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = LocalNazeDisplayFont.current,
                letterSpacing = 7.sp,
                modifier = Modifier.padding(top = 28.dp)
            )

            // Step content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        (fadeIn(tween(240)) + slideInVertically(tween(240)) { it / 16 })
                      
      .togetherWith(fadeOut(tween(140)))
                    },
                    label = "onboardingStep"
                ) { currentStep ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val stepData = when (currentStep) {
                            0 -> OnboardingStepData(
                                "Welcome to Naze",
                                "A calm, adaptive home screen.\nBuilt for every day.",
                                "Get started", null
                            )
                            1 -> OnboardingStepData(
                                "Set as default launcher",
                                "Make Naze your Home app so it opens whenever you tap Home.",
                                "Set default", "Later",
                                { onRequestDefaultLauncher() }
                            )
                            2 -> OnboardingStepData(
                                "Location (optional)",
                                "Lets Naze show local weather automatically.\nYou can also set a city manually later in Settings.",
                                "Allow", "Not now",
                                { onRequestLocationPermission() }
                            )
                            3 -> OnboardingStepData(
                                "Weather",
                                "Naze checks the weather when you open the home screen and caches it for offline use.",
                                "Continue", null
                            )
                            4 -> OnboardingStepData(
                                "Clock & customization",
                                "12/24-hour format, clock size, gestures, drawer grid and performance mode — all in Settings.",
                                "Continue", null
                            )
                            else -> OnboardingStepData(
                               
 "You're all set",
                                "Welcome home.",
                                "Finish", null
                            )
                        }
                        OnboardingStep(stepData, onAdvance = { step++ }, onFinish = onFinish)
                    }
                }
            }

            // Progress dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 34.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(6) { index ->
                    val active = index == step
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = if (active) 18.dp else 6.dp, height = 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (active) OnboardingAccent
                                else OnboardingText.copy(alpha = 0.22f)
                            )
                    )
                }
            }
        }
    }
}

private data class OnboardingStepData(
    val title: String,
    val body: String,
    val primaryLabel: String,
    val secondaryLabel: String?,
    /** Side effect to run alongside advancing (e.g. the system launcher-role dialog). */
    val onPrimaryExtra: (() -> Unit)? = null
)

@Composable
private fun OnboardingStep(
    data: OnboardingStepData,
    onAdvance: () -> Unit,
    onFinish: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = data.title,
            color = OnboardingText,
            fontSize = 30.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = LocalNazeDisplayFont.current,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = 
data.body,
            color = OnboardingText.copy(alpha = 0.62f),
            fontSize = 15.sp,
            lineHeight = 23.sp,
            fontFamily = LocalNazeUiFont.current,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(36.dp))

        OnboardingButton(
            label = data.primaryLabel,
            primary = true,
            onClick = {
                data.onPrimaryExtra?.invoke()
                if (data.primaryLabel == "Finish") onFinish() else onAdvance()
            }
        )

        if (data.secondaryLabel != null) {
            Spacer(Modifier.height(12.dp))
            OnboardingButton(
                label = data.secondaryLabel,
                primary = false,
                onClick = onAdvance
            )
        }
    }
}

@Composable
private fun OnboardingButton(
    label: String,
    primary: Boolean,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction, pressedScale = 0.97f)
            .clip(RoundedCornerShape(50))
            .background(
                if (primary) OnboardingAccent else OnboardingText.copy(alpha = 0.08f)
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (primary) Color(0xFF0A0E1A) else OnboardingText.copy(alpha = 0.85f),
            fontSize = 15.sp,
            fontWeight = if (primary) FontWeight.SemiBold else FontWeight.Medium,
            fontFamily = LocalNazeUiFont.current
        )
    }
}

package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.domain.PolicyType
import com.example.ui.theme.Bronze700
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold400
import com.example.ui.theme.Sapphire700

@Composable
fun SpatialBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val bg = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val outline = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Top-right emerald ambient depth orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primary.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(size.width * 0.88f, size.height * 0.10f),
                    radius = size.width * 0.72f
                ),
                center = Offset(size.width * 0.88f, size.height * 0.10f),
                radius = size.width * 0.72f
            )
            // Mid-left warm gold ambient depth orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondary.copy(alpha = 0.09f), Color.Transparent),
                    center = Offset(size.width * 0.10f, size.height * 0.58f),
                    radius = size.width * 0.60f
                ),
                center = Offset(size.width * 0.10f, size.height * 0.58f),
                radius = size.width * 0.60f
            )
            // Subtle architectural 3D grid lines
            val step = 44.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = outline.copy(alpha = 0.35f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = outline.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += step
            }
        }
        content()
    }
}

@Composable
fun Spatial3DCard(
    modifier: Modifier = Modifier,
    depth: Dp = 6.dp,
    cornerRadius: Dp = 22.dp,
    surfaceColor: Color = MaterialTheme.colorScheme.surface,
    bevelColor: Color = MaterialTheme.colorScheme.outline,
    glowColor: Color = MaterialTheme.colorScheme.primary,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val specularBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.75f),
            bevelColor.copy(alpha = 0.55f)
        )
    )

    Box(modifier = modifier) {
        // Extruded 3D plinth layer underneath
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = depth)
                .shadow(
                    elevation = 14.dp,
                    shape = shape,
                    ambientColor = glowColor.copy(alpha = 0.25f),
                    spotColor = glowColor.copy(alpha = 0.25f)
                )
                .clip(shape)
                .background(bevelColor.copy(alpha = 0.85f))
        )

        // Foreground elevated card face with specular rim
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.2.dp, specularBrush, shape),
            shape = shape,
            color = surfaceColor,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}

@Composable
fun Spatial3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    plinthColor: Color = Emerald900,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    testTag: String = "spatial_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetY by animateDpAsState(
        targetValue = if (isPressed && enabled) 4.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "button_press_offset"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "button_press_scale"
    )

    val shape = RoundedCornerShape(18.dp)
    val activeBg = if (enabled) containerColor else containerColor.copy(alpha = 0.45f)
    val activePlinth = if (enabled) plinthColor else plinthColor.copy(alpha = 0.35f)

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(bottom = 5.dp)
    ) {
        // 3D base plinth
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 5.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = shape,
                    ambientColor = containerColor.copy(alpha = 0.4f),
                    spotColor = containerColor.copy(alpha = 0.4f)
                )
                .clip(shape)
                .background(activePlinth)
        )

        // Tactile face
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            activeBg.copy(alpha = 0.95f),
                            activeBg
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.48f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    ),
                    shape = shape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = androidx.compose.material3.ripple(color = Color.White),
                    enabled = enabled,
                    onClick = onClick
                )
                .testTag(testTag)
                .padding(horizontal = 20.dp, vertical = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun Spatial3DChip(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 3.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "chip_offset"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "chip_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        },
        label = "chip_border"
    )

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .padding(bottom = 3.dp)
    ) {
        // 3D bevel base
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 3.dp)
                .clip(shape)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
                .clip(shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = androidx.compose.material3.ripple(),
                    onClick = onClick
                )
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
            shape = shape,
            color = bgColor,
            border = BorderStroke(if (selected) 1.6.dp else 1.dp, borderColor)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (selected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Spatial3DPolicyDeck(
    availableTypes: List<PolicyType>,
    selectedType: PolicyType,
    versionByType: Map<PolicyType, String>,
    onSelectPolicy: (PolicyType) -> Unit,
    modifier: Modifier = Modifier
) {
    val useHorizontalScroll = availableTypes.size > 3
    val rowModifier = if (useHorizontalScroll) {
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
    } else {
        modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    }

    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        availableTypes.forEach { type ->
            val isSelected = type == selectedType
            val accent = when (type) {
                PolicyType.DATA_PROTECTION -> Emerald700
                PolicyType.ACCEPTABLE_USE -> Sapphire700
                PolicyType.PASSWORD -> Bronze700
                PolicyType.INCIDENT_RESPONSE -> Color(0xFFB91C1C)
                PolicyType.BUSINESS_CONTINUITY -> Color(0xFF0F766E)
            }

            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.03f else 0.96f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "deck_scale"
            )
            val rotX by animateFloatAsState(
                targetValue = if (isSelected) 0f else 10f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "deck_rotX"
            )
            val translateY by animateDpAsState(
                targetValue = if (isSelected) (-4).dp else 2.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "deck_translateY"
            )

            val shape = RoundedCornerShape(18.dp)
            val cardModifier = if (useHorizontalScroll) {
                Modifier.width(142.dp)
            } else {
                Modifier.weight(1f)
            }

            Box(
                modifier = cardModifier
                    .zIndex(if (isSelected) 2f else 0f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        rotationX = rotX
                        cameraDistance = 14f * density
                    }
                    .offset(y = translateY)
            ) {
                // 3D plinth
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(y = 5.dp)
                        .shadow(
                            elevation = if (isSelected) 14.dp else 4.dp,
                            shape = shape,
                            ambientColor = accent.copy(alpha = 0.35f),
                            spotColor = accent.copy(alpha = 0.35f)
                        )
                        .clip(shape)
                        .background(if (isSelected) accent else MaterialTheme.colorScheme.outline)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clip(shape)
                        .clickable { onSelectPolicy(type) }
                        .testTag("policy_deck_${type.name.lowercase()}"),
                    shape = shape,
                    color = if (isSelected) accent else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Gold400 else accent.copy(alpha = 0.7f)
                                    )
                            )
                            Text(
                                text = "${type.codePrefix} • ${versionByType[type] ?: "v1.0"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White.copy(alpha = 0.88f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = type.shortTitle,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isSelected) "Active View" else "Tap to View",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

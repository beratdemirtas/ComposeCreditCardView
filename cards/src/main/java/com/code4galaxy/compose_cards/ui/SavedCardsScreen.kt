package com.code4galaxy.compose_cards.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.animateFloatAsState
import com.code4galaxy.compose_cards.data.CardRepository
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SavedCardsScreen(
    savedCards: List<List<TextFieldValue>>,
    onAddNewCard: () -> Unit
) {
    LazyColumn(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
        item {
            val cardHeight = 200.dp
            val centerWidthFraction = 0.88f
            val sideVisible = 32.dp
            var selectedIndex by remember { mutableStateOf(0) }

            androidx.compose.foundation.layout.BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight + 72.dp),
            ) {
                var dragX by remember { mutableStateOf(0f) }
                val threshold = 72f

                val parentWidth = this.maxWidth
                val centerWidth = parentWidth * centerWidthFraction
                val parentHalf = parentWidth / 2
                val centerHalf = centerWidth / 2

                val rightTargetCenter = (parentWidth - sideVisible) + centerHalf
                val leftTargetCenter = (sideVisible - centerHalf)
                val rightShift = (rightTargetCenter - parentHalf)
                val leftShift = (parentHalf - leftTargetCenter)

                savedCards.forEachIndexed { index, card ->
                    val rel = index - selectedIndex
                    val clamped = rel.coerceIn(-1, 1)
                    val distance = kotlin.math.abs(rel)
                    val baseX = when {
                        clamped > 0 -> rightShift
                        clamped < 0 -> -leftShift
                        else -> 0.dp
                    }
                    val scale by animateFloatAsState(if (clamped == 0) 1f else 0.98f, label = "")
                    val alpha = if (distance <= 1) 1f else 0f

                    Box(
                        modifier = Modifier
                            .offset(x = baseX)
                            .align(Alignment.Center)
                            .fillMaxWidth(centerWidthFraction)
                            .graphicsLayer {
                                shape = RoundedCornerShape(20.dp)
                                clip = true
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            }
                            .zIndex((100 - distance).toFloat())
                            .clickable { selectedIndex = index }
                            .pointerInput(selectedIndex, savedCards.size) {
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragX += dragAmount
                                    },
                                    onDragEnd = {
                                        when {
                                            dragX > threshold && selectedIndex > 0 -> selectedIndex -= 1
                                            dragX < -threshold && selectedIndex < savedCards.lastIndex -> selectedIndex += 1
                                        }
                                        dragX = 0f
                                    }
                                )
                            }
                    ) {
                        CreditCard(
                            cardNumber = card[0],
                            holderName = card[1],
                            expiryDate = card[2],
                            cardCVV = card[3]
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .matchParentSize()
                        .pointerInput(selectedIndex, savedCards.size) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragX += dragAmount
                                },
                                onDragEnd = {
                                    when {
                                        dragX > threshold && selectedIndex > 0 -> selectedIndex -= 1
                                        dragX < -threshold && selectedIndex < savedCards.lastIndex -> selectedIndex += 1
                                    }
                                    dragX = 0f
                                }
                            )
                        }
                )

                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                ) {
                    savedCards.forEachIndexed { i, _ ->
                        val isActive = i == selectedIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isActive) 10.dp else 8.dp)
                                .background(
                                    color = if (isActive) MaterialTheme.colors.onBackground else Color.Gray.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (selectedIndex in 0 until savedCards.size) {
                        CardRepository.removeAt(selectedIndex)
                        val newSize = savedCards.size
                        selectedIndex = (newSize - 1).coerceAtLeast(0)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 40.dp, end = 40.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.error)
            ) {
                Text(
                    text = "Delete Selected",
                    color = MaterialTheme.colors.onPrimary
                )
            }
        }
        item {
            Button(
                onClick = onAddNewCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp, end = 40.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.onBackground)
            ) {
                Text(
                    text = "Add New Card",
                    color = MaterialTheme.colors.onPrimary
                )
            }
        }
    }
}

@Preview
@Composable
fun savedCardsPreview() {
    val sampleCards = listOf(
        listOf(
            TextFieldValue("4434132141234132"),
            TextFieldValue("BERAT DEMIRTAS"),
            TextFieldValue("1229"),
            TextFieldValue("123")
        )
    )
    SavedCardsScreen(savedCards = sampleCards, onAddNewCard = {})
}
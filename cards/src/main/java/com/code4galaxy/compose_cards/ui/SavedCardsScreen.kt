package com.code4galaxy.compose_cards.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
// Snap fling not available in this Compose version; we'll implement manual snapping
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.code4galaxy.compose_cards.data.CardRepository
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedCardsScreen(
    savedCards: List<List<TextFieldValue>>,
    onAddNewCard: () -> Unit
) {
    LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
        item {
            val cardHeight = 200.dp
            val centerWidthFraction = 0.76f
            val sideVisible = 16.dp
            var selectedIndex by remember { mutableStateOf(0) }

            androidx.compose.foundation.layout.BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight + 72.dp),
            ) {
                val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
                val parentWidth = this.maxWidth
                val itemWidth = parentWidth * centerWidthFraction
                val spacing = 0.dp
                val peek = 24.dp
                val centerPadding = (parentWidth - itemWidth) / 2
                val peekShown = (centerPadding - 50.dp).coerceAtMost(peek).coerceAtLeast(0.dp)
                val edgePadding = (centerPadding - peekShown)
                val density = LocalDensity.current
                val scope = rememberCoroutineScope()

                val centeredIndex by remember {
                    derivedStateOf {
                        val layout = listState.layoutInfo
                        val center = (layout.viewportEndOffset + layout.viewportStartOffset) / 2f
                        val closest = layout.visibleItemsInfo.minByOrNull { info ->
                            val itemCenter = info.offset + info.size / 2f
                            kotlin.math.abs(itemCenter - center)
                        }
                        closest?.index ?: selectedIndex
                    }
                }

                LaunchedEffect(centeredIndex) {
                    selectedIndex = centeredIndex
                }

                LazyRow(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = edgePadding),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(spacing),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    itemsIndexed(savedCards, key = { i, _ -> i }) { index, card ->
                        val scale by remember {
                            derivedStateOf {
                                val layout = listState.layoutInfo
                                val info = layout.visibleItemsInfo.find { it.index == index }
                                if (info != null) {
                                    val center = (layout.viewportEndOffset + layout.viewportStartOffset) / 2f
                                    val itemCenter = info.offset + info.size / 2f
                                    val dist = kotlin.math.abs(itemCenter - center)
                                    val norm = (dist / info.size).coerceIn(0f, 1f)
                                    0.95f + (1f - norm) * 0.05f
                                } else 0.95f
                            }
                        }

                        Box(
                            modifier = Modifier
                                .width(itemWidth)
                                .height(cardHeight)
                                .graphicsLayer {
                                    shape = RoundedCornerShape(20.dp)
                                    clip = true
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .zIndex(if (index == selectedIndex) 2f else 1f)
                                .clickable {
                                    if (index != selectedIndex) {
                                        val peekShownPx = with(density) { (centerPadding - edgePadding).toPx() }
                                        val offset = -peekShownPx.toInt()
                                        scope.launch {
                                            listState.animateScrollToItem(index, offset)
                                            selectedIndex = index
                                        }
                                    }
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
                }

                // Snap to the nearest item when scroll settles, centering it in viewport
                LaunchedEffect(listState.isScrollInProgress, centeredIndex) {
                    if (!listState.isScrollInProgress) {
                        val peekShownPx = with(density) { (centerPadding - edgePadding).toPx() }
                        // Shift left by visible peek so item center aligns with viewport center
                        val offset = -peekShownPx.toInt()
                        listState.animateScrollToItem(centeredIndex, offset)
                        selectedIndex = centeredIndex
                    }
                }

                // Background gesture catcher removed; swipe handled by swipeable overlay.

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
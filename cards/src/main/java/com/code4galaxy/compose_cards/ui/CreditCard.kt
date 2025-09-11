package com.code4galaxy.compose_cards.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.compose.ui.text.style.TextAlign
import com.code4galaxy.compose_cards.util.Card


@Composable
fun CreditCard(
    cardNumber: TextFieldValue,
    holderName: TextFieldValue,
    expiryDate: TextFieldValue,
    cardCVV: TextFieldValue
) {
    var cardType by remember { mutableStateOf(Card.None) }

    val displayedNumberRaw = cardNumber.text.take(16).padEnd(16, '*')
    val displayedNumber = displayedNumberRaw.chunked(4).joinToString(" ")
    val numberFontSize = 22.sp

    val cvv = if (cardCVV.text.length > 3) 3 else cardCVV.text.length
    val maskedCVV = remember { "*".repeat(3) }.replaceRange(0 until cvv, cardCVV.text.take(3))

    cardType = when {
        cardNumber.text.startsWith("9792") -> Card.Troy
        cardNumber.text.take(2) == "30" || cardNumber.text.take(2) == "36" || cardNumber.text.take(2) == "38" -> Card.DinersClub
        cardNumber.text.startsWith("4") -> Card.Visa
        cardNumber.text.take(2) in listOf("50", "51", "52", "53", "54", "55") -> Card.Mastercard
        cardNumber.text.take(2) in listOf("56", "57", "58", "63", "67") -> Card.Maestro
        cardNumber.text.startsWith("60") -> Card.RuPay
        cardNumber.text.startsWith("37") -> Card.AmericanExpress
        else -> Card.None
    }

    val animatedColor = animateColorAsState(
        targetValue =
            when (cardType) {
                Card.Visa -> Color(0xFF1C478B)
                Card.Mastercard -> Color(0xFF3BB9A1)
                Card.RuPay -> Color(0xFFB2B1FD)
                Card.AmericanExpress -> Color(0xFFA671FC)
                Card.Maestro -> Color(0xFF99BEF8)
                Card.DinersClub -> Color(0xFFFC4444)
                Card.Troy -> Color(0xFF45AF97)
                else -> MaterialTheme.colors.onBackground
            },
        label = ""
    )

    Surface(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        color = animatedColor.value,
        elevation = 0.dp

    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (cardImage, cardName, cardHolderName, number, expiryLabel, cvc, cvcLabel) = createRefs()

                AnimatedVisibility(visible = cardType != Card.None,
                    modifier = Modifier
                        .padding(start = 12.dp, top = 10.dp)
                        .constrainAs(cardImage) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                        }) {
                    Image(
                        painter = painterResource(id = cardType.image),
                        contentDescription = "Card Image"
                    )
                }

                Text(
                    text = displayedNumber,
                    style = MaterialTheme.typography.h5.copy(fontSize = numberFontSize),
                    maxLines = 1,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .animateContentSize(spring())
                        .padding(bottom = 20.dp)
                        .constrainAs(number) {
                            linkTo(
                                start = parent.start,
                                end = parent.end
                            )
                            linkTo(
                                top = parent.top,
                                bottom = parent.bottom
                            )
                            width = Dimension.fillToConstraints
                        }
                )
                if (holderName.text.isEmpty()) {
                    Text(
                        text = "Card Holder Name",
                        color = Color.White,
                        modifier = Modifier
                            .animateContentSize(TweenSpec(300))
                            .padding(top = 10.dp, start = 16.dp, bottom = 16.dp)
                            .constrainAs(cardName) {
                                start.linkTo(parent.start)
                                bottom.linkTo(parent.bottom)
                            },
                        maxLines = 1
                    )
                }
                else
                Text(
                    text = holderName.text,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false,
                    style = MaterialTheme.typography.body1.copy(
                        fontSize = if (holderName.text.length > 20) 12.sp else 14.sp
                    ),
                    modifier = Modifier
                        .animateContentSize(TweenSpec(300))
                        .padding(top = 10.dp, start = 16.dp, bottom = 16.dp)
                        .constrainAs(cardName) {
                            start.linkTo(parent.start)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        }
                )

                Text(
                    text = "SKT",
                    color = Color.White,
                    style = MaterialTheme.typography.caption,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .constrainAs(expiryLabel) {
                            start.linkTo(parent.start)
                            bottom.linkTo(cardName.top)
                        }
                )

                Text(
                    text = expiryDate.text.take(4).chunked(2).joinToString(" / "),
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 4.dp, start = 10.dp, bottom = 16.dp)
                        .constrainAs(cardHolderName) {
                            start.linkTo(expiryLabel.end)
                            baseline.linkTo(expiryLabel.baseline)
                        }
                )

                Text(
                    text = "CVC",
                    color = Color.White,
                    style = MaterialTheme.typography.caption,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .constrainAs(cvcLabel) {
                            end.linkTo(parent.end)
                            bottom.linkTo(cvc.top)
                        }
                )

                Text(
                    text = maskedCVV.take(3),
                    color = Color.White,
                    modifier = Modifier
                        .padding(end = 16.dp, bottom = 16.dp)
                        .constrainAs(cvc) {
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        }
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewPaymentCard() {
    CreditCard(
        TextFieldValue("*****************"),
        TextFieldValue("BERAT DEMIRTAS"),
        TextFieldValue("0229"),
        TextFieldValue("123")
    )
}
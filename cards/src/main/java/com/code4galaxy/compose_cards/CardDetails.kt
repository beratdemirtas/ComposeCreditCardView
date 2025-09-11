package com.code4galaxy.compose_cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.code4galaxy.compose_cards.component.CardNumberFilter
import com.code4galaxy.compose_cards.component.InputTextField
import com.code4galaxy.compose_cards.ui.CreditCard
import com.code4galaxy.compose_cards.data.CardRepository
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText

fun filterDigits(input: String, maxLength: Int): String =
    input.filter { it.isDigit() }.take(maxLength)

fun filterName(input: String): String =
    input.filter { it.isLetter() || it.isWhitespace() }.uppercase()

class ExpiryDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(4)
        val formatted = when {
            trimmed.length <= 2 -> trimmed
            else -> trimmed.substring(0, 2) + "/" + trimmed.substring(2)
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 4) return offset + 1
                return 5
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                return 4
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

@Composable
fun CardDetails() {
    var cardNumber by remember { mutableStateOf(TextFieldValue()) }
    var cardHolderName by remember { mutableStateOf(TextFieldValue()) }
    var expiryDate by remember { mutableStateOf(TextFieldValue()) }
    var cardCVV by remember { mutableStateOf(TextFieldValue()) }
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    Column(modifier = Modifier.fillMaxSize()) {

        CreditCard(
            cardNumber = cardNumber,
            holderName = cardHolderName,
            expiryDate = expiryDate,
            cardCVV = cardCVV
        )

        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            item {
                InputTextField(
                    textFieldValue = cardNumber,
                    label = stringResource(R.string.your_card_number),
                    keyboardType = KeyboardType.Number,
                    onTextChanged = { newValue ->
                        val filtered = filterDigits(newValue.text,16)
                        cardNumber = TextFieldValue(filtered, selection = TextRange(filtered.length))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    visualTransformation = CardNumberFilter
                )
            }

            item {
                InputTextField(
                    textFieldValue = cardHolderName,
                    label = "Card Holder Name",
                    onTextChanged = { newValue ->
                        val filtered = filterName(newValue.text)
                        cardHolderName = newValue.copy(text = filtered, selection = TextRange(filtered.length))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InputTextField(
                        textFieldValue = expiryDate,
                        label = stringResource(R.string.expiry_date),
                        keyboardType = KeyboardType.Number,
                        onTextChanged = { newValue ->
                            val filtered = filterDigits(newValue.text, 4)
                            expiryDate = newValue.copy(text = filtered, selection = TextRange(filtered.length))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        visualTransformation = ExpiryDateVisualTransformation()
                    )
                    InputTextField(
                        textFieldValue = cardCVV,
                        label = stringResource(R.string.cvv),
                        keyboardType = KeyboardType.Number,
                        onTextChanged = { newValue ->
                            val filtered = filterDigits(newValue.text, 3)
                            cardCVV = newValue.copy(text = filtered, selection = TextRange(filtered.length))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        val snapshot = listOf(
                            TextFieldValue(cardNumber.text),
                            TextFieldValue(cardHolderName.text),
                            TextFieldValue(expiryDate.text),
                            TextFieldValue(cardCVV.text)
                        )
                        CardRepository.addCard(snapshot)

                        backDispatcher?.onBackPressed()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colors.onBackground)
                ) {
                    Text(
                        text = stringResource(id = R.string.save),
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.padding(horizontal = 30.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFE,
    device = "id:pixel_6_pro"
)
@Composable
fun Card() {
    CardDetails()
}

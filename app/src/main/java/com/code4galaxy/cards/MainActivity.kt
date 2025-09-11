package com.code4galaxy.cards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.code4galaxy.compose_cards.CardDetails
import com.code4galaxy.cards.ui.theme.ComposeCardsTheme
import com.code4galaxy.compose_cards.ui.SavedCardsScreen
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.code4galaxy.compose_cards.data.CardRepository
import androidx.core.content.edit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeCardsTheme {
                val navController = rememberNavController()
                CardRepository.init(this)
                CardRepository.load()
                NavHost(navController = navController, startDestination = "payment") {
                    composable("payment") {
                        SavedCardsScreen(
                            savedCards = CardRepository.savedCards,
                            onAddNewCard = { navController.navigate("addCard") }
                        )
                    }
                    composable("addCard") {
                        CardDetails()
                    }
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun Card() {
    CardDetails()
}
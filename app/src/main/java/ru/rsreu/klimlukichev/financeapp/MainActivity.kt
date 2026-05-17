package ru.rsreu.klimlukichev.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.rsreu.klimlukichev.financeapp.ui.home.HomeScreen
import ru.rsreu.klimlukichev.financeapp.ui.theme.FinanceAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceAppTheme {
                HomeScreen()
            }
        }
    }
}

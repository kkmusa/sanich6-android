package com.example.sanich

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.sanich.data.DatabaseProvider
import com.example.sanich.data.PedRepository
import com.example.sanich.data.HistoryDatabase
import com.example.sanich.navigation.AppNavigation
import com.example.sanich.ui.theme.SanichTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = DatabaseProvider.getDatabase(this)
        val repository = PedRepository(database.pedDao())
        val historyDatabase = HistoryDatabase.getInstance(this)

        setContent {
            SanichTheme {
                AppNavigation(repository = repository, historyDao = historyDatabase.historyDao())
            }
        }
    }
}

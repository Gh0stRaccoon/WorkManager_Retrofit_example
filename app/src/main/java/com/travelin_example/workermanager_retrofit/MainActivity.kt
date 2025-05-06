package com.travelin_example.workermanager_retrofit

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.travelin_example.workermanager_retrofit.ui.theme.WorkerManager_retrofitTheme
import com.travelin_example.workermanager_retrofit.worker.TokenRefreshWorker
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

val Context.dataStore by preferencesDataStore(name = "amadeus_prefs")
val TOKEN_KEY = stringPreferencesKey("access_token")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Worker cada 20 minutos
        val workRequest = PeriodicWorkRequestBuilder<TokenRefreshWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "AmadeusTokenRefresh",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        // Ejecutar una vez al abrir app
        val request = OneTimeWorkRequestBuilder<TokenRefreshWorker>().build()
        WorkManager.getInstance(this).enqueue(request)


        setContent {
            WorkerManager_retrofitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TokenDisplay(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TokenDisplay(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val tokenFlow = remember {
        context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY] ?: "Token no disponible aún"
        }
    }

    val token by tokenFlow.collectAsState(initial = "Cargando token...")

    Column(modifier = modifier) {
        Text("Token actual:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(token, style = MaterialTheme.typography.bodyMedium)
    }
}
package com.travelin_example.workermanager_retrofit.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.travelin_example.workermanager_retrofit.data.remote.api.AmadeusAuthService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.datastore.preferences.core.edit
import com.travelin_example.workermanager_retrofit.TOKEN_KEY
import com.travelin_example.workermanager_retrofit.dataStore

class TokenRefreshWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.v("TokenRefreshWorker", "🚧 Punto de control: antes de Retrofit")

        return try {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://test.api.amadeus.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val authService = retrofit.create(AmadeusAuthService::class.java)

            val response = authService.getAccessToken(
                clientId = "",
                clientSecret = ""
            )

            Log.v("TokenRefreshWorker", "✅ Token recibido desde Amadeus: ${response.accessToken}")

            guardarToken(response.accessToken)

            Result.success()
        } catch (e: Exception) {
            Log.e("TokenRefreshWorker", "Error al refrescar token", e)
            Result.retry()
        }
    }

    private suspend fun guardarToken(token: String) {
        applicationContext.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }
}

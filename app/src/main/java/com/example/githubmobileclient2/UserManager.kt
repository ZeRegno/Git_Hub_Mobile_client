package com.example.githubmobileclient2

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Сохранение токена авторизации в фоновом потоке
    suspend fun saveAuthToken(authToken: String) {
        withContext(Dispatchers.IO) {
            val editor = sharedPreferences.edit()
            editor.putString("AUTH_TOKEN", authToken)
            editor.apply() // Применяем изменения
        }
    }

    // Получение токена авторизации в фоновом потоке
    suspend fun getAuthToken(): String? {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getString("AUTH_TOKEN", null)
        }
    }

    // Сохранение данных пользователя (username и токен) в фоновом потоке
    suspend fun saveUserData(username: String, authToken: String) {
        withContext(Dispatchers.IO) {
            val editor = sharedPreferences.edit()
            editor.putString("username", username)
            editor.putString("auth_token", authToken)
            editor.apply()
        }
    }

    // Получение данных пользователя (username и токен) в фоновом потоке
    suspend fun getUserData(): Pair<String, String>? {
        return withContext(Dispatchers.IO) {
            val username = sharedPreferences.getString("username", null)
            val authToken = sharedPreferences.getString("auth_token", null)
            if (username != null && authToken != null) {
                Pair(username, authToken)
            } else {
                null
            }
        }
    }

    // Очистка данных пользователя в фоновом потоке
    suspend fun clearUserData() {
        withContext(Dispatchers.IO) {
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
        }
    }
}
package com.example.githubmobileclient2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var showPasswordCheckBox: CheckBox
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.editTextLogin)
        passwordEditText = findViewById(R.id.editTextPassword)
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox)
        loginButton = findViewById(R.id.buttonLogin)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Запускаем операцию авторизации в фоновом потоке
                lifecycleScope.launch {
                    authenticateUser(username, password)
                }
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            passwordEditText.inputType = if (isChecked) {
                android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
    }


    private suspend fun authenticateUser(username: String, password: String) {
        try {

            val isAuthenticated = withContext(Dispatchers.IO) {

                true // Для примера, считаем, что авторизация успешна
            }

            if (isAuthenticated) {
                startAuthActivity(username, password)
            } else {
                // Обработка неудачной авторизации (например, неверные логин/пароль)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            // Обработка ошибок (например, сети или сервера)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@LoginActivity, "Error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startAuthActivity(username: String, password: String) {
        val intent = Intent(this, AuthActivity::class.java).apply {
            putExtra("username", username)
            putExtra("password", password)
        }
        startActivity(intent)
    }
}
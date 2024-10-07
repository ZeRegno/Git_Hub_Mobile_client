package com.example.githubmobileclient2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class RepositoryListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var repositoryAdapter: RepositoryAdapter
    private val apiService: ApiService by lazy { ApiService.create() }
    private lateinit var progressBar: ProgressBar
    private lateinit var btnChangeUser: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.repository_list_activity)

        Log.d("RepositoryListActivity", "onCreate called, initializing RecyclerView")

        recyclerView = findViewById(R.id.repositoriesRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        btnChangeUser = findViewById(R.id.btnChangeUser)  // Инициализация кнопки

        recyclerView.layoutManager = LinearLayoutManager(this)

        val authToken = intent.getStringExtra("auth_token")

        if (authToken != null) {
            Log.d("RepositoryListActivity", "Auth token received, starting fetchRepositories")
            fetchRepositories(authToken)
        } else {
            Log.e("RepositoryListActivity", "Auth token is null, cannot proceed")
        }


        btnChangeUser.setOnClickListener {
            showChangeUserDialog()
        }
    }


    private fun showChangeUserDialog() {
        AlertDialog.Builder(this)
            .setTitle("Сменить пользователя")
            .setMessage("Вы уверены, что хотите сменить пользователя?")
            .setPositiveButton("Да") { dialog, _ ->
                dialog.dismiss()

                startAuthActivity()
            }
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private fun startAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun fetchRepositories(authToken: String) {
        lifecycleScope.launch {
            val token = "Bearer $authToken"


            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            btnChangeUser.visibility = View.GONE

            try {
                val repositories = apiService.listRepos(token)


                val commitLists = mutableMapOf<String, List<Commit>>()


                for (repository in repositories) {
                    try {
                        val commits = apiService.listCommits(token, repository.owner.login, repository.name)
                        commitLists[repository.name] = commits
                    } catch (e: Exception) {
                        Log.e("API_ERROR", "Failed to fetch commits for ${repository.name}: ${e.message}")
                    }
                }


                repositoryAdapter = RepositoryAdapter(repositories, commitLists)
                recyclerView.adapter = repositoryAdapter

            } catch (e: HttpException) {
                Log.e("API_ERROR", "HTTP error: ${e.message}")
            } catch (e: IOException) {
                Log.e("API_ERROR", "Network error: ${e.message}")
            } catch (e: Exception) {
                Log.e("API_ERROR", "Unexpected error: ${e.message}")
            } finally {

                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                btnChangeUser.visibility = View.VISIBLE
            }
        }
    }
}
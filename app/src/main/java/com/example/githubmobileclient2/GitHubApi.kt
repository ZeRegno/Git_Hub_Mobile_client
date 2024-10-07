package com.example.githubmobileclient2


import Repository
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Header

interface GitHubApi {
    @GET("user/repos")
    suspend fun listRepos(@Header("Authorization") authHeader: String): List<Repository>
}

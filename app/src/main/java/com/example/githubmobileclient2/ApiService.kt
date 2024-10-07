package com.example.githubmobileclient2

import Repository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import com.example.githubmobileclient2.Commit
import retrofit2.http.Path

interface ApiService {

    @GET("user")
    suspend fun getUserInfo(
        @Header("Authorization") token: String
    ): User

    @GET("user/repos")
    suspend fun listRepos(
        @Header("Authorization") token: String
    ): List<Repository>
    @GET("repos/{owner}/{repo}/commits")
    suspend fun listCommits(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): List<Commit>

    companion object {
        private const val BASE_URL = "https://api.github.com/"

        fun create(): ApiService {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val httpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
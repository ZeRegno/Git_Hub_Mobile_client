package com.example.githubmobileclient2

data class Commit(
    val sha: String,
    val commit: CommitDetails,
    val author: UserGit?
)

data class CommitDetails(
    val message: String,
    val author: CommitAuthor
)

data class CommitAuthor(
    val name: String,
    val date: String
)

data class UserGit(
    val login: String,
    val avatar_url: String
)
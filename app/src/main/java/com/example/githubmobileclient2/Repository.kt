data class Repository(
    val name: String,
    val description: String?,
    val owner: Owner,
    val forks: Int,
    val watchers: Int
)

data class Owner(
    val login: String,
    val avatarUrl: String
)
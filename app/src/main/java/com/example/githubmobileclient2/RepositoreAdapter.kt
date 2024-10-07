package com.example.githubmobileclient2

import Repository
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RepositoryAdapter(
    private val repositories: List<Repository>,
    private val commitLists: Map<String, List<Commit>>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_REPOSITORY = 0
        private const val TYPE_COMMIT = 1
    }


    private val expandedStates = mutableMapOf<String, Boolean>()

    override fun getItemViewType(position: Int): Int {
        var currentPos = 0
        for (repository in repositories) {
            if (currentPos == position) return TYPE_REPOSITORY
            currentPos++

            if (expandedStates[repository.name] == true) {
                val commitCount = commitLists[repository.name]?.size ?: 0
                for (commitIndex in 0 until commitCount) {
                    if (currentPos == position) return TYPE_COMMIT
                    currentPos++
                }
            }
        }
        throw IllegalArgumentException("Invalid position: $position")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_REPOSITORY) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_repository, parent, false)
            RepositoryViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_commit_row, parent, false)
            CommitViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return repositories.sumOf { 1 + if (expandedStates[it.name] == true) (commitLists[it.name]?.size ?: 0) else 0 }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var currentPos = 0
        for (repository in repositories) {
            if (currentPos == position) {
                (holder as RepositoryViewHolder).bind(repository, this)
                return
            }

            if (expandedStates[repository.name] == true) {
                val commitCount = commitLists[repository.name]?.size ?: 0
                for (commitIndex in 0 until commitCount) {
                    currentPos++
                    if (currentPos == position) {
                        (holder as CommitViewHolder).bind(commitLists[repository.name]!![commitIndex])
                        return
                    }
                }
            }
            currentPos++
        }
    }


    inner class RepositoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.repositoryName)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.repositoryDescription)
        private val authorTextView: TextView = itemView.findViewById(R.id.repositoryAuthor)
        private val forksTextView: TextView = itemView.findViewById(R.id.repositoryForks)
        private val watchersTextView: TextView = itemView.findViewById(R.id.repositoryWatchers)
        private val avatarImageView: ImageView = itemView.findViewById(R.id.repositoryAvatar)
        private val arrowIcon: ImageView = itemView.findViewById(R.id.arrowIcon)

        fun bind(repository: Repository, adapter: RepositoryAdapter) {
            nameTextView.text = repository.name
            authorTextView.text = repository.owner.login
            descriptionTextView.text = repository.description
            forksTextView.text = "Forks: ${repository.forks}"
            watchersTextView.text = "Watchers: ${repository.watchers}"

            Glide.with(itemView.context)
                .load(repository.owner.avatarUrl)
                .into(avatarImageView)


            val isExpanded = expandedStates[repository.name] ?: false
            arrowIcon.setImageResource(if (isExpanded) R.drawable.arrow_drop_up else R.drawable.arrow_drop_down)


            arrowIcon.setOnClickListener {
                val newState = !isExpanded
                expandedStates[repository.name] = newState
                adapter.notifyDataSetChanged()
            }
        }
    }


    class CommitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hashTextView: TextView = itemView.findViewById(R.id.commitHash)
        private val messageTextView: TextView = itemView.findViewById(R.id.commitMessage)
        private val authorTextView: TextView = itemView.findViewById(R.id.commitAuthor)
        private val dateTextView: TextView = itemView.findViewById(R.id.commitDate)
        private val avatarImageView: ImageView = itemView.findViewById(R.id.commitAuthorAvatar)

        fun bind(commit: Commit) {
            hashTextView.text = commit.sha.substring(0, 10)
            messageTextView.text = commit.commit.message
            authorTextView.text = commit.commit.author.name
            dateTextView.text = commit.commit.author.date

            Glide.with(itemView.context)
                .load(commit.author?.avatar_url)
                .into(avatarImageView)
        }
    }
}

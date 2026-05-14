package com.example.grama_suvidha

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load

class ProjectAdapter : ListAdapter<Project, ProjectAdapter.ProjectViewHolder>(ProjectDiffCallback()) {

    class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProjectId: TextView = itemView.findViewById(R.id.tvProjectId)
        val tvProjectName: TextView = itemView.findViewById(R.id.tvProjectName)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        val tvBudget: TextView = itemView.findViewById(R.id.tvBudget)
        val tvExpectedDate: TextView = itemView.findViewById(R.id.tvExpectedDate)
        val ivProjectThumb: android.widget.ImageView = itemView.findViewById(R.id.ivProjectThumb)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = getItem(position)
        val context: Context = holder.itemView.context
        
        holder.tvProjectId.text = "ID: ${project.id}"
        holder.tvProjectName.text = project.name
        holder.tvStatus.text = context.getString(R.string.status_text, project.status)
        holder.progressBar.progress = project.progress
        holder.tvProgress.text = context.getString(R.string.progress_text, project.progress)
        
        // Budget & Expected Completion
        holder.tvBudget.text = context.getString(R.string.budget_text, project.budget)
        holder.tvExpectedDate.text = context.getString(R.string.expected_date_text, project.expectedCompletion)

        // Load Thumbnail using Coil
        holder.ivProjectThumb.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        val thumbUrl = project.imageUrlAfter ?: project.imageUrlBefore
        if (!thumbUrl.isNullOrEmpty()) {
            holder.ivProjectThumb.load(android.net.Uri.parse(thumbUrl)) {
                crossfade(true)
                allowHardware(false)
                placeholder(R.drawable.ic_project_placeholder)
                error(R.drawable.ic_project_placeholder)
            }
        }

        val cardStatus: com.google.android.material.card.MaterialCardView = holder.itemView.findViewById(R.id.cardStatus)

        // Set colors based on status
        when (project.statusEn.lowercase()) {
            "completed" -> {
                holder.tvStatus.setTextColor(context.getColor(R.color.status_completed))
                cardStatus.setCardBackgroundColor(context.getColor(R.color.status_completed_bg))
            }
            "in progress" -> {
                holder.tvStatus.setTextColor(context.getColor(R.color.status_in_progress))
                cardStatus.setCardBackgroundColor(context.getColor(R.color.status_in_progress_bg))
            }
            else -> {
                holder.tvStatus.setTextColor(context.getColor(R.color.status_pending))
                cardStatus.setCardBackgroundColor(context.getColor(R.color.status_pending_bg))
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProjectDetailsActivity::class.java)
            intent.putExtra("PROJECT_DATA", project)
            context.startActivity(intent)
        }
    }

    class ProjectDiffCallback : DiffUtil.ItemCallback<Project>() {
        override fun areItemsTheSame(oldItem: Project, newItem: Project): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Project, newItem: Project): Boolean {
            return oldItem == newItem
        }
    }
}

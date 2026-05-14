package com.example.grama_suvidha

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FeedbackAdapter(private var feedbacks: List<FeedbackEntry>) :
    RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    class FeedbackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUser: TextView = view.findViewById(R.id.tvFeedbackUser)
        val tvDate: TextView = view.findViewById(R.id.tvFeedbackDate)
        val tvComment: TextView = view.findViewById(R.id.tvFeedbackComment)
        val ratingBar: RatingBar = view.findViewById(R.id.feedbackRating)
        val ivImage: ShapeableImageView = view.findViewById(R.id.ivFeedbackImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbacks[position]
        holder.tvUser.text = feedback.userName
        holder.ratingBar.rating = feedback.rating
        holder.tvComment.text = feedback.comment
        
        // Format date
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(feedback.createdAt))

        if (!feedback.imageUri.isNullOrEmpty()) {
            holder.ivImage.visibility = View.VISIBLE
            holder.ivImage.load(android.net.Uri.parse(feedback.imageUri)) {
                crossfade(true)
                transformations(RoundedCornersTransformation(16f))
            }
        } else {
            holder.ivImage.visibility = View.GONE
        }
    }

    override fun getItemCount() = feedbacks.size

    fun updateData(newFeedbacks: List<FeedbackEntry>) {
        feedbacks = newFeedbacks
        notifyDataSetChanged()
    }
}

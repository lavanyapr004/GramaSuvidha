package com.example.grama_suvidha

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserIssuesActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        // enableEdgeToEdge() // Disabled for stability check
        LocaleHelper.updateConfiguration(this, LocaleHelper.getLanguage(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_issues)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.issuesToolbar).setNavigationOnClickListener {
            finish()
        }

        val rvIssues = findViewById<RecyclerView>(R.id.rvUserIssues)
        rvIssues.layoutManager = LinearLayoutManager(this)

        val tvEmpty = findViewById<TextView>(R.id.tvEmptyIssues)

        val prefs = getSharedPreferences("GramaSuvidhaPrefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("loggedInUserId", 0)

        val db = AppDatabase.getDatabase(this)
        
        CoroutineScope(Dispatchers.Main).launch {
            val issues = withContext(Dispatchers.IO) {
                db.issueDao().getIssuesByUser(userId)
            }
            
            if (issues.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                rvIssues.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                rvIssues.visibility = View.VISIBLE
                rvIssues.adapter = IssuesAdapter(issues)
            }
        }
    }

    class IssuesAdapter(private val issues: List<Issue>) : RecyclerView.Adapter<IssuesAdapter.IssueViewHolder>() {
        
        class IssueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvIssueTitle)
            val tvProject: TextView = view.findViewById(R.id.tvIssueProject)
            val tvDesc: TextView = view.findViewById(R.id.tvIssueDescription)
            val tvStatus: TextView = view.findViewById(R.id.tvIssueStatus)
            val tvDate: TextView = view.findViewById(R.id.tvIssueDate)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_issue, parent, false)
            return IssueViewHolder(view)
        }

        override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
            val issue = issues[position]
            holder.tvTitle.text = issue.category
            holder.tvProject.text = "Project: ${issue.projectName}"
            holder.tvDesc.text = issue.description
            holder.tvStatus.text = "SUBMITTED"
            
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            holder.tvDate.text = "Reported on: ${sdf.format(Date(issue.createdAt))}"
        }

        override fun getItemCount() = issues.size
    }
}

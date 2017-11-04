package ru.a1024bits.bytheway.adapter;

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.ui.activity.MenuActivity

class ShowAllUsersAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    private var glide: RequestManager = Glide.with(this.context)
    var users: MutableList<User> = ArrayList()

    override fun getItemViewType(position: Int): Int {
        return if (users.isEmpty()) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    fun addItems(list: List<User>) {
        this.users = list as MutableList<User>
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        if (viewType == VIEW_TYPE_ITEM) {
            return UserViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_content_users, parent, false)
            )
        } else if (viewType == VIEW_TYPE_LOADING) {
            return LoadingViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            )
        }
        return null
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UserViewHolder) {
            val currentUser = users[position]
            holder.lastName.text = currentUser.lastName
            holder.name.text = currentUser.name
            glide.load(currentUser.urlPhoto).into(holder.avatar)
            holder.itemView.setOnClickListener {
                if (context is MenuActivity) {
                    context.showUserSimpleProfile(currentUser)
                }
            }
        } else if (holder is LoadingViewHolder) {
            holder.progressBar.isIndeterminate = true
        }
    }

    override fun getItemCount(): Int {
        return if (users.isEmpty()) 1 else users.size
    }

    private inner class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
    }

    private inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var lastName = view.findViewById<TextView>(R.id.lastName_content_user)
        var name = view.findViewById<TextView>(R.id.name_content_user)
        var avatar = view.findViewById<ImageView>(R.id.user_avatar)
    }
}
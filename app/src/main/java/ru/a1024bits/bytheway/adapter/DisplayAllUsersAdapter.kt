package ru.a1024bits.bytheway.adapter;

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import ru.a1024bits.bytheway.ExtensionsAllUsers
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.ui.activity.MenuActivity

class DisplayAllUsersAdapter(val context: Context, val extensions: ExtensionsAllUsers) : RecyclerView.Adapter<DisplayAllUsersAdapter.UserViewHolder>() {
    private var glide: RequestManager = Glide.with(this.context)
    var users: MutableList<User> = ArrayList()


    fun setItems(users: List<User>) {
        this.users = users as MutableList<User>
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_content_all_users, parent, false)
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder?, position: Int) {
        val currentUser = users[position]
        holder?.name?.text = currentUser.name
        holder?.dates?.text = currentUser.dates["start_date"]?.let { currentUser.dates["end_date"]?.let { it1 -> extensions.getTextFromDates(it, it1) } }
        glide.load(currentUser.urlPhoto).into(holder?.avatar)
        holder?.itemView?.setOnClickListener {
            if (context is MenuActivity) {
                context.showUserSimpleProfile(currentUser)
            }
        }
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name = view.findViewById<TextView>(R.id.name_content_user)
        var avatar = view.findViewById<ImageView>(R.id.user_avatar)
        var dates = view.findViewById<TextView>(R.id.users_dates)
    }
}
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
import com.bumptech.glide.request.RequestOptions
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.ui.activity.MenuActivity

class ShowAllUsersAdapter(val context: Context) : RecyclerView.Adapter<ShowAllUsersAdapter.UserViewHolder>() {
    private var glide: RequestManager = Glide.with(this.context)
    var users: MutableList<User> = ArrayList()
    
    
    fun setItems(list: List<User>) {
        this.users = list as MutableList<User>
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_content_all_users, parent, false)
        )
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = users[position]
        holder.lastName.text = currentUser.lastName
        holder.name.text = currentUser.name
        glide.load(currentUser.urlPhoto)
                .apply(RequestOptions().circleCrop())
                .into(holder.avatar)
        holder.itemView.setOnClickListener {
            if (context is MenuActivity) {
                context.showUserSimpleProfile(currentUser)
            }
        }
    }
    
    override fun getItemCount(): Int {
        return users.size
    }
    
    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var lastName = view.findViewById<TextView>(R.id.text_city)
        var name = view.findViewById<TextView>(R.id.name_content_user)
        var avatar = view.findViewById<ImageView>(R.id.user_avatar)
    }
}
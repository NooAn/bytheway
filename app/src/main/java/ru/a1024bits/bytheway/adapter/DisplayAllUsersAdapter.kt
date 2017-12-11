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
import com.google.firebase.auth.FirebaseAuth
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
        holder?.dates?.text = if (currentUser.dates["start_date"] != null && currentUser.dates["end_date"] != null)
            extensions.getTextFromDates(currentUser.dates["start_date"], currentUser.dates["end_date"], 1)
        else
            context.getString(R.string.item_all_users_empty_date)
        if (currentUser.age > 0)
            holder?.age?.text = if (currentUser.age > 0) StringBuilder(", ").append(currentUser.age.toString()) else ""
        holder?.cities?.text = if (currentUser.cities["first_city"] != null && currentUser.cities["last_city"] != null)
            StringBuilder(currentUser.cities["first_city"]).append(" - ").append(currentUser.cities["last_city"])
        else
            context.getString(R.string.not_cities)
        glide.load(currentUser.urlPhoto)
                ?.apply(RequestOptions.circleCropTransform())
                ?.into(holder?.avatar)
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
        var age = view.findViewById<TextView>(R.id.age_content_user)
        var cities = view.findViewById<TextView>(R.id.users_cities)
    }
}
package ru.a1024bits.bytheway.adapter;

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.analytics.FirebaseAnalytics
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.viewmodel.DisplayUsersViewModel

class DisplayAllUsersAdapter(val context: Context, val viewModel: DisplayUsersViewModel) : RecyclerView.Adapter<DisplayAllUsersAdapter.UserViewHolder>() {
    private var glide: RequestManager = Glide.with(this.context)
    var users: MutableList<User> = ArrayList()


    fun setItems(users: List<User>) {
        this.users = users as MutableList<User>
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder =
            UserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_content_all_users, parent, false))

    override fun onBindViewHolder(holder: UserViewHolder?, position: Int) {
        val currentUser = users[position]
        holder?.name?.text = currentUser.name
        holder?.dates?.text = if (currentUser.dates["start_date"] != null && currentUser.dates["end_date"] != null)
            viewModel.getTextFromDates(currentUser.dates["start_date"], currentUser.dates["end_date"], 1)
        else
            context.getString(R.string.item_all_users_empty_date)
        if (currentUser.age >= 0)
            holder?.age?.text = if (currentUser.age > 0) StringBuilder(", ").append(currentUser.age.toString()) else ""
        holder?.cities?.text = if (currentUser.cities["first_city"] != null && currentUser.cities["last_city"] != null)
            StringBuilder(currentUser.cities["first_city"]).append(" - ").append(currentUser.cities["last_city"])
        else
            context.getString(R.string.not_cities)
        glide.load(currentUser.urlPhoto)
                ?.apply(RequestOptions.circleCropTransform())
                ?.into(holder?.avatar)
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.setOnClickListener({ _ ->
                if (adapterPosition != RecyclerView.NO_POSITION && context is MenuActivity) {
                    FirebaseAnalytics.getInstance(context.applicationContext).logEvent("AllUsersFragment_SELECT_USER_" + adapterPosition, null)
                    context.showUserSimpleProfile(users[adapterPosition])
                }
            })
        }

        var name = view.findViewById<TextView>(R.id.nameContentUser)
        var avatar = view.findViewById<ImageView>(R.id.userAvatar)
        var dates = view.findViewById<TextView>(R.id.usersDates)
        var age = view.findViewById<TextView>(R.id.ageContentUser)
        var cities = view.findViewById<TextView>(R.id.usersCities)
    }
}
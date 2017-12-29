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
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY


class SimilarTravelsAdapter(val context: Context) : RecyclerView.Adapter<SimilarTravelsAdapter.UserViewHolder>() {
    private var glide: RequestManager = Glide.with(this.context)
    var users: MutableList<User> = ArrayList()

    fun addItems(list: List<User>) {
        this.users = list as MutableList<User>
        notifyDataSetChanged()
        Log.d("LOG", "addItems: " + list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_content_similar_travel, parent, false)
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        Log.d("LOG", "onBindViewHolder on position: " + position)
        val currentUser = users[position]

        holder.cities.text = StringBuilder().append(getShortCity(currentUser.cities.get(FIRST_INDEX_CITY) ?: ""))
                .append(" - ")
                .append(getShortCity(currentUser.cities.get(LAST_INDEX_CITY) ?: ""))

        holder.name.text = StringBuilder().append(currentUser.name)
                .append(" ")
                .append(currentUser.lastName)
                .append(", ")
                .append(currentUser.age)

        holder.percentSimilarTravel.text = StringBuilder().append(currentUser.percentsSimilarTravel).append(" %")

        holder.percentSimilarTravel.setTextColor(when (position) {
            0 -> context.resources.getColor(R.color.one_level)
            1, 2 -> context.resources.getColor(R.color.two_level)
            else -> {
                context.resources.getColor(R.color.all_level)
            }
        })

        glide.load(currentUser.urlPhoto)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.avatar)

        holder.itemView.setOnClickListener {
            if (context is MenuActivity) {
                context.showUserSimpleProfile(currentUser)
            }
        }
    }

    private fun getShortCity(city: String): String? {
        val n = if (city?.length > 14) 14 else city.length
        val shortCity = city?.substring(0, n)
        if (shortCity?.length == city?.length) {
            return city
        } else {
            return shortCity + "..."
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var cities = view.findViewById<TextView>(R.id.users_cities)
        var name = view.findViewById<TextView>(R.id.name_content_user)
        var avatar = view.findViewById<ImageView>(R.id.user_avatar)
        var percentSimilarTravel = view.findViewById<TextView>(R.id.percent_similar_travel)
    }
}

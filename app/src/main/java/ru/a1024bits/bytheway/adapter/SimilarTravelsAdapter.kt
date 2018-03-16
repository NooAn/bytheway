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
import com.google.firebase.analytics.FirebaseAnalytics
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY


class SimilarTravelsAdapter(val context: Context, val users: List<User>) : RecyclerView.Adapter<SimilarTravelsAdapter.UserViewHolder>() {
    private var glide: RequestManager = Glide.with(this.context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_content_similar_travel, parent, false)
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = users[position]
        holder.cities.text = if (currentUser.cities["first_city"] != null && currentUser.cities["last_city"] != null)
            StringBuilder(getShortCity(currentUser.cities[FIRST_INDEX_CITY]?.trim() ?: ""))
                    .append(" - ")
                    .append(getShortCity(currentUser.cities[LAST_INDEX_CITY]?.trim() ?: ""))
        else
            context.getString(R.string.not_cities)

        val nameText = StringBuilder().append(currentUser.name).append(" ").append(currentUser.lastName)
        if (currentUser.age > 0) nameText.append(", ").append(currentUser.age)
        holder.name.text = nameText

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

    }

    private fun getShortCity(city: String): String? {
        val n = if (city.length > 19) 19 else city.length
        val shortCity = city.substring(0, n)
        return if (shortCity.length == city.length) {
            city
        } else {
            shortCity + "..."
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var cities = view.findViewById<TextView>(R.id.usersCities)
        var name = view.findViewById<TextView>(R.id.nameContentUser)
        var avatar = view.findViewById<ImageView>(R.id.userAvatar)
        var percentSimilarTravel = view.findViewById<TextView>(R.id.percent_similar_travel)

        init {
            view.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION && context is MenuActivity) {
                    if (adapterPosition <= 10) FirebaseAnalytics.getInstance(context.applicationContext).logEvent("SimilarTravelsFragment_SELECT_USER_" + adapterPosition, null)
                    context.showUserSimpleProfile(users[adapterPosition])
                }
            }
        }
    }
}

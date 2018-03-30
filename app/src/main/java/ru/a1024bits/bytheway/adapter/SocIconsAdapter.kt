package ru.a1024bits.bytheway.adapter


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import ru.a1024bits.bytheway.R


class SocIconsAdapter(val list: List<Int>, private val listener: (Int) -> Unit) : RecyclerView.Adapter<SocIconsAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position], position, listener)


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_icons, parent, false))

    override fun getItemCount() = list.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Int, pos: Int, listener: (Int) -> Unit) = with(itemView) {
            val icon = itemView.findViewById<ImageView>(R.id.icon)
            icon.setImageResource(item)
            icon.setOnClickListener {
                listener(pos)
            }
        }
    }
}
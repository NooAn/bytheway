package ru.a1024bits.bytheway.adapter;

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.MockUserRepository

class ShowAllUsersAdapter(recyclerView: RecyclerView, val context: Context, var senderUsers: MockUserRepository)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1
    private var isLoading: Boolean = false
    private val visibleThreshold = 5
    private var lastVisibleItem: Int = 0
    private var totalItemCount: Int = 0
    private var setterUsersToThisAdapter: Observer<User>
    var users: MutableList<User> = ArrayList()

    init {
        setterUsersToThisAdapter = object : Observer<User> {
            override fun onError(e: Throwable) {
                throw e
            }

            override fun onComplete() {
                isLoading = false
                this@ShowAllUsersAdapter.notifyDataSetChanged()
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onNext(t: User) {
                t.let { users.add(it) }
            }

        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                totalItemCount = linearLayoutManager.itemCount
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                if (!isLoading && (recyclerView.layoutManager.itemCount - visibleThreshold) <= lastVisibleItem) {
                    isLoading = true
                    senderUsers.installChanUsers(setterUsersToThisAdapter, lastVisibleItem + 1L)
                }
            }
        })
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == users.size) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.user_list_content, parent, false)
            return UserViewHolder(view)
        } else if (viewType == VIEW_TYPE_LOADING) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            return LoadingViewHolder(view)
        }
        return null
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UserViewHolder) {
            val contact = this.users[position]
            val userViewHolder = holder
            userViewHolder.lastName.text = contact.lastName
            userViewHolder.name.text = contact.name
        } else if (holder is LoadingViewHolder) {
            holder.progressBar.isIndeterminate = true
        }
    }

    override fun getItemCount(): Int {
        return this.users.size + 1
    }

    private inner class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var progressBar = view.findViewById<ProgressBar>(R.id.progressBar1)
    }

    private inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var lastName = view.findViewById<TextView>(R.id.lastName_content_user)
        var name = view.findViewById<TextView>(R.id.name_content_user)
    }
}
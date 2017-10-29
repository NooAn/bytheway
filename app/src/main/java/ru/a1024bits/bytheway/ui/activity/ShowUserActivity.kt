package ru.a1024bits.bytheway.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import retrofit2.http.GET
import retrofit2.http.Query
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.User
import javax.inject.Inject

class ShowUsersActivity : AppCompatActivity() {
    private lateinit var showingUsers: MutableList<User>
    private lateinit var showingUsersAdapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_users)

        showingUsers = ArrayList()
        var recyclerView: RecyclerView = findViewById(R.id.lazy_shower_users)
        showingUsersAdapter = UsersAdapter(recyclerView, showingUsers, this, MockGeneratorData(object : MockWebService {
            override fun getChanUsers(fromCount: Long, count: Int): List<User> {
                return (0..40).map { User("" + it, "" + it, it) }
            }

        }))
        recyclerView.setAdapter(showingUsersAdapter)
    }
}


class UsersAdapter(recyclerView: RecyclerView, val users: MutableList<User>, val activity: Activity, var senderUsers: MockGeneratorData)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1
    private var isLoading: Boolean = false
    private val visibleThreshold = 5
    private var lastVisibleItem: Int = 0
    private var totalItemCount: Int = 0
    private var setterUsersToThisAdapter: Observer<User>

    init {
        setterUsersToThisAdapter = object : Observer<User> {
            override fun onError(e: Throwable) {
                throw e
            }

            override fun onComplete() {
                isLoading = false
                Log.d("tag", "size users:  " + users.size)
                this@UsersAdapter.notifyDataSetChanged()
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onNext(t: User) {
                t.let { users.add(it) }
            }

        }
        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = linearLayoutManager.itemCount
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                if (!isLoading && totalItemCount <= lastVisibleItem + visibleThreshold) {
                    isLoading = true
                    this@UsersAdapter.notifyDataSetChanged()
                    senderUsers.installChanUsers(setterUsersToThisAdapter, lastVisibleItem + 1L)
                    //todo create optional user for show load
                }
            }
        })
        senderUsers.installChanUsers(setterUsersToThisAdapter, 0)
//        )
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == users.size) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(activity).inflate(R.layout.user_list_content, parent, false)
            return UserViewHolder(view)
        } else if (viewType == VIEW_TYPE_LOADING) {
            val view = LayoutInflater.from(activity).inflate(R.layout.item_loading, parent, false)
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
        var progressBar: ProgressBar = view.findViewById<ProgressBar>(R.id.progressBar1)
    }

    private inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var lastName: TextView = view.findViewById<TextView>(R.id.lastName_content_user)
        var name: TextView = view.findViewById<TextView>(R.id.name_content_user)
    }
}

class MockGeneratorData @Inject constructor(private val webService: MockWebService) {
    class ValAs(private val fromCount: Long, private val senderElements: ObservableEmitter<User>, private val webService: MockWebService)
        : AsyncTask<Void, User, Void>() {

        override fun doInBackground(vararg p0: Void): Void? {
            try {
                Thread.sleep(4000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            val res = webService.getChanUsers(fromCount)
            for (i in res) {
                senderElements.onNext(i)
                Log.d("tag", " " + i)
            }
            return null
        }

        override fun onProgressUpdate(vararg values: User) {
            senderElements.onNext(values[0])
        }

        override fun onPostExecute(v: Void?) {
            senderElements.onComplete()
        }
    }


    fun installChanUsers(getterUsers: Observer<User>, fromCount: Long) {
        Observable.create(ObservableOnSubscribe<User> { senderElements ->
            //todo on retrofit
            ValAs(fromCount, senderElements, webService).execute()

//            var res = webService.getChanUsers(fromCount)
//
////                listener.onCompleteGetChanUsers(response.body()!!)
//            for (i in res) {
//                senderElements.onNext(i)
//            }
//            getterUsers.onComplete()
//                }
//            }).onResponse(res, null)
        }).subscribe(getterUsers)
//                observable.subscribe(getterUsers)
    }
}

interface MockWebService {

    @GET("/users")
    fun getChanUsers(@Query("fromCount") fromCount: Long, @Query("count") count: Int = 20): List<User>
}

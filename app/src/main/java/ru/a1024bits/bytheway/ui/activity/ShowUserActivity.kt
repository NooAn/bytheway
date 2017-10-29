package ru.a1024bits.bytheway.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import ru.a1024bits.bytheway.MockWebService
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.adapter.ShowAllUsersAdapter
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.MockGeneratorData

class ShowUsersActivity : AppCompatActivity() {
    private lateinit var showingUsers: MutableList<User>
    private lateinit var showingUsersAdapter: ShowAllUsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_users)

        showingUsers = ArrayList()
        var recyclerView: RecyclerView = findViewById(R.id.lazy_shower_users)
        showingUsersAdapter = ShowAllUsersAdapter(recyclerView, showingUsers, this, MockGeneratorData(object : MockWebService {
            override fun getChanUsers(fromCount: Long, count: Int): List<User> {
                return (0..40).map { User("" + it, "" + it, it) }
            }

        }))
        recyclerView.setAdapter(showingUsersAdapter)
    }
}
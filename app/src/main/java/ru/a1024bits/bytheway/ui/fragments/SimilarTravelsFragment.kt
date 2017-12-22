package ru.a1024bits.bytheway.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_display_similar_user_travels.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.adapter.SimilarTravelsAdapter
import ru.a1024bits.bytheway.model.User
import java.util.*

class SimilarTravelsFragment : Fragment() {
    private lateinit var currentView: View
    private lateinit var showUsersAdapter: SimilarTravelsAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_display_similar_user_travels, container, false)
        return currentView
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = currentView.findViewById(R.id.display_similar_user_travels)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.component.inject(this)
        showUsersAdapter = SimilarTravelsAdapter(this.context)
        recyclerView.adapter = showUsersAdapter
        if (listUser != null) {
            if (listUser?.isNotEmpty() == true) {
                block_empty_users.visibility = View.GONE
                showUsersAdapter.addItems(listUser ?: arrayListOf())
            }
        }
    }

    var listUser: List<User>? = null

    companion object {
        fun newInstance(list: List<User>): SimilarTravelsFragment {
            val fragment = SimilarTravelsFragment()
            fragment.arguments = Bundle()
            fragment.listUser = list
            return fragment
        }
    }
}
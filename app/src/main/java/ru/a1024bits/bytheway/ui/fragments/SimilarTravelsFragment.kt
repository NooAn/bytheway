package ru.a1024bits.bytheway.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.fragment_display_similar_user_travels.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.adapter.SimilarTravelsAdapter
import ru.a1024bits.bytheway.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SimilarTravelsFragment : Fragment() {
    var listUser: List<User>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null && listUser == null) {
            val message = savedInstanceState.getString(SAVE)
            listUser = Gson().fromJson(message, object : TypeToken<List<User>>() {}.type)
        }
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_display_similar_user_travels, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FirebaseAnalytics.getInstance(context.applicationContext).setCurrentScreen(this.activity, "SimilarTravelsFragment", this.javaClass.simpleName)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putString(SAVE, Gson().toJson(listUser))
        super.onSaveInstanceState(outState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        block_empty_users.visibility = View.GONE
        view?.let {
            it.findViewById<RecyclerView>(R.id.display_similar_user_travels).adapter = SimilarTravelsAdapter(this.context, listUser
                    ?: arrayListOf())

            if (listUser?.size == 0) {
                block_empty_users.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        const val SAVE = "listUser"
        fun newInstance(list: List<User>): SimilarTravelsFragment {
            val fragment = SimilarTravelsFragment()
            fragment.arguments = Bundle()
            fragment.listUser = list
            return fragment
        }
    }
}
package ru.a1024bits.bytheway.ui.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import kotlinx.android.synthetic.main.fragment_search_block.*
import ru.a1024bits.bytheway.adapter.CityAutoCompleteAdapter
import ru.a1024bits.aviaanimation.ui.util.AutoCompleteTextView
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.AviaCity


/**
 * Created by andrey.gusenkov on 29/09/2017.
 */
class SearchFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_search_block, container, false)

        val cityNameTitle = view.findViewById<AutoCompleteTextView>(R.id.textview2)
        cityNameTitle.setThreshold(2)
        cityNameTitle.setAdapter(CityAutoCompleteAdapter(activity))
        cityNameTitle.setOnItemClickListener({ adapterView, view, position, id ->
            val aviaCity = adapterView.getItemAtPosition(position) as AviaCity
            cityNameTitle.setText(aviaCity.name)
        })

        val buttonSearch = view.findViewById<Button>(R.id.buttonSearch)
        return view
    }
}
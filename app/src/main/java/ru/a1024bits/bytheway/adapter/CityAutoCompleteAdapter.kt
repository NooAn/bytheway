package ru.a1024bits.bytheway.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filterable
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.a1024bits.aviaanimation.ui.RepositoryAviaCity
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.AviaCity
import java.util.function.Consumer


/**
 * Created by x220 on 30.09.2017.
 */

class CityAutoCompleteAdapter(private val mContext: Context) : BaseAdapter(), Filterable {
    private var resultList: List<AviaCity> = ArrayList<AviaCity>()

    override fun getCount(): Int = resultList.size

    override fun getItem(index: Int): AviaCity = resultList[index]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, view: View?, parent: ViewGroup): View? {
        var convertView = view
        if (convertView == null) {
            val inflater = mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.simple_dropdown_item_2line, parent, false)
        }
        convertView?.findViewById<TextView>(R.id.text1)?.setText(getItem(position).cityName)
        convertView?.findViewById<TextView>(R.id.text2)?.setText(getItem(position).airportName)
        return convertView
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null || constraint.toString().isNotEmpty()) {
                    val result = repository
                            .getCities(constraint.toString())
                            .blockingSingle()

                    filterResults.values = result
                    filterResults.count = result.size
                    val av = result.get(0)
                    Log.e("LOG", (result + " " + result.size).toString());

                } else {
                    filterResults.values = arrayListOf(AviaCity())
                    filterResults.count = 1
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    resultList = results.values as List<AviaCity>
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    /**
     * Returns a search result for the given name city.
     */
    val repository: RepositoryAviaCity = RepositoryAviaCity();


    companion object {
        private val MAX_RESULTS = 10
    }
}
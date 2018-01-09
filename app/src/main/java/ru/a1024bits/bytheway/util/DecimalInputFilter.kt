package ru.a1024bits.bytheway.util

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern


/**
 * Created by tikhon.osipov on 25.11.17.
 */
class DecimalInputFilter : InputFilter {
    companion object {
        val regex = "^([1-9][0-9]{0,12})\$"
    }

    private val pattern: Pattern by lazy { Pattern.compile(regex) }

    override fun filter(source: CharSequence?, start: Int, end: Int,
                        dest: Spanned?, dstart: Int, dend: Int): CharSequence? {

        if(source == null || dest == null) return null
        val replacement = source.subSequence(start, end).toString()
        val newVal = (dest.subSequence(0, dstart).toString() + replacement
                + dest.subSequence(dend, dest.length).toString())
        val matcher = pattern.matcher(newVal)
        if (matcher.matches())
            return null

        return ""
    }
}
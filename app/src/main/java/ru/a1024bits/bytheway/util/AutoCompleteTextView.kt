package ru.a1024bits.aviaanimation.ui.util

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.AutoCompleteTextView


/**
 * Created by x220 on 30.09.2017.
 */
class AutoCompleteTextView(context: Context, attrs: AttributeSet) : AutoCompleteTextView(context, attrs) {

    private var mAutoCompleteDelay = DEFAULT_AUTOCOMPLETE_DELAY
    private var mLoadingIndicator: ProgressBar? = null

    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super@AutoCompleteTextView.performFiltering(msg.obj as CharSequence, msg.arg1)
        }
    }

    fun setLoadingIndicator(progressBar: ProgressBar) {
        mLoadingIndicator = progressBar
    }

    fun setAutoCompleteDelay(autoCompleteDelay: Int) {
        mAutoCompleteDelay = autoCompleteDelay
    }

    override fun performFiltering(text: CharSequence, keyCode: Int) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator!!.visibility = View.VISIBLE
        }
        mHandler.removeMessages(MESSAGE_TEXT_CHANGED)
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_TEXT_CHANGED, text), mAutoCompleteDelay.toLong())
    }

    override fun onFilterComplete(count: Int) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator!!.visibility = View.GONE
        }
        super.onFilterComplete(count)
    }

    companion object {
        private val MESSAGE_TEXT_CHANGED = 100
        private val DEFAULT_AUTOCOMPLETE_DELAY = 750
    }
}
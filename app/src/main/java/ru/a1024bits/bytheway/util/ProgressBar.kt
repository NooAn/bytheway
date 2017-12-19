package ru.a1024bits.bytheway.util

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewStub
import android.widget.ProgressBar
import ru.a1024bits.bytheway.R


class ProgressBar {
    var stub: View? = null

    private object Holder {
        val INSTANCE = ProgressBar()
    }

    companion object {
        fun init(act: Activity): ru.a1024bits.bytheway.util.ProgressBar {
            Holder.INSTANCE.start(act)
            return Holder.INSTANCE
        }
    }

    fun start(act: Activity): ru.a1024bits.bytheway.util.ProgressBar {
        if (stub == null) {
            Log.e("STUB", "INITED")
            stub = act.findViewById<ViewStub>(R.id.vsHeader).inflate()
            stub?.findViewById<ProgressBar>(R.id.inflateProgressbar)?.isIndeterminate = true
        } else {
            stub?.visibility = View.VISIBLE
        }
        return this
    }

    fun stop() {
        stub?.visibility = View.INVISIBLE
    }
}
package ru.a1024bits.bytheway.util

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import android.widget.LinearLayout

import ru.a1024bits.bytheway.R

import java.util.ArrayList
import android.view.LayoutInflater
import android.widget.ImageView
import android.util.TypedValue
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.AlphaAnimation

class ProgressCustom : ViewGroup {
    private val mData = ArrayList<ImageView?>()

    private var mLayoutWidth = 78
    private var mLayoutHeight = 10

    private var mTotalW = 0
    private var mTotalH = 0

    private var mBoundsHelper = RectF()

    private var mHideBg = 0 //optional

    private var mLinear: LinearLayout? = null
    private var mImageLinear: LinearLayout? = null
    private var mLoadTitle: ImageView? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mTotalW = View.MeasureSpec.getSize(widthMeasureSpec)
        mTotalH = View.MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(mTotalW, mTotalH)

    }

    private fun init() {
        val v = LayoutInflater.from(context).inflate(R.layout.splash_animation, this, false)
        mLinear = v.findViewById(R.id.splash_anim_container) as LinearLayout
        addView(mLinear)

        mLoadTitle = v.findViewById(R.id.load_title) as ImageView
        mImageLinear = mLinear?.findViewById<LinearLayout>(R.id.im_container)
        mData.add(mLinear?.findViewById<ImageView>(R.id.start_indicator_image_1))
        mData.add(mLinear?.findViewById<ImageView>(R.id.start_indicator_image_2))
        mData.add(mLinear?.findViewById<ImageView>(R.id.start_indicator_image_3))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val r = resources
        val dpWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mLayoutWidth.toFloat(), r.displayMetrics)
        val dpHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mLayoutHeight.toFloat(), r.displayMetrics)
        val centerHorizont = mTotalW / 2
        val centerVertical = mTotalH / 2

        var left = centerHorizont
        var top = centerVertical
        left -= dpWidth.toInt()
        top -= dpHeight.toInt()

        mBoundsHelper = RectF(0.0F, 0.0F, dpWidth, dpHeight)
        mBoundsHelper.offsetTo(left.toFloat(), top.toFloat())

        mLinear!!.layout(0, 0, mTotalW, mTotalH)
        mImageLinear!!.layout(50, 0, mTotalW, mTotalH)

//        val Tw = mLoadTitle?.width!!.toInt()
        val Tw = 276 // hardcore width!

        mLoadTitle!!.layout(mBoundsHelper.left.toInt() + 50, mBoundsHelper.top.toInt() - 200,
                centerHorizont + (Tw / 2), mBoundsHelper.bottom.toInt())

        var iconOffset = 50
        for (i in 0 until mData.size) {
            mData.get(i)!!.layout(mBoundsHelper.left.toInt() - 50,
                    mBoundsHelper.top.toInt(),
                    mBoundsHelper.right.toInt() + iconOffset,
                    mBoundsHelper.bottom.toInt())
            iconOffset += 100
        }
    }

    fun startAnimation() {
        var delay = 0L

        for (it in mData) {
            delay += 350L
            it?.visibility = View.VISIBLE
            val animation = AlphaAnimation(1f, 0f)
            animation.duration = 300
            animation.interpolator = LinearInterpolator()
            animation.repeatCount = Animation.INFINITE
            animation.repeatMode = Animation.REVERSE
            animation.startOffset = delay
            it?.startAnimation(animation)
        }
    }

    fun show() {
        if (mHideBg == 0) setBackgroundColor(resources.getColor(R.color.white))
        visibility = VISIBLE
        startAnimation()
    }

    fun hide() {
        layout(0, 0, 0, 0)
        visibility = GONE
    }
}
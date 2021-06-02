package com.bu.selfstudy.tool.dropdowntextview

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.bu.selfstudy.R


/**
 * 最初contentTextView的text已設置好了, 使用MeasureSpec去測量它的高之後,
 * 就可從contentTextView.measuredHeight取得高度, 接著設置動畫, 根據設計,
 * 無內容時不擴展, 有內容時擴展, 但超過三行並不繼續擴展, 而是在第四行採用模糊設計
 *
 */
class DropdownTextView(context: Context, attrs: AttributeSet): LinearLayout(context, attrs){
    private lateinit var panelView: View
    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var arrowImg: View


    private var titleText: String? = null
    private var contentText: String? = null
    private var expandDuration: Int = -1
    private var isExpanded: Boolean = false
    private var isExpandedByUser: Boolean = false

    init {
        context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.DropdownTextView,
                0, 0).apply {

            try {
                titleText = getString(R.styleable.DropdownTextView_titleText)
                contentText = getString(R.styleable.DropdownTextView_contentText)
                isExpanded = getBoolean(R.styleable.DropdownTextView_isExpanded, false)
                isExpandedByUser = isExpanded
                expandDuration = getInteger(R.styleable.DropdownTextView_expandDuration, 300)
            } finally {
                recycle()
            }
        }

        View.inflate(context, R.layout.dropdown_text_view, this)

        panelView = findViewById(R.id.panelView)
        titleTextView = findViewById(R.id.titleTextView)
        contentTextView = findViewById(R.id.contentTextView)
        arrowImg = findViewById(R.id.arrowImg)

        panelView.setOnClickListener {
            if (isExpanded) {
                isExpandedByUser = false
                collapse(true)
            } else {
                isExpandedByUser = true
                expand(true)
            }
        }

        arrowImg.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
        titleTextView.text = titleText
        contentTextView.text = contentText



        post {
            if (isExpanded) {
                expandInternal(false)
            } else {
                collapseInternal(false)
            }
            setArrowViewState(isExpanded, false)
        }

    }



    fun expand(animate: Boolean) {
        if (isExpanded) {
            return
        }

        expandInternal(animate)
    }

    fun collapse(animate: Boolean) {
        if (!isExpanded) {
            return
        }

        collapseInternal(animate)
    }

    private fun expandInternal(animate: Boolean) {
        setHeightToContentHeight(animate)
        setArrowViewState(true, animate)
        isExpanded = true
    }

    private fun collapseInternal(animate: Boolean) {
        setHeightToZero(animate)
        setArrowViewState(false, animate)
        isExpanded = false
    }

    private fun setArrowViewState(expand: Boolean, animate: Boolean) {
        val angle = if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            if (expand) 90.0f else 180.0f
        } else {
            if (expand) 90.0f else 0.0f
        }

        arrowImg.animate()
                .rotation(angle)
                .setDuration((if (animate) expandDuration else 0).toLong())
                .start()
    }

    private fun setHeightToZero(animate: Boolean) {
        val targetHeight = panelView.height
        if (animate) {
            animate(this, height, targetHeight, expandDuration)
        } else {
            setContentHeight(targetHeight)
        }
    }

    private fun setHeightToContentHeight(animate: Boolean) {
        measureContentTextView()
        val targetHeight = panelView.height + contentTextView.measuredHeight
        if (animate) {
            animate(this, height, targetHeight, expandDuration)
        } else {
            setContentHeight(targetHeight)
        }
    }

    private fun setContentHeight(height: Int) {
        layoutParams.height = height
        requestLayout()
    }


    private fun animate(view: View, from: Int, to: Int, duration: Int) {
        changeValue(from, to, duration.toLong()) {
            view.layoutParams.height = it
            view.requestLayout()
            invalidate()
        }
    }

    private fun measureContentTextView() {
        val widthMS = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST)
        val heightMS = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        contentTextView.measure(widthMS, heightMS)
    }



    fun setTitleText(text: String?) {
        titleTextView.text = if(text.isNullOrBlank()) "" else text
    }

    fun setContentText(text: String?) {
        contentTextView.text = if(text.isNullOrBlank()) "" else text
        if(isExpanded){
            setHeightToContentHeight(false)
        }
    }

    fun getTitleText() = titleTextView.text

    fun getContent() = contentTextView.text

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putBoolean("expanded", this.isExpanded)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState: Parcelable? = null
        if (state is Bundle) {
            isExpanded = state.getBoolean("expanded")
            superState = state.getParcelable("superState")
        }

        super.onRestoreInstanceState(superState)
    }

}
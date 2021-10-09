package com.bu.selfstudy.tool.dropdowntextview

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.bu.selfstudy.R
import com.bu.selfstudy.tool.log
import kotlin.math.exp

class MyDropdownTextView(context:Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    private lateinit var panelView: View
    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var arrowImg: View

    private var titleText:String = ""
    private var contentText:String = ""
    private var isExpanded:Boolean = true
    private var expandDuration:Int = -1

    private var selectionTextCallback: SelectionTextCallback? = null

    init {
        context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.DropdownTextView,
                0,
                0
        ).apply {
            try {
                titleText = getString(R.styleable.DropdownTextView_titleText)?:""
                contentText = getString(R.styleable.DropdownTextView_contentText)?:""
                isExpanded = getBoolean(R.styleable.DropdownTextView_isExpanded, true)
                expandDuration = getInt(R.styleable.DropdownTextView_expandDuration, 400)
            }finally {
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
                collapse(true)
            } else {
                expand(true)
            }
        }
        arrowImg.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
        titleTextView.text = titleText
        contentTextView.text = contentText
        contentTextView.customSelectionActionModeCallback = object : ActionMode.Callback {

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                mode?.menu?.removeItem(android.R.id.shareText)
                mode?.menuInflater?.inflate(R.menu.selection_copy_paste, menu)
                return true//false則不彈窗
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                when(item?.itemId){
                    R.id.search->{
                        selectionTextCallback?.onSelectionTextChanged(getSelectionText())
                    }
                }
                return false//返回true则系统的"复制"、"搜索"之类的item将无效，只有自定义item有响应
            }

            override fun onDestroyActionMode(mode: ActionMode?) {}
        }

        post {
            if(!isExpanded){
                collapseInternal(false)
            }
            setArrowViewState(isExpanded, false)
        }
    }

    fun expand(animate: Boolean){
        if(isExpanded)
            return

        expandInternal(animate)
    }

    fun collapse(animate: Boolean){
        if(!isExpanded)
            return

        collapseInternal(animate)
    }

    private fun expandInternal(animate: Boolean){
        setHeightToContentHeight(animate)
        setArrowViewState(true, animate)
        isExpanded = true
    }

    private fun collapseInternal(animate: Boolean){
        setHeightToZero(animate)
        setArrowViewState(false, animate)
        isExpanded = false
    }

    private fun setHeightToZero(animate: Boolean){
        val targetHeight = panelView.height //title height
        if(animate){
            animate(this, height, targetHeight, expandDuration)
        }else{
            setHeight(targetHeight)
        }
    }

    private fun setHeightToContentHeight(animate: Boolean){
        measureContentTextView()
        val targetHeight = panelView.height + contentTextView.measuredHeight
        if (animate) {
            animate(this, height, targetHeight, expandDuration)
        } else {
            setHeight(targetHeight)
        }
    }

    private fun setHeight(height: Int){
        layoutParams.height = height
        requestLayout()
    }

    private fun animate(view: View, from: Int, to: Int, duration: Int) {
        val valuesHolder = PropertyValuesHolder.ofInt("height" ,from, to)

        ValueAnimator.ofPropertyValuesHolder(valuesHolder).apply {
            setDuration(duration.toLong())
            addUpdateListener {
                view.layoutParams.height = getAnimatedValue("height") as Int
                view.requestLayout()
                invalidate()
            }
            start()
        }


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


    private fun setArrowViewState(expand: Boolean, animate: Boolean){
        val angle = if(resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL){
            if(expand) 90.0f else 180.0f
        }else{
            if(expand) 90.0f else 0.0f
        }

        arrowImg.animate()
                .rotation(angle)
                .setDuration(if(animate) expandDuration.toLong() else 0L)
                .start()
    }

    fun setContentText(text: String?){
        contentTextView.text = text
    }

    fun setSelectionTextCallback(callback: SelectionTextCallback){
        selectionTextCallback = callback
    }


    fun getSelectionText(): String?{
        with(contentTextView){
            return text.subSequence(selectionStart, selectionEnd).toString()
        }
    }

}
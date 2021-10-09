package com.bu.selfstudy.tool.speeddialbutton

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.bu.selfstudy.R
import com.bu.selfstudy.tool.showToast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView

/**
 * usage:
 * 1. 在xml中加入此元件
 * 2. 呼叫createChildButtonAndText()即可
 */
class SpeedDialButton(
        context: Context,
        attributeSet: AttributeSet
): ConstraintLayout(context, attributeSet){

    lateinit var overlay: RelativeLayout

    lateinit var mainButton: FloatingActionButton
    lateinit var mainIconDrawableOpen: Drawable
    lateinit var mainIconDrawableClose: Drawable

    var mainButtonIsOpen: Boolean = false

    val childButtonList = mutableListOf<FloatingActionButton>()
    val childTextViewList = mutableListOf<MaterialTextView>()

    init {
        context.theme.obtainStyledAttributes(
                attributeSet,
                R.styleable.SpeedDialButton,
                0,
                0
        ).apply {
            try {
                mainIconDrawableOpen = getDrawable(R.styleable.SpeedDialButton_mainIcon)!!
            }finally {
                recycle()
            }
        }

        elevation = dpToPixel(context, 6).toFloat()

        context.getDrawable(R.drawable.ic_baseline_close_24)!!.let{ originalDrawable->
            mainIconDrawableClose = object: LayerDrawable(arrayOf(originalDrawable)){
                @Override
                override fun draw(canvas: Canvas) {
                    canvas.save()
                    canvas.rotate(-45f, originalDrawable.intrinsicWidth / 2f, originalDrawable.intrinsicHeight / 2f)
                    super.draw(canvas)
                    canvas.restore()
                }
            }
        }
        mainButton = createMainButton()
    }

    private fun createFrameLayout() = RelativeLayout(context).apply {
        id = R.id.overlay
        elevation = dpToPixel(context, 5).toFloat()
        isVisible = false
        layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )

        TypedValue().also {
            context.theme.resolveAttribute(R.attr.overlay_color, it, true)
            setBackgroundColor(it.data)
        }

        setOnClickListener {
            toggleChange(true)
        }

        (this@SpeedDialButton.parent as ViewGroup).addView(this)
    }

    private fun createMainButton() = FloatingActionButton(context).apply {
        id = R.id.main_button
        layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ).also {
            it.setMargins(16 * context.resources.displayMetrics.density.toInt())
            it.endToEnd = this@SpeedDialButton.id
            it.bottomToBottom = this@SpeedDialButton.id
        }

        setImageDrawable(mainIconDrawableOpen)

        setOnClickListener {
            toggleChange(true)
        }

        addView(this)
    }

    fun toggleChange(animate: Boolean){
        mainButtonIsOpen = !mainButtonIsOpen
        updateMainButtonDrawable(mainButtonIsOpen, animate)
        updateChildButtons(mainButtonIsOpen, animate)
        updateOverlay(mainButtonIsOpen, animate)
    }

    private fun updateOverlay(isOpen: Boolean, animate: Boolean){
        if(!animate){
            overlay.isVisible = isOpen
            return
        }

        if(isOpen){
            overlay.animate()
                    .setDuration(200L)
                    .alpha(1f)
                    .withStartAction{
                        overlay.isVisible = true
                        overlay.alpha = 0f
                    }
                    .start()
        }else{
            overlay.animate()
                    .setDuration(200L)
                    .alpha(0f)
                    .withEndAction { overlay.isVisible = false }
                    .start()
        }
    }

    private fun updateMainButtonDrawable(isOpen: Boolean, animate: Boolean){
        if(!animate){
            if(isOpen){
                mainButton.setImageDrawable(mainIconDrawableClose)
                mainButton.rotation = 45f
            }else{
                mainButton.setImageDrawable(mainIconDrawableOpen)
                mainButton.rotation = 0f
            }
            return
        }

        if(isOpen){
            mainButton.animate()
                .rotation(45f)
                .setDuration(200L)
                .setInterpolator(FastOutSlowInInterpolator())
                .withEndAction{

                }
                .start()
            mainButton.setImageDrawable(mainIconDrawableClose)
        }else{
            mainButton.animate()
                .rotation(0f)
                .setDuration(200L)
                .setInterpolator(FastOutSlowInInterpolator())
                .withEndAction{
                }
                .start()
            mainButton.setImageDrawable(mainIconDrawableOpen)

        }
    }

    private fun updateChildButtons(isOpen: Boolean, animate: Boolean){
        if(!animate){
            childTextViewList.forEach { it.isVisible = isOpen }
            childButtonList.forEach { it.isVisible = isOpen }
            return
        }

        if(isOpen){
            childTextViewList.forEachIndexed { index, view->
                AnimationUtils.loadAnimation(view.context, R.anim.speed_child_show).let { anim->
                    anim.startOffset = index * 25L
                    view.startAnimation(anim)
                }

                view.isVisible = true
            }
            childButtonList.forEachIndexed{ index, view ->
                AnimationUtils.loadAnimation(view.context, R.anim.speed_child_show).let { anim->
                    anim.startOffset = index * 25L
                    view.startAnimation(anim)
                }

                view.isVisible = true
            }
        }else{
            childTextViewList.forEachIndexed { index, view->
                AnimationUtils.loadAnimation(view.context, R.anim.speed_child_hide).let { anim->
                    anim.setAnimationListener(object:Animation.AnimationListener{
                        override fun onAnimationStart(animation: Animation?){}
                        override fun onAnimationEnd(animation: Animation?){ view.isVisible = false }
                        override fun onAnimationRepeat(animation: Animation?){}
                    })
                    anim.startOffset = (childTextViewList.size-1-index) * 50L
                    view.startAnimation(anim)
                }
            }
            childButtonList.forEachIndexed{ index, view ->
                AnimationUtils.loadAnimation(view.context, R.anim.speed_child_hide).let { anim->
                    anim.setAnimationListener(object:Animation.AnimationListener{
                        override fun onAnimationStart(animation: Animation?){}
                        override fun onAnimationEnd(animation: Animation?){ view.isVisible = false }
                        override fun onAnimationRepeat(animation: Animation?){}
                    })
                    anim.startOffset = (childTextViewList.size-1-index) * 50L
                    view.startAnimation(anim)
                }
            }
        }

    }


    /**
     * Normal Fab: 56dp, 中心點距離右方螢幕邊緣56/2+16 = 44dp
     * Mini Fab: 48dp, 中心點距離右方螢幕邊緣48/2+8 = 32dp
     * 補足12dp
     */
    fun createChildButtonAndText(
            @IdRes buttonIdRes: Int,
            @DrawableRes buttonIconRes: Int,
            text: String,
            clickEvent: (FloatingActionButton, MaterialTextView)->Unit
    ){
        if(childButtonList.any { it.id == buttonIdRes })
            return

        val button = FloatingActionButton(context).apply {
            id = buttonIdRes
            isVisible = false
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
                var siblingViewId = 0
                var siblingMargin = 0

                if(childButtonList.isEmpty()){
                    siblingViewId = mainButton.id
                    siblingMargin = 16
                }else{
                    siblingViewId =  childButtonList.last().id
                    siblingMargin = 8
                }


                dpToPixel(context, siblingMargin).let { marginPixel->
                    it.setMargins(marginPixel, marginPixel, marginPixel, marginPixel)
                }

                it.startToStart = siblingViewId
                it.endToEnd = siblingViewId
                it.bottomToTop = siblingViewId
            }

            size = FloatingActionButton.SIZE_MINI
            setImageDrawable(context.getDrawable(buttonIconRes))
        }

        val textView = MaterialTextView(context).apply {
            isVisible = false
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
                dpToPixel(context, 32).let { marginPixel->
                    it.setMargins(marginPixel, marginPixel, marginPixel, marginPixel)
                }
                it.topToTop = button.id
                it.bottomToBottom = button.id
                it.endToStart = button.id
            }
            val verticalPadding = dpToPixel(context, 4)
            val HorizontalPadding = dpToPixel(context, 8)
            setPadding(HorizontalPadding, verticalPadding, HorizontalPadding, verticalPadding)

            setTextAppearance(context, R.style.TextAppearance_MaterialComponents_Subtitle2)
            setTextColor(context.resources.getColor(R.color.black60))
            setText(text)
            setTypeface(null, Typeface.BOLD);
            setBackground(context.resources.getDrawable(R.drawable.item_background_label))
        }

        addView(button)
        addView(textView)
        childTextViewList.add(textView)
        childButtonList.add(button)

        button.setOnClickListener {
            toggleChange(false)
            clickEvent(button, textView)
        }

        textView.setOnClickListener {
            toggleChange(false)
            clickEvent(button, textView)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!::overlay.isInitialized) {
            overlay = createFrameLayout()
        }
    }

    fun changeIconAndText(
            @IdRes buttonIdRes: Int,
            @DrawableRes buttonIconRes: Int,
            text: String
    ){
        childButtonList.indexOfFirst { it.id == buttonIdRes }?.let { index->
            if(index == -1)
                return@let

            childButtonList[index].setImageDrawable(context.getDrawable(buttonIconRes))
            childTextViewList[index].setText(text)
        }
    }
}
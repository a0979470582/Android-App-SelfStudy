package com.bu.selfstudy.ui.word

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager


/**
 * RecyclerView內部的Bug, 啟用項目預覽動畫時有機率錯誤
 */
class LinearLayoutManagerWrapper : LinearLayoutManager {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, orientation: Int, reverseLayout: Boolean) :
            super(context, orientation, reverseLayout)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    override fun supportsPredictiveItemAnimations() = false
}
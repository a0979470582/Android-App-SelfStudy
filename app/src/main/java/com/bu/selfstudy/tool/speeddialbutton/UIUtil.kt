package com.bu.selfstudy.tool.speeddialbutton

import android.content.Context

fun dpToPixel(context: Context, dp: Int) =
        dp * context.resources.displayMetrics.density.toInt()

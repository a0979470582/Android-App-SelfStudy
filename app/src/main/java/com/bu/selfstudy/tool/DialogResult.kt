package com.bu.selfstudy.tool

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import kotlinx.android.parcel.Parcelize

@Parcelize
class DialogResultEvent(
        val dialogName: String,
        val bundle: Bundle,
        val isSuccessful: Boolean,
        var isPending: Boolean = true
): Parcelable

fun Fragment.setDialogResult(vararg pairs:Pair<String, Any> ,isSuccessful: Boolean = true){
    val bundle = Bundle().apply {
        for(pair in pairs){
            val key = pair.first
            when(val value = pair.second){
                is Parcelable -> putParcelable(key, value)
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is Int -> putInt(key, value)
            }
        }
    }

    setNavigationResult("dialog-event", DialogResultEvent(
        this.javaClass.name.split(".").last(), bundle, isSuccessful)
    )
}
fun Fragment.getDialogResult(block:(dialogName:String, bundle:Bundle)->Unit){
    getNavigationResultLiveData<DialogResultEvent>("event")?.observe(viewLifecycleOwner){
        it.apply {
            if(isSuccessful && isPending){
                isPending = false
                block(dialogName, bundle)
            }
        }
    }
}
package com.bu.selfstudy.tools

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Parcelize
class DatabaseResultEvent(
        val action: String,
        val message: String,
        val count: Int,
        val destination:String? = null,
        var isPending: Boolean = true
): Parcelable

fun Fragment.getDatabaseResult(
        databaseEventLD: MutableLiveData<DatabaseResultEvent>,
        block:(action:String, message:String, count:Int)->Unit
){
    databaseEventLD.observe(viewLifecycleOwner){
        it.apply {
            if (count > 0 && isPending) {
                isPending = false
                block(action, message, count)
            }
        }
    }
}

fun ViewModel.setDatabaseResult(
        isLoadingLD: MutableLiveData<Boolean>,
        databaseEventLD: MutableLiveData<DatabaseResultEvent>,
        action: String,
        block:suspend ()->Int
){
    isLoadingLD.value = true
    viewModelScope.launch(Dispatchers.IO){
        val count = block()
        databaseEventLD.postValue(
                DatabaseResultEvent(action, "成功$action $count 個單字", count)
        )
    }
}
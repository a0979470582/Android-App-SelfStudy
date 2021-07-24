package com.bu.selfstudy.ui.trash


import android.os.Bundle
import androidx.lifecycle.*
import androidx.paging.Config
import androidx.paging.LivePagedListBuilder
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.DeleteRecord
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.model.WordTuple
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.DeleteRecordRepository
import com.bu.selfstudy.data.repository.WordRepository
import com.bu.selfstudy.tool.SingleLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 更改資料庫中的wordList都會透過LiveData反應在目前的ViewPager, 例如新增,刪除,修改都會同步到資料庫,
 * 接著透過LiveData傳回並在ViewPager看到更新, 而wordFragment的生命週期進入onStop時, 會同步目前使用者所
 * 看到的單字頁(wordId)到資料庫
 */
class TrashViewModel() : ViewModel() {

    val databaseEvent = SingleLiveData<Pair<String, Bundle?>>()

    val deleteRecord = MutableLiveData<List<DeleteRecord>>()
    val deleteRecordBook = DeleteRecordRepository.loadDeleteRecordBook().asLiveData()
    val deleteRecordWord = DeleteRecordRepository.loadDeleteRecordWord().asLiveData()


    fun combineDeleteRecord(){
        val newList = ArrayList<DeleteRecord>()
        deleteRecordBook.value?.let {
            newList.addAll(it)
        }
        deleteRecordWord.value?.let {
            newList.addAll(it)
        }
        deleteRecord.value = newList
    }

    val recordIdList = ArrayList<Long>()
    fun refreshIdList(recordList: List<DeleteRecord>){
        viewModelScope.launch {
            recordIdList.clear()
            recordIdList.addAll(recordList.map { if(it != null) it.id else 0 })
        }
    }

    var longPressedRecordIdList = ArrayList<Long>()
    fun refreshLongPressedRecord(recordIdList: List<Long>){
        viewModelScope.launch {
            longPressedRecordIdList.clear()
            longPressedRecordIdList.addAll(recordIdList)
        }
    }

    fun refreshDeleteRecord(deleteRecord: DeleteRecord) {
        viewModelScope.launch {
            DeleteRecordRepository.refreshDeleteRecord(deleteRecord)
        }
    }

}
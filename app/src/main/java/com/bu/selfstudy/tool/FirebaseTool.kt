package com.bu.selfstudy.tool

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.bu.selfstudy.ui.main.MainActivity
import com.bu.selfstudy.R
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.BackupMetadata
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

fun AppCompatActivity.backupUserData(){

    //先判斷是否引導使用者登入
    if(FirebaseAuth.getInstance().currentUser == null) {
        signInWithFirebase()
        return
    }

    //詢問使用者是否覆蓋上次備份
    if(SelfStudyApplication.backupMetadata.value?.hasBackup == true){
        AlertDialog.Builder(this)
            .setMessage("是否覆蓋上次備份?")
            .setPositiveButton("確認"){ _, _ ->
                backupUserDataInternal()
            }.setNegativeButton("取消"){ _, _ ->
                return@setNegativeButton
            }.setOnCancelListener{
                return@setOnCancelListener
            }
            .show()
    }else{
        backupUserDataInternal()
    }

    //避免使用者有累積的還原檔案未刪除
    lifecycleScope.launch(Dispatchers.IO) {
        clearCacheFile()
    }
}

private fun AppCompatActivity.backupUserDataInternal(){

    //Storage的根目錄
    val storageReference = FirebaseStorage.getInstance().reference

    //使用者uid
    val userUid = FirebaseAuth.getInstance().currentUser?.uid?:""

    //DB檔
    val file1 = Uri.fromFile(getDatabasePath("app_database"))
    val file2 = Uri.fromFile(getDatabasePath("app_database-wal"))
    val file3 = Uri.fromFile(getDatabasePath("app_database-shm"))


    "備份中...請稍後".showToast()
    storageReference
            .child("user_backup_file/${userUid}/${file2.lastPathSegment}")
            .putFile(file2)

    storageReference
            .child("user_backup_file/${userUid}/${file3.lastPathSegment}")
            .putFile(file3)

    storageReference
            .child("user_backup_file/${userUid}/${file1.lastPathSegment}")
            .putFile(file1)
            .addOnFailureListener {
                if(!hasNetwork())
                    "開啟網路進行備份".showToast()
                else
                    "備份未完成".showToast()
            }.addOnSuccessListener { taskSnapshot ->
                "備份成功".showToast()

                val backupTimeString = "上次備份: "+
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                            Date(taskSnapshot.metadata?.creationTimeMillis!!)
                        )

                SelfStudyApplication.backupMetadata.value = BackupMetadata(
                    backupTimeString = backupTimeString,
                    hasBackup = true
                )

            }.addOnProgressListener {
                val number = it.bytesTransferred.toDouble()
                        .div(it.totalByteCount)
                        .times(100)
                        .roundToInt()
                "備份中...${number}%".showToast()
            }


}

fun AppCompatActivity.restoreUserData(){

    //判斷是否引導使用者登入, 理論上進來此方法之前已有先判斷一次
    if(FirebaseAuth.getInstance().currentUser == null) {
        signInWithFirebase()
        return
    }

    //判斷是否存在備份, 理論上無備份也無法點擊還原按鈕
    if(SelfStudyApplication.backupMetadata.value?.hasBackup == true)
        AlertDialog.Builder(this)
            .setMessage("是否還原到上次備份之狀態 ?")
            .setPositiveButton("確認"){ _, _ ->
                restoreUserDataInternal()
            }.setNegativeButton("取消"){ _, _ ->
                return@setNegativeButton
            }.setOnCancelListener{
                return@setOnCancelListener
            }
            .show()
    else
        "無備份".showToast()
}

private fun AppCompatActivity.restoreUserDataInternal(){

    val progressDialog = ProgressDialog(this)

    with(progressDialog){
        setMessage("還原中...請稍後")
        setIndeterminate(false)
        setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        setCanceledOnTouchOutside(false)
        show()//注意dismiss的呼叫者介面是否正確, 才能正確關閉
    }

    val fileCount = MutableLiveData(0)

    val storageReference = FirebaseStorage.getInstance().reference

    val userUid = FirebaseAuth.getInstance().currentUser?.uid?:""

    val cacheFile1 = File.createTempFile("app_database", "")
    val cacheFile2 = File.createTempFile("app_database_wal", "")
    val cacheFile3 = File.createTempFile("app_database_shm", "")


    progressDialog.setMessage("還原檔案下載中...")
    "還原檔案下載中..."
    storageReference.child("user_backup_file/${userUid}/app_database")
            .getFile(cacheFile1)
            .addOnProgressListener {

                val number = it.bytesTransferred.toDouble()
                    .div(it.totalByteCount)
                    .times(90)//為了與底下的任務共同分擔進度, 故只到90
                    .roundToInt()

                progressDialog.setProgress(number)

            }.addOnSuccessListener {
                fileCount.value = fileCount.value!! + 1
            }.addOnFailureListener {
                (progressDialog as Dialog).dismiss()
                if(!hasNetwork())
                    "開啟網路來進行還原".showToast()
                else
                    "還原未完成".showToast()

            }
    storageReference.child("user_backup_file/${userUid}/app_database-wal").getFile(cacheFile2)
            .addOnSuccessListener {
                fileCount.value = fileCount.value!! + 1
            }.addOnFailureListener{
                (progressDialog as Dialog).dismiss()
            }
    storageReference.child("user_backup_file/${userUid}/app_database-shm").getFile(cacheFile3)
            .addOnSuccessListener {
                fileCount.value = fileCount.value!! + 1
            }.addOnFailureListener{
                (progressDialog as Dialog).dismiss()
            }

    fileCount.observe(this){
        if(it == 3){
            progressDialog.setMessage("正在還原中...")
            progressDialog.setProgress(95)
            lifecycleScope.launch(Dispatchers.IO) {
                getDatabasePath("app_database").writeBytes(cacheFile1.readBytes())
                getDatabasePath("app_database-wal").writeBytes(cacheFile2.readBytes())
                getDatabasePath("app_database-shm").writeBytes(cacheFile3.readBytes())

                launch(Dispatchers.Main) {
                    progressDialog.setMessage("還原完成")
                    progressDialog.setProgress(100)
                    (progressDialog as Dialog).dismiss()
                    "還原完成".showToast()
                }

                cacheFile1.delete()
                cacheFile2.delete()
                cacheFile3.delete()
            }
        }
    }
}

/**
 * 取得使用者的備份狀態, 並記錄在Application
 * SelfStudyApplication.backupDateTime的值
 * 使用者未登入 -> 未登入
 * 找到備份檔案 -> 上次備份: 2021....
 * 未找到備份檔案 -> 目前沒有備份
 */
fun AppCompatActivity.updateBackupDatetime(){
    val storageReference = FirebaseStorage.getInstance().reference

    val userUid = FirebaseAuth.getInstance().currentUser?.uid?:""

    if(FirebaseAuth.getInstance().currentUser == null) {
        SelfStudyApplication.backupMetadata.value = BackupMetadata(
            backupTimeString = "未登入",
            hasBackup = false
        )

        return
    }

    storageReference
        .child("user_backup_file/${userUid}/app_database")
        .metadata
        .addOnSuccessListener {
            val epoch = it.creationTimeMillis

            val backupTimeString = "上次備份: "+
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(epoch))

            SelfStudyApplication.backupMetadata.value = BackupMetadata(
                backupTimeString = backupTimeString,
                hasBackup = true
            )

        }.addOnFailureListener{
            SelfStudyApplication.backupMetadata.value = BackupMetadata(
                backupTimeString = "目前無備份",
                hasBackup = false
            )
        }
}

fun AppCompatActivity.clearCacheFile(){
    cacheDir.listFiles().forEach {
        if(it.isFile && it.name.startsWith("app_database"))
            it.delete()
    }
}

fun AppCompatActivity.getSignInIntent(): Intent{
    val authProvider = listOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build()
    )

    val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(authProvider)
            .setAlwaysShowSignInMethodScreen(true)
            .setIsSmartLockEnabled(false)
            .setLogo(R.drawable.ic_astronaut)
            .setTosAndPrivacyPolicyUrls("https://policies.google.com/terms?hl=zh-TW",
                    "https://policies.google.com/privacy?hl=zh-TW")
            .setTheme(R.style.LoginTheme)
            .build()

    return intent
}

fun AppCompatActivity.uploadUserSuggestion(
        userMail: String,
        title: String,
        suggestion: String
){

    "傳送中...".showToast()
    val storageReference = FirebaseStorage.getInstance().reference

    val userUid = FirebaseAuth.getInstance().currentUser?.uid?:""

    val content = "uid: $userUid, userMail: $userMail, title: $title, suggestion: $suggestion"

    storageReference
            .child("user_suggestion/${userMail}-${Date().time}.txt")
            .putBytes(content.toByteArray())
            .addOnSuccessListener {
                "您的意見已傳送".showToast()
            }.addOnFailureListener {
                "傳送未完成".showToast()
            }
}


/**
 * 進行登出
 **/
fun AppCompatActivity.signOutWithFirebase(){
    AuthUI.getInstance().signOut(this).addOnCompleteListener {
        if(it.isSuccessful)
            "登出成功".showToast()
    }
}

/**
 * 發送登入intent
 */
fun AppCompatActivity.signInWithFirebase(){
    startActivityForResult(getSignInIntent(), MainActivity.SIGN_IN)
}

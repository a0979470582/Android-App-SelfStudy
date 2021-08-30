package com.bu.selfstudy.ui.main

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import androidx.preference.PreferenceManager
import com.bu.selfstudy.NavGraphDirections
import com.bu.selfstudy.R
import com.bu.selfstudy.databinding.ActivityMainBinding
import com.bu.selfstudy.databinding.ActivityNavHeaderBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.ui.search.SearchFragment
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso


/**
 * 單一Activity設計
 *
 *
 * 1. Navigation的fragment一定要在xml上才找得到其id
 * 2. 路徑的功用是可設定動畫, 以及讓編譯器檢查程式是否正確
 * 3. 一個global路徑會加入到所有fragment
 * 4. 若無須接收參數, 不必加入路徑
 * 5. global路徑可從NavGraphDirections找到, 無須透過目前的fragment
 *
 * 可以根據目的地id設置某些元件的狀態, 但也可以在fragment的onAttach, onDetach來設置
 *
 * BackStack是一個雙向序列Deque, 因此無法移除位於堆疊中間的fragment
*/
class MainActivity : AppCompatActivity(){
    private val activityViewModel: ActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    lateinit var navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration

    companion object{
        const val SIGN_IN = 0
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val navBinding: ActivityNavHeaderBinding = DataBindingUtil.inflate(
            layoutInflater, R.layout.activity_nav_header, binding.navView, false
        )

        binding.navView.addHeaderView(navBinding.root)


        navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)

        NavigationUI.setupWithNavController(binding.navView, navController)


        //bookList常使用, 生命週期放在Activity
        activityViewModel.bookListLiveData.observe(this){
            activityViewModel.refreshBookIdList(it)
        }


        activityViewModel.databaseEvent.observe(this){ pair->
            when(pair?.first){
                "insertWord" -> pair.second?.let { showInsertMessage(it) }
                "insertBook" -> "新增成功".showToast()
                        //.showOnlyTextSnackbar(
                        //view = binding.root, anchorView = binding.root)
            }
        }

        //監聽登入狀況反應在NavigationView, 並更新使用者備份時間
        FirebaseAuth.getInstance().addAuthStateListener { auth: FirebaseAuth ->
            navBinding.user = auth.currentUser

            updateBackupDatetime()
        }


        //登入按鈕
        navBinding.constraintLayout.setOnClickListener {
            if(FirebaseAuth.getInstance().currentUser == null){
                signInWithFirebase()
            }
        }



        //更多菜單按鈕
        navBinding.moreIcon.setOnClickListener {
            PopupMenu(this, it).let { popupMenu->
                popupMenu.menuInflater.inflate(R.menu.activity_nav_view_more, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                    when (menuItem.itemId) {
                        R.id.action_backup -> {
                            backupUserData()
                        }
                        R.id.action_logout -> {
                            signOutWithFirebase()
                        }
                    }
                    true
                }

                popupMenu.show()
            }
        }
    }


    /**
     * 判斷登入是否成功, 告知使用者
     * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN) {
            if (resultCode != Activity.RESULT_OK) {
                val response = IdpResponse.fromResultIntent(data)
                response?.error?.errorCode?.let {
                    when(it){
                        ErrorCodes.NO_NETWORK -> "開啟網路連接更多內容"
                        ErrorCodes.EMAIL_MISMATCH_ERROR -> "Email 帳號或密碼有誤"
                        ErrorCodes.PROVIDER_ERROR -> "第三方錯誤"
                        ErrorCodes.UNKNOWN_ERROR -> "未知錯誤"
                        else->"$it"
                    }.showToast()
                }
            }else{
                "登入成功".showToast()
            }
        }
    }

    /**
     * 在SearchFragment中使用麥克風進行查詢會在此收到, 收到後返回給SearchFragment
     * 單純的文字查詢SearchFragment會自行處理
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if(intent.action == Intent.ACTION_SEARCH){
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                (supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                        ?.childFragmentManager?.fragments?.first() as SearchFragment).also {
                            it.startSearch(query)
                }
            }
        }
    }

    /**
     * 新增單字後顯示Snackbar, 可點擊檢視跳轉
     */
    private fun showInsertMessage(bundle: Bundle) {
        Snackbar.make(binding.root, "新增成功", Snackbar.LENGTH_LONG).run {
            setAction("檢視") {
                navController.navigate(
                    NavGraphDirections.actionGlobalWordFragment(
                            bookId = bundle.getLong("bookId"),
                            wordId = bundle.getLong("wordId")
                    )
                )
            }
            show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController)||
                super.onOptionsItemSelected(item)
    }

}
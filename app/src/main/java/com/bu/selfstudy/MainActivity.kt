package com.bu.selfstudy

import android.app.SearchManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.bu.selfstudy.databinding.ActivityMainBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.ui.book.BookFragmentDirections
import com.bu.selfstudy.ui.search.SearchFragment
import com.bu.selfstudy.ui.wordcard.WordCardFragmentDirections
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable

/**
 * 單一Activity設計
 */
class MainActivity : AppCompatActivity(){
    private val activityViewModel: ActivityViewModel by viewModels()
    private val binding : ActivityMainBinding by viewBinding()
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        /**
         * jetpack navigation
         */
        navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)

        setSupportActionBar(binding.toolbar)

        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.navView.setupWithNavController(navController)


        //Member data for DrawerLayout
        activityViewModel.memberLiveData.observe(this){
            if(it != null){
                with(binding.navView){
                    findViewById<TextView>(R.id.mailField)?.text = it.email
                    findViewById<TextView>(R.id.userNameField)?.text = it.userName
                }
            }
        }

        //bookList常使用, 範圍放在Activity
        activityViewModel.bookListLiveData.observe(this){
            activityViewModel.refreshBookIdList(it)
        }


        activityViewModel.databaseEvent.observe(this){ pair->
            when(pair?.first){
                "insertWord" -> {
                    pair.second?.let { showInsertMessage(it) }
                }
                "insertBook" -> "新增成功".showToast()

            }
        }


        /**
         * 1. Navigation的fragment一定要在xml上才找得到其id
         * 2. 路徑的功用是可設定動畫, 以及讓編譯器檢查程式是否正確
         * 3. 一個global路徑會加入到所有fragment
         * 4. 若無須接收參數, 不必加入路徑
         * 5. global路徑可從NavGraphDirections找到, 無須透過目前的類
         *
         * 在這裡可以根據目的地id設置某些元件的狀態, 但也可以在fragment的
         * onAttach, onDetach來設置, 例如SearchFragment中就有動態修改Toolbar的滾動事件
         */
        navController.addOnDestinationChangedListener { _, destination, _ ->
            //單字卡頁關閉DrawerLayout, 避免滑動衝突
            binding.drawerLayout.setDrawerLockMode(
                    if (destination.id == R.id.wordCardFragment)
                        DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                    else
                        DrawerLayout.LOCK_MODE_UNLOCKED
            )

            //特定頁才會顯示FABs
            binding.speedDialView.isVisible =
                    (destination.id == R.id.bookFragment) ||
                    (destination.id == R.id.wordCardFragment) ||
                    (destination.id == R.id.archiveFragment) ||
                    (destination.id == R.id.recentWordFragment) ||
                    (destination.id == R.id.markFragment)

            //TODO
            //特定頁面的Toolbar修改顏色(< 1 ms)
            if(destination.id == R.id.wordCardFragment || destination.id == R.id.wordListFragment){
                binding.toolbar.setTitleTextColor(Color.WHITE)
                binding.appBarLayout.background = resources.getDrawable(R.color.blue80)
                binding.toolbar.background = getDrawable(R.drawable.toolbar_background_color)
                binding.toolbar.overflowIcon?.setTint(Color.WHITE)
                binding.toolbar.navigationIcon?.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC)
                window.statusBarColor = resources.getColor(R.color.blue80)

                val lp = AppBarLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                binding.toolbar.layoutParams = lp

                val lp2 = CoordinatorLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                binding.appBarLayout.layoutParams = lp2

            }else{
                testTaskTime {
                    val gary = resources.getColor(R.color.icon_black)
                    binding.toolbar.setTitleTextColor(gary)
                    binding.appBarLayout.background = resources.getDrawable(android.R.color.transparent)
                    binding.toolbar.background = getDrawable(R.drawable.toolbar_background_corner)
                    binding.toolbar.overflowIcon?.setTint(gary)
                    binding.toolbar.navigationIcon?.colorFilter = PorterDuffColorFilter(gary, PorterDuff.Mode.SRC)
                    window.statusBarColor = gary


                    val lp2 = CoordinatorLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    lp2.setMargins(36,24,36,0)
                    binding.appBarLayout.layoutParams = lp2

                    val lp = AppBarLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    lp.setMargins(12)

                    binding.toolbar.layoutParams = lp
                }


            }
        }

        //TODO
        binding.toolbar.setOnClickListener {
            navController.navigate(R.id.searchFragment)
        }
    }

    /**
     * 在SearchFragment中使用麥克風進行查詢會在此收到,
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
        binding.root.showSnackbar("新增成功", "檢視"){
            navController.navigate(
                    NavGraphDirections.actionGlobalWordCardFragment(
                            bookId = bundle.getLong("bookId"),
                            wordId = bundle.getLong("wordId")
                    )
            )
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
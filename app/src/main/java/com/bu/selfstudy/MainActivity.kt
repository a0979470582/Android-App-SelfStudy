package com.bu.selfstudy

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import com.bu.selfstudy.databinding.ActivityMainBinding
import com.bu.selfstudy.databinding.ActivityNavHeaderBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.ui.search.SearchFragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso


/**
 * 單一Activity設計
 */
class MainActivity : AppCompatActivity(){
    private val activityViewModel: ActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    lateinit var navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration

    val SIGN_IN = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val _binding: ActivityNavHeaderBinding = DataBindingUtil.inflate(
                layoutInflater, R.layout.activity_nav_header, binding.navView, false)
        binding.navView.addHeaderView(_binding.root)


        navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)

        NavigationUI.setupWithNavController(binding.navView, navController)


        //bookList常使用, 範圍放在Activity
        activityViewModel.bookListLiveData.observe(this){
            activityViewModel.refreshBookIdList(it)
        }


        activityViewModel.databaseEvent.observe(this){ pair->
            when(pair?.first){
                "insertWord" -> pair.second?.let { showInsertMessage(it) }
                "insertBook" -> "新增成功".showToast()
            }
        }
/*


        /**
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
        navController.addOnDestinationChangedListener { _, destination, _ ->

            /*特定頁才會顯示FABs
            binding.speedDialView.isVisible =
                    (destination.id == R.id.bookFragment) ||
                    (destination.id == R.id.wordFragment)||
                    (destination.id == R.id.archiveFragment) ||
                    (destination.id == R.id.recentWordFragment) ||
                    (destination.id == R.id.markFragment)*/


            //特定頁面的Toolbar修改顏色(< 1 ms)
            /*
            when(destination.id){
                R.id.wordFragment->{
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

                }
                R.id.themeFragment, R.id.settingFragment, R.id.suggestFragment->{
                    val gary = resources.getColor(R.color.icon_black)
                    binding.toolbar.setTitleTextColor(gary)
                    binding.appBarLayout.background = resources.getDrawable(android.R.color.transparent)
                    binding.toolbar.background = getDrawable(R.drawable.toolbar_background_corner)
                    binding.toolbar.overflowIcon?.setTint(gary)
                    binding.toolbar.navigationIcon?.colorFilter = PorterDuffColorFilter(gary, PorterDuff.Mode.SRC)
                    window.statusBarColor = gary


                    val lp = AppBarLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    binding.toolbar.layoutParams = lp

                    val lp2 = CoordinatorLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    binding.appBarLayout.layoutParams = lp2
                }
                else->{
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
                    lp2.setMargins(36, 24, 36, 0)
                    binding.appBarLayout.layoutParams = lp2

                    val lp = AppBarLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    lp.setMargins(12)

                    binding.toolbar.layoutParams = lp

                }
            }*/
        }
*/

        val authProvider = listOf(
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.FacebookBuilder().build(),
                AuthUI.IdpConfig.EmailBuilder().build()
        )

        val authListener: FirebaseAuth.AuthStateListener =
                FirebaseAuth.AuthStateListener { auth: FirebaseAuth ->
                    val user: FirebaseUser? = auth.currentUser
                    if (user == null) {
                        _binding.mailField.text = ""
                        _binding.userNameField.text = "點擊登入"
                        _binding.iconImage.setImageResource(R.drawable.ic_shooting_star)
                    } else {
                        _binding.mailField.text = user.email
                        _binding.userNameField.text = user.displayName
                        Picasso.get()
                                .load(user.photoUrl)
                                .error(R.drawable.app_icon)
                                .into(_binding.iconImage)
                    }
                }


        FirebaseAuth.getInstance().addAuthStateListener(authListener)

        _binding.linearLayout.setOnClickListener {
            if(FirebaseAuth.getInstance().currentUser == null){
                val intent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(authProvider)
                        .setAlwaysShowSignInMethodScreen(true)
                        .setIsSmartLockEnabled(false)
                        .setLogo(R.drawable.app_icon)
                        .setTosAndPrivacyPolicyUrls("https://policies.google.com/terms?hl=zh-TW",
                                "https://policies.google.com/privacy?hl=zh-TW")
                        .setTheme(R.style.LoginTheme)
                        .build()
                startActivityForResult(intent, SIGN_IN)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN) {
            if (resultCode != Activity.RESULT_OK) {
                val response = IdpResponse.fromResultIntent(data)
                Toast.makeText(applicationContext, response?.error?.errorCode.toString(), Toast.LENGTH_SHORT).show()
            }
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
        Snackbar.make(binding.root, "新增成功", Snackbar.LENGTH_LONG).run {
            setAction("檢視") {
                navController.navigate(
                        NavGraphDirections.actionGlobalWordFragment(
                                bookId = bundle.getLong("bookId"),
                                wordId = bundle.getLong("wordId")
                        )
                )
            }
            //setAnchorView(binding.speedDialView)
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
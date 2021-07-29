package com.bu.selfstudy

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.bu.selfstudy.databinding.ActivityMainBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.ui.book.BookFragmentDirections
import com.bu.selfstudy.ui.search.SearchFragment
import com.bu.selfstudy.ui.wordcard.WordCardFragmentDirections


class MainActivity : AppCompatActivity(){
    private val activityViewModel: ActivityViewModel by viewModels()
    private val binding : ActivityMainBinding by viewBinding()
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)

        setSupportActionBar(binding.toolbar)

        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.navView.setupWithNavController(navController)

        activityViewModel.memberLiveData.observe(this){
            if(it != null){
                with(binding.navView){
                    findViewById<TextView>(R.id.mailField)?.text = it.email
                    findViewById<TextView>(R.id.userNameField)?.text = it.userName
                }
            }
        }

        activityViewModel.bookListLiveData.observe(this){
            activityViewModel.refreshBookIdList(it)
        }

        activityViewModel.databaseEvent.observe(this){ pair->
            when(pair?.first){
                "insertWord"->{ pair.second?.let { showInsertMessage(it) } }
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.drawerLayout.setDrawerLockMode(
                if(destination.id == R.id.wordCardFragment)
                    DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                else
                    DrawerLayout.LOCK_MODE_UNLOCKED
            )
        }

        binding.toolbarTextView.setOnClickListener {
            navController.navigate(R.id.searchFragment)
        }
    }

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

    private fun showInsertMessage(bundle: Bundle) {
        binding.root.showSnackbar("新增成功", "檢視"){
            //按下Snackbar上的按鈕之後, 可跳轉到新增的那一個單字卡
            val wordId = bundle.getLong("wordId")
            val bookId = bundle.getLong("bookId")

            navController.currentDestination?.let {
                val action = when(it.id){
                    R.id.wordCardFragment -> WordCardFragmentDirections
                            .actionWordCardFragmentSelf(bookId = bookId, wordId = wordId)
                    R.id.bookFragment -> BookFragmentDirections
                            .actionBookFragmentToWordCardFragment(
                            bookId = bookId, wordId = wordId)
                    else -> return@let
                }
                navController.navigate(action)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.search_toolbar, menu)
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
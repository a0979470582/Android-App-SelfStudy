package com.bu.selfstudy

import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.bu.selfstudy.databinding.ActivityMainBinding
import com.bu.selfstudy.tools.log
import com.bu.selfstudy.tools.viewBinding
import com.bu.selfstudy.ui.ActivityViewModel


class MainActivity : AppCompatActivity(){
    private val activityViewModel: ActivityViewModel by viewModels()

    private val binding : ActivityMainBinding by viewBinding()
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.navView.setupWithNavController(navController)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        activityViewModel.memberLiveData.observe(this){
        }


        activityViewModel.bookListLiveData.observe(this){
        }


        activityViewModel.bookLiveData.observe(this){
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
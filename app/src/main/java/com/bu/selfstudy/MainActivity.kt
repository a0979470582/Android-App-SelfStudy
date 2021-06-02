package com.bu.selfstudy

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.databinding.ActivityMainBinding
import com.bu.selfstudy.tool.log
import com.bu.selfstudy.tool.showSnackbar
import com.bu.selfstudy.tool.showToast
import com.bu.selfstudy.tool.viewBinding
import com.google.gson.JsonObject
import com.google.gson.JsonParser


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
            binding.navView.findViewById<TextView>(R.id.mailText).text = it.email
            binding.navView.findViewById<TextView>(R.id.userText).text = it.userName
        }

        activityViewModel.bookListLiveData.observe(this){
            activityViewModel.refreshBookIdList(it)
        }

        binding.fab.setOnClickListener {
            navController.navigate(R.id.addWordFragment)
        }

        navController.addOnDestinationChangedListener{ navController, navDestination, bundle ->
            binding.fab.isVisible = (
                    navDestination.id == R.id.bookFragment ||
                    navDestination.id == R.id.wordFragment
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
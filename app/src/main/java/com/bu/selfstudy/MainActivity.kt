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
import com.bu.selfstudy.tool.viewBinding
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.tool.log
import com.bu.selfstudy.tool.showSnackbar
import com.bu.selfstudy.tool.showToast


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

        activityViewModel.memberLiveData.observe(this){
        }

        activityViewModel.bookLiveData.observe(this){
            activityViewModel.refreshData()
        }

        activityViewModel.insertEvent.observe(this){
            if(it!=null && it.isNotEmpty()){
                binding.root.showSnackbar("已新增了${it.size}個單字", "檢視"){
                    "正在檢視中...".showToast()
                }
            }
        }
        activityViewModel.deleteEvent.observe(this){
            if(it!=null && it>0){
                "已移除${it}個單字".showToast()
            }
        }
        activityViewModel.deleteToTrashEvent.observe(this){
            "ok".log()
            if(it!=null && it>0){
                binding.root.showSnackbar("已將${it}個單字移至回收桶", "回復"){
                    "正在回復中...".showToast()
                }
            }
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
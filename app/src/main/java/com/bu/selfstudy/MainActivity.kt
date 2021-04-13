package com.bu.selfstudy

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import com.bu.selfstudy.databinding.ActivityMainBinding
import com.bu.selfstudy.tools.log
import com.bu.selfstudy.tools.viewBinding
import com.bu.selfstudy.ui.ActivityViewModel
import com.bu.selfstudy.ui.word.WordFragmentDirections


class MainActivity : AppCompatActivity(){
    private val viewModel: ActivityViewModel by viewModels()

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

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if(destination.id == R.id.wordFragment) {
                supportActionBar?.setDisplayShowTitleEnabled(false)
                //binding.spinner.visibility = View.VISIBLE
            } else {
                supportActionBar?.setDisplayShowTitleEnabled(true)
                binding.spinner.visibility = View.GONE
            }
        }



        viewModel.bookListLiveData.observe(this){
            viewModel.bookList.clear()
            viewModel.bookList.addAll(it)
        }

        viewModel.memberLiveData.observe(this){
        }
        viewModel.currentBookIdLiveData.observe(this){id->
            binding.spinner.setSelection(
               viewModel.bookListLiveData.value?.indexOfFirst {book->
                    book.id == id
                }!!
            )
        }

        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            viewModel.bookList
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinner.adapter = adapter
        }

        val arrayString = ArrayList<String>()
        arrayString.add("我的第一本題庫")
        arrayString.add("我的第一本題庫")
        arrayString.add("我的第一本題庫")
        arrayString.add("我的第一本題庫")
        arrayString.add("我的第一本題庫")
        val arrayAdapter = ArrayAdapter(this, R.layout.spinner_item, arrayString)
        binding.autoCompleteTextView.setAdapter(arrayAdapter)

        binding.spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val newBookId = viewModel.bookListLiveData.value!![position].id
                val newMember = viewModel.memberLiveData.value!!.copy()
                newMember.currentBookId = newBookId
                viewModel.updateMember(newMember)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

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
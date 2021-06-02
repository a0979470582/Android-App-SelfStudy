package com.bu.selfstudy.ui.home

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.R
import com.bu.selfstudy.databinding.FragmentHomeBinding
import com.bu.selfstudy.tool.showToast
import com.bu.selfstudy.tool.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment: Fragment()  {
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val binding: FragmentHomeBinding by viewBinding()
    private val listAdapter = HomeListAdapter()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding.adapter = listAdapter
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        binding.recyclerView.setHasFixedSize(true)

        activityViewModel.bookListLiveData.observe(viewLifecycleOwner){
            listAdapter.submitList(it)

        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        var backPressedToExitOnce: Boolean = false

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (backPressedToExitOnce) {
                requireActivity().finish()
                return@addCallback
            }

            backPressedToExitOnce = true
            resources.getString(R.string.toast_exit_app).showToast()
            lifecycleScope.launch {
                delay(2000)
                backPressedToExitOnce = false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_search -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_toolbar, menu)
    }
}
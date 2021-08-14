package com.bu.selfstudy.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.bu.selfstudy.MainActivity
import com.bu.selfstudy.R
import com.bu.selfstudy.data.repository.SearchRepository
import com.bu.selfstudy.databinding.FragmentSettingBinding
import com.bu.selfstudy.tool.showToast
import com.bu.selfstudy.tool.viewBinding
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SettingFragment : Fragment() {

    private val binding : FragmentSettingBinding by viewBinding()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity as MainActivity).let {
            it.setSupportActionBar(binding.toolbar)

            NavigationUI.setupActionBarWithNavController(
                it, findNavController(), it.appBarConfiguration)
        }

        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout, InternalSettingsFragment())
            .commit()
    }


    class InternalSettingsFragment : PreferenceFragmentCompat() {
        @InternalCoroutinesApi
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.setting_preference, rootKey)

            lifecycleScope.launchWhenStarted {
                SearchRepository.loadHistory("").collect {
                    findPreference<Preference>("clear_search")?.isEnabled = it.isNotEmpty()
                }
            }

            findPreference<Preference>("clear_search")?.setOnPreferenceClickListener {
                lifecycleScope.launch {
                    if(SearchRepository.clearAllHistory()>0)
                        "已清除搜尋紀錄".showToast()
                    else
                        "目前沒有搜尋紀錄".showToast()
                }
                return@setOnPreferenceClickListener true
            }
        }
    }
}


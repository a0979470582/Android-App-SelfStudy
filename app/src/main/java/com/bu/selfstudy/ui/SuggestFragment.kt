package com.bu.selfstudy.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bu.selfstudy.databinding.FragmentBookBinding
import com.bu.selfstudy.databinding.FragmentSettingBinding
import com.bu.selfstudy.databinding.FragmentSuggestBinding
import com.bu.selfstudy.tool.viewBinding
import com.bu.selfstudy.ui.book.BookAdapter
import com.bu.selfstudy.ui.book.BookViewModel

class SuggestFragment : Fragment() {

    private val binding : FragmentSuggestBinding by viewBinding()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}
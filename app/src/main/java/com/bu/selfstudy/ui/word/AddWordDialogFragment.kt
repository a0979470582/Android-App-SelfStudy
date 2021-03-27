package com.bu.selfstudy.ui.word

import android.os.Bundle
import android.view.*
import androidx.core.view.GravityCompat
import androidx.fragment.app.DialogFragment
import androidx.room.ColumnInfo
import com.bu.selfstudy.R
import com.bu.selfstudy.databinding.AddWordBinding
import com.bu.selfstudy.databinding.FragmentBookBinding
import java.util.*

class AddWordDialogFragment: DialogFragment() {

    private var _binding : AddWordBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_word, menu)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = AddWordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Toolbar
        binding.toolbar.title = "建立單字"
        binding.toolbar.inflateMenu(R.menu.book_toolbar)
        binding.toolbar.setNavigationIcon(R.drawable.abc_vector_test)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        binding.toolbar.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.action_save->{/*
                    var wordName: String
                    var translation: String ?= null,
                    var variation: String ?= null,
                    var example: String ?= null,
                    var description: String ?= null,
                    var dictionaryUri: String ?= null,
                    var timestamp: Date = Date(),
                    var isTrash:Boolean = false*/
                    false
                }
                else->true
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
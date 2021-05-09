package com.bu.selfstudy.ui.addword

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.FragmentAddWordBinding
import com.bu.selfstudy.tool.*
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AddWordFragment: Fragment() {
    private val binding : FragmentAddWordBinding by viewBinding()
    private val activityViewModel: ActivityViewModel by activityViewModels()

    var wordName = ""
    var pronunciation = ""
    var translation = ""
    var variation = ""
    var example = ""
    var note = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        getNavigationResultLiveData<Long>("bookId")?.observe(viewLifecycleOwner){
            val word = Word(
                bookId = it,
                wordName = wordName,
                pronunciation = pronunciation,
                translation = translation,
                variation = variation,
                example = example,
                note = note
            )
            activityViewModel.insertWord(word)
            findNavController().popBackStack()
            closeKeyboard()
        }

        binding.wordField.editText?.doOnTextChanged() { inputText, _, _, _ ->
            inputText?.let {
                if (it.isNotBlank())
                    binding.wordField.error=null
                binding.pronunciationField.editText!!.setText(it)
            }
        }

        binding.soundField.setEndIconOnClickListener {

        }

        binding.soundField.setStartIconOnClickListener {
        }

        binding.wordField.editText!!.setOnEditorActionListener { view, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {

                    binding.translationField.requestFocus()
                    binding.wordField.isEndIconVisible = false
                    false
                }
                else -> false
            }
        }
    }









    override fun onAttach(context: Context) {
        super.onAttach(context)

        val callback: OnBackPressedCallback = object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                val focusView = binding.root.findFocus()
                if(focusView == null){
                    //saveState()
                    findNavController().popBackStack()
                }else{
                    focusView.clearFocus()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
                this, // LifecycleOwner
                callback)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_send -> {
                wordName = binding.wordField.editText!!.text.toString()
                if(wordName.isBlank())
                    binding.wordField.error = "請輸入正確的英文單字"
                else{
                    findNavController().navigate(R.id.chooseBookDialog)
                }
            }
            android.R.id.home->{
                findNavController().popBackStack()
            }

        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.add_word_toolbar, menu)
    }

}
package com.bu.selfstudy.ui.editword

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.bu.selfstudy.MainActivity
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.FragmentAddWordBinding
import com.bu.selfstudy.tool.*


class EditWordFragment: Fragment() {
    private val binding : FragmentAddWordBinding by viewBinding()
    private val args: EditWordFragmentArgs by navArgs()

    private lateinit var viewModel: EditWordViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel = ViewModelProvider(
                this,
                EditWordViewModel.provideFactory(args.word)
        ).get(EditWordViewModel::class.java)

        fillInWord(viewModel.word)
        setCommonListener()
        setTextChangeListener()

        viewModel.wordLiveData.observe(viewLifecycleOwner){
            viewModel.setEditState()
        }

        viewModel.hasEditLiveData.observe(viewLifecycleOwner){
            (activity as AppCompatActivity).supportActionBar?.invalidateOptionsMenu()
        }

        viewModel.databaseEvent.observe(viewLifecycleOwner){
            when(it?.first){
                "update"->"已保存".showToast()
            }
        }
        setFragmentResultListener("saveAndExit"){_, bundle->
            viewModel.updateWord()
            setFragmentResult("EditWordFragment", Bundle())
            findNavController().popBackStack()
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        (activity as MainActivity).let {
            it.setSupportActionBar(binding.toolbar)

            NavigationUI.setupActionBarWithNavController(
                    it, findNavController(), it.appBarConfiguration)
        }
    }

    private fun setCommonListener() {
        binding.wordField.editText?.doOnTextChanged() { inputText, _, _, _ ->
            inputText?.let {
                if (it.isNotBlank())
                    binding.wordField.error = null
                binding.pronunciationField.editText!!.setText(it)
            }
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
        binding.soundField.setEndIconOnClickListener {
            popMenu()
        }

        binding.soundField.setStartIconOnClickListener {
            playSound()
        }
    }


    @SuppressLint("RestrictedApi")
    private fun setTextChangeListener() {
        val allEditText = listOf(
            binding.wordField.editText!!,
            binding.pronunciationField.editText!!,
            binding.translationField.editText!!,
            binding.variationField.editText!!,
            binding.exampleField.editText!!,
            binding.noteField.editText!!,
        )
        val allWordColumn = listOf(
            {text:String -> viewModel.word.wordName=text},
            {text:String -> viewModel.word.pronunciation=text},
            {text:String -> viewModel.word.translation=text},
            {text:String -> viewModel.word.variation=text},
            {text:String -> viewModel.word.example=text},
            {text:String -> viewModel.word.note=text}
        )

        for(i in allEditText.indices){
            allEditText[i].doOnTextChanged { text, _, _, _ ->
                allWordColumn[i](text.toString())
                viewModel.setEditState()
            }
        }
    }


    private fun fillInWord(word: Word){
        binding.wordField.editText!!.setText(word.wordName)
        binding.pronunciationField.editText!!.setText(word.pronunciation)
        binding.translationField.editText!!.setText(word.translation)
        binding.variationField.editText!!.setText(word.variation)
        binding.exampleField.editText!!.setText(word.example)
        binding.noteField.editText!!.setText(word.note)

        binding.wordField.isEndIconVisible = false
        binding.pronunciationField.isEndIconVisible = false
        binding.translationField.isEndIconVisible = false
        binding.variationField.isEndIconVisible = false
        binding.exampleField.isEndIconVisible = false
        binding.noteField.isEndIconVisible = false
    }

    fun popMenu(){

    }
    fun playSound(){

    }

    //clear current focus of EditText
    override fun onAttach(context: Context) {
        super.onAttach(context)

        val callback: OnBackPressedCallback = object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                val focusView = binding.root.findFocus()
                if(focusView != null)
                    focusView.clearFocus()
                else
                    navigateToBackOrDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this, // LifecycleOwner
            callback
        )
    }

    private fun navigateToBackOrDialog() {
        if(viewModel.hasEditLiveData.value!!) {
            val action = EditWordFragmentDirections.actionEditWordFragmentToExitEditingDialog()
            findNavController().navigate(action)
        } else
            findNavController().popBackStack()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_save -> {
                if (viewModel.word.wordName.isBlank())
                    binding.wordField.error = "請輸入正確的英文單字"
                else {
                    viewModel.updateWord()
                    closeKeyboard()
                    binding.root.findFocus()?.clearFocus()
                }
            }
            android.R.id.home -> {
                closeKeyboard()
                navigateToBackOrDialog()
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.only_save_toolbar, menu)
        menu.findItem(R.id.action_save).isEnabled = viewModel.hasEditLiveData.value?:false
    }
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_save).isEnabled = viewModel.hasEditLiveData.value?:false
    }

}
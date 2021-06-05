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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.FragmentAddWordBinding
import com.bu.selfstudy.tool.*


class EditWordFragment: Fragment() {
    private val binding : FragmentAddWordBinding by viewBinding()
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val args: EditWordFragmentArgs by navArgs()

    private lateinit var viewModel: EditWordViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        getNavigationResultLiveData<Boolean>("exitAndSave")?.observe(viewLifecycleOwner){
            viewModel.editWord?.let {word->
                //activityViewModel.updateWord(word)
            }
            findNavController().popBackStack(R.id.wordCardFragment, false)
        }

        setTextChangeListener()
        setCommonListener()

        viewModel = ViewModelProvider(this, EditWordViewModel.provideFactory(args.wordId))
            .get(EditWordViewModel::class.java)




        viewModel.wordLiveData.observe(viewLifecycleOwner){
            if(viewModel.editWord == null){
                viewModel.editWord = it.copy()
                fillInWord(it)
            }
            viewModel.hasEditLiveData.value = false
        }


        viewModel.hasEditLiveData.observe(viewLifecycleOwner){
            (activity as AppCompatActivity).supportActionBar?.invalidateOptionsMenu()
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
            {text:String -> viewModel.editWord?.wordName=text},
            {text:String -> viewModel.editWord?.pronunciation=text},
            {text:String -> viewModel.editWord?.translation=text},
            {text:String -> viewModel.editWord?.variation=text},
            {text:String -> viewModel.editWord?.example=text},
            {text:String -> viewModel.editWord?.note=text}
        )

        for(i in allEditText.indices){
            allEditText[i].doOnTextChanged { text, _, _, _ ->
                allWordColumn[i](text.toString())
                viewModel.setHasEdit()
            }
        }
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
                if(focusView != null){
                    focusView.clearFocus()
                    return
                }

                if(viewModel.hasEditLiveData.value!!) {
                    val action = EditWordFragmentDirections
                        .actionEditWordFragmentToExitEditingDialog()
                    findNavController().navigate(action)
                }
                else
                    findNavController().popBackStack()

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this, // LifecycleOwner
            callback
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_save -> {
                viewModel.editWord?.let {
                    if (it.wordName.isBlank())
                        binding.wordField.error = "請輸入正確的英文單字"
                    //else
                        //activityViewModel.updateWord(it)

                }
            }
            android.R.id.home -> {
                findNavController().popBackStack()
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.editword_toolbar, menu)
        menu.findItem(R.id.action_save).isEnabled = viewModel.hasEditLiveData.value?:false
    }
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_save).isEnabled = viewModel.hasEditLiveData.value?:false
    }

}
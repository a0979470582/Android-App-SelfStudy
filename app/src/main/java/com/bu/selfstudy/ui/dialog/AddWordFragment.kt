package com.bu.selfstudy.ui.dialog

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.FragmentAddWordBinding
import com.bu.selfstudy.tools.*


class AddWordFragment: Fragment() {
    private val binding : FragmentAddWordBinding by viewBinding()
    private val args: AddWordFragmentArgs by navArgs()
    private var bookId:Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        bookId = args.defaultBookId

        getDialogResult(){_, bundle ->
            bookId = bundle.getLong("bookId")
            returnNewWord()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_send -> {
                if (bookId == 0L) {
                    navigateToChooseBookDialog()
                }else{
                    returnNewWord()
                }
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.add_word_toolbar, menu)
    }

    private fun returnNewWord(){
        val wordName = binding.wordNameEdT.text.toString()
        val translation = binding.translationEdT.text.toString()

        val word = Word(
            wordName = wordName,
            translation = translation,
            bookId = bookId
        )
        setDialogResult("word" to word)
        findNavController().popBackStack()
    }


    private fun navigateToChooseBookDialog() {
        val action = AddWordFragmentDirections.actionGlobalChooseBookDialog(
                args.bookNames,
                args.bookIds,
                "加入哪一個題庫?",
                "insert"
        )
        findNavController().navigate(action)
    }
}
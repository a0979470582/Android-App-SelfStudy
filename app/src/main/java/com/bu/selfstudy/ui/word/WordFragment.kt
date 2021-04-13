package com.bu.selfstudy.ui.word


import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import com.bu.selfstudy.MainActivity
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.FragmentWordBinding
import com.bu.selfstudy.tools.showToast
import com.bu.selfstudy.tools.*
import com.bu.selfstudy.ui.ActivityViewModel

class WordFragment : Fragment() {
    private val viewModel: WordViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by activityViewModels()

    private val binding: FragmentWordBinding by viewBinding()

    private val adapter = WordAdapter()
    private lateinit var tracker: SelectionTracker<Long>
    private var searchView: SearchView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        activityViewModel.currentBookIdLiveData.observe(viewLifecycleOwner){
            viewModel.currentBookIdLiveData.value = it
        }

        viewModel.wordListLiveData.observe(viewLifecycleOwner){
            it.size.log()
        }

        binding.translationTextView.setTitleText("釋義")
        binding.translationTextView
            .setContentText("adj. 特殊的；特定的；特別的；特有的，獨特的；異常的" +
                    "\nn. 投擲；投距，射程")
        binding.variationTextView.setTitleText("變化形")
        binding.variationTextView.setContentText("Plurals：sounds\nConjugation ：sounded sounded sounding")

        binding.exampleTextView.setTitleText("例句")
        binding.exampleTextView.setContentText("\"形容詞\n" +
                "1.特殊的；特定的；特別的\n" +
                "The teacher showed particular concern for the disabled child. 老師特別關心那個殘疾兒童。\n" +
                "2.特有的，獨特的；異常的\n" +
                "Her particular way of smiling left a good impression on me. 她特有的微笑給我留下了美好的印象。\n" +
                "3.（過於）講究的；苛求的，挑剔的[（+about/over）][（+wh-）]\n" +
                "She is particular about what she eats. 她過分講究吃。\n" +
                "4.細緻的，詳細的\n" +
                "The witness gave us a particular account of what happened. 目擊者把發生的事情詳細地對我們說了一遍。\n" +
                "n.名詞\n" +
                "1.個別的項目，細目\n" +
                "The particular may have to be satisfied to the general. 為顧全總體個別的項目也許不得不放棄。\n" +
                "2.詳細情況\n" +
                "I suppose the secretary knows the particulars of the plan. 我想那位祕書知道這一計畫的詳細情況。\n" +
                "3.特點，特色\" ")

        binding.noteTextView.setTitleText("註記")
        binding.noteTextView.setContentText("這是一個很棒的單字 !")


    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_search->{
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.word_toolbar, menu)

    }
}
        /*
        binding.recyclerView.let {
            it.adapter = this.adapter
            it.setHasFixedSize(true)
        }

        tracker = buildSelectionTracker(
                binding.recyclerView,
                viewModel.idList,
                SelectionPredicates.createSelectAnything(),
                R.menu.word_action_mode,
                ::onActionItemClicked
        ).also { adapter.tracker = it }


        viewModel.wordListLD.observe(viewLifecycleOwner){ words ->
            adapter.setWordList(words)
            viewModel.setIdList(words)
            viewModel.isLoadingLD.value = false
        }
        viewModel.bookListLD.observe(viewLifecycleOwner){}

        /**db event*/
        getDatabaseResult(viewModel.databaseEventLD){_, message, _ ->
            message.showToast()
        }

        /**dialog event*/
        getDialogResult()
    }
*/
/*
    private fun onActionItemClicked(itemId:Int){
        when (itemId) {
            R.id.action_delete -> {
                val action = WordFragmentDirections.actionGlobalToDeleteDialog(
                        "是否刪除 ${tracker.selection.size()} 個單字 ?")
                findNavController().navigate(action)
            }
            R.id.action_choose_all -> {
                viewModel.idList.let {
                    tracker.setItemsSelected(it,it.size != tracker.selection.size()
                    )
                }
            }

            R.id.action_copy -> {
                navigateToChooseBookDialog("copy")
            }
            R.id.action_move -> {
                navigateToChooseBookDialog("move")
            }
        }
    }


    /**遺珠之憾:1. 空query時旋轉螢幕無法保留介面*/
    /**在這裡產生searchView對象*/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.word_toolbar, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val searchManager = requireActivity().getSystemService(SEARCH_SERVICE) as SearchManager
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

        searchView?.apply{
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                /**避免按下送出後, 鍵盤消失, 但輸入框的光標仍在閃爍*/
                override fun onQueryTextSubmit(query: String): Boolean {
                    clearFocus()
                    return false
                }

                /**每一次搜尋文字改變, 就觸發資料庫刷新*/
                override fun onQueryTextChange(query: String): Boolean {
                    viewModel.searchQueryLD.value = query
                    return false
                }
            })

            /**如果頁面重建, ViewModel保有查詢字串, 表示先前頁面銷毀時, 使用者正在使用查詢
            我們知道頁面銷毀會使SearchView也銷毀, 但SearchView在第一次開啟或最後銷毀時, 都會
            觸發onQueryTextChange且query是空值, 注意expandActionView就會觸發此情形*/
            val pendingQuery = viewModel.searchQueryLD.value
            if(pendingQuery!=null && pendingQuery.isNotBlank()){
                searchItem.expandActionView()
                setQuery(pendingQuery, false)
                clearFocus()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add->{
                navigateToAddWordFragment()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToChooseBookDialog(action:String){
        viewModel.bookListLD.value?.let {
            val bookList = it.filter { it.id != viewModel.bookId }
            val action = WordFragmentDirections.actionGlobalChooseBookDialog(
                    bookList.map { it.bookName }.toTypedArray(),
                    bookList.map { it.id }.toLongArray(),
                    "選擇一個題庫",
                    action
            )
            findNavController().navigate(action)
        }
    }

    private fun navigateToAddWordFragment(){
        val action = WordFragmentDirections.actionGlobalAddWordFragment(
                args.bookId,
                viewModel.bookListLD.value!!.map { it.bookName }.toTypedArray(),
                viewModel.bookListLD.value!!.map { it.id }.toLongArray()
        )
        findNavController().navigate(action)
    }

    private fun getDialogResult(){
        getDialogResult {dialogName, bundle ->
            when(dialogName){
                "ToDeleteDialog"->{
                    if(bundle.getBoolean("isDelete"))
                        viewModel.deleteWordsToTrash(tracker.selection.toList())
                }
                "ChooseBookDialog"->{
                    bundle.getLong("bookId").let {bookId->
                        when(bundle.getString("action")){
                            "copy"->viewModel.copyWordsTo(tracker.selection.toList(), bookId)
                            "move"->viewModel.moveWordsTo(tracker.selection.toList(), bookId)
                        }
                    }
                }
                "AddWordFragment"->{
                    bundle.getParcelable<Word>("word").let { word->
                        viewModel.insertWords(listOfNotNull(word))
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        tracker.onRestoreInstanceState(savedInstanceState)
    }
}*/
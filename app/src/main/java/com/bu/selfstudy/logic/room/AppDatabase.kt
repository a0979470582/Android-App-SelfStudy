package com.bu.selfstudy.logic.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bu.selfstudy.SelfStudyApplication.Companion.context
import com.bu.selfstudy.logic.dao.BookDao
import com.bu.selfstudy.logic.dao.MemberDao
import com.bu.selfstudy.logic.dao.WordDao
import com.bu.selfstudy.logic.model.Book
import com.bu.selfstudy.logic.model.Member
import com.bu.selfstudy.logic.model.Word
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Database(version = 1, entities = [Member::class, Book::class, Word::class])
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun bookDao(): BookDao
    abstract fun wordDao(): WordDao

    companion object {
        private var instance: AppDatabase? = null
        @Synchronized
        fun getDatabase(): AppDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                    .createFromAsset("database/app_database")
                    .build().apply {
                        instance = this
                        //initialize()
                }
        }
    }
/*
    private fun initialize(){
        val repos = arrayOf("我的第一本題庫", "我的第二本題庫", "我的第三本題庫")
        val words = arrayOf("reserve", "particular", "humor")
        val translations = arrayOf(
            "vt.保存, 預訂\nn.保存, 儲存",
            "adj.特別的, 細緻的\nn.細目",
            "n.幽默感[U]；幽默[U]"
        )
        val variations = arrayOf(
            "過去式：reserved 過去分詞：reserved 現在分詞：reserving\n名詞複數：reserves",
            "名詞複數：particulars",
            "名詞複數：humors\n過去式：humored 過去分詞：humored 現在分詞：humoring"
        )

        val examples = arrayOf(
            "vt.及物動詞\n" +
                    "1.儲備，保存；保留[（+for）]\n" +
                    "These seats are reserved for special guests. 這些座位是為特別來賓保留的。\n" +
                    "We will reserve the money; we may need it later. 我們將把這筆錢存起來，也許以後用得著。\n" +
                    "2.預約，預訂\n" +
                    "They have reserved rooms at a hotel. 他們已預訂了旅館房間。\n" +
                    "I have reserved a table at the restaurant. 我已在飯店預訂了一桌菜。\n" +
                    "3.延遲作出；暫時不作\n" +
                    "The court will reserve judgement. 法庭將延期判決。\n" +
                    "n.名詞\n" +
                    "1.儲備（物）；儲備金；保留（物）；儲藏量[C][U][（+of）]\n" +
                    "The old man kept a large reserve of firewood for cold weather. 這位老人貯存了大量的柴薪以備天冷時用。",  //2
            "adj.形容詞\n" +
                    "1.特殊的；特定的；特別的[Z][B]\n" +
                    "The teacher showed particular concern for the disabled child. 老師特別關心那個殘疾兒童。\n" +
                    "2.特有的，獨特的；異常的[Z][B]\n" +
                    "Her particular way of smiling left a good impression on me. 她特有的微笑給我留下了美好的印象。\n" +
                    "3.（過於）講究的；苛求的，挑剔的[（+about/over）][（+wh-）]\n" +
                    "She is particular about what she eats. 她過分講究吃。\n" +
                    "4.細緻的，詳細的[B]\n" +
                    "The witness gave us a particular account of what happened. 目擊者把發生的事情詳細地對我們說了一遍。\n" +
                    "n.名詞\n" +
                    "1.個別的項目，細目[C]\n" +
                    "The particular may have to be satisfied to the general. 為顧全總體個別的項目也許不得不放棄。\n" +
                    "2.詳細情況[P]\n" +
                    "I suppose the secretary knows the particulars of the plan. 我想那位祕書知道這一計畫的詳細情況。\n",  //3
            "n.名詞\n" +
                    "1.幽默[U]\n" +
                    "Humor is often more than a laughing matter. 幽默常常不只是一笑了之的事。\n" +
                    "He has a good sense of humor. 他富於幽默感。\n" +
                    "2.心情，情緒[S]\n" +
                    "The boss is in no humor to talk to you right now. 此刻老板沒有心情同你說話。"
        )
        val descriptions = arrayOf(
            "to fall back on one's reserves 依靠自己的儲備\noil/capital reserves 石油／資金儲備",
            "同義詞 a.特別的 special, unusual, different",
            "同義詞 n.幽默感；幽默 wit, pleasantry, comedy"
        )

        val dictionaryUri = arrayOf(
            "https://tw.dictionary.search.yahoo.com/search?p=reserve",
            "https://tw.dictionary.search.yahoo.com/search?p=particular",
            "https://tw.dictionary.search.yahoo.com/search?p=humor"
        )
        val pronounceUri = arrayOf(
            "reserve.mp3",
            "particular.mp3",
            "humor.mp3"
        )



        val member = Member(
            email = "a0979470582@gmail.com",
            password = "123456789",
            userName = "LuLu",
            sex = "F",
            iconUri = "icon.jpg"
        )
        runBlocking{member.id = memberDao().insertMember(member)}

        var bookList = ArrayList<Book>()
        repeat(12){
            bookList.add(Book(bookName = repos[it%3], memberId = member.id))
        }
        var bookIds:List<Long>
        runBlocking{bookIds = bookDao().insertBooks(*bookList.toTypedArray())}

        for (i in bookIds.indices){
            bookList[i].id = bookIds[i]
        }


        var wordListData = ArrayList<Word>()
        repeat(3){
            val index = it%3
            val word = Word(wordName = words[index],
                translation = translations[index],
                variation = variations[index],
                example = examples[index],
                description = descriptions[index],
                dictionaryUri = dictionaryUri[index],
                pronounceUri = pronounceUri[index],
                bookId = 0//Pending
            )
            wordListData.add(word)
        }

        var wordList = ArrayList<Word>()
        for((index, book) in bookList.withIndex()){
            repeat((index+1)*15){
                val word = wordListData[it%3]
                word.bookId = book.id
                wordList.add(word)
            }
            runBlocking{
                wordDao().insertWords(*wordList.toTypedArray())
            }
            wordList.clear()
        }
    }*/
}
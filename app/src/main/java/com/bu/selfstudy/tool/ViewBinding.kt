package com.bu.selfstudy.tool

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import java.lang.reflect.Method
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**流程:
 1. 在Fragment中, 變數binding是一個包裹實際的xxxViewBinding對象的屬性類, onCreateView()中引用
    binding.root, 就會觸發屬性類的getValue, 如果該值不存在則透過xxxViewBinding的靜態方法inflate
    重新創建
 2. 不需要在onDestroyView()中銷毀viewBinding, 因為現在binding對象的生命週期跟fragment一樣長,
    一般情況下需要銷毀, 是因為Fragment進出返回棧, 使onCreateView和onDestroyView來回執行, 而onCreateView
    可能就因此建立出多個View
 3. 由於Java的泛型會在執行時期抹除, 因此T::class.java語法必須藉由inline+reified語法, inline是指會將方法內
    的程式碼移到調用處, 而此例中的T:ViewBinding就會在編譯時換成FragmentRecyclerBinding, 這是inline的原理,
    但要使用泛型實化必須加上reified關鍵字, 泛型實化是指將T當作String類, Activity類一樣使用, 就像T::class.java
 */




inline fun <reified T : ViewBinding> Fragment.viewBinding():FragmentViewBindingProperty<T>{
    val cls:Class<T> = (T::class.java)
    val method: Method = cls.getMethod("inflate", LayoutInflater::class.java)
    return FragmentViewBindingProperty(method)
}
class FragmentViewBindingProperty<T : ViewBinding>(
        private val method: Method
    ): ReadOnlyProperty<Fragment, T>{

    private var viewBinding:T? = null

    override fun getValue(fragment: Fragment, property: KProperty<*>): T {
        viewBinding?.let { return it }

        viewBinding = method.invoke(null, fragment.layoutInflater) as T
        return viewBinding!!
    }
}

inline fun <reified T : ViewBinding> Activity.viewBinding():ActivityViewBindingProperty<T>{
    val cls:Class<T> = (T::class.java)
    val method: Method = cls.getMethod("inflate", LayoutInflater::class.java)
    return ActivityViewBindingProperty(method)
}
class ActivityViewBindingProperty<T : ViewBinding>(
    private val method: Method
): ReadOnlyProperty<Activity, T>{

    private var viewBinding:T? = null

    override fun getValue(activity: Activity, property: KProperty<*>): T {
        viewBinding?.let { return it }

        viewBinding = method.invoke(null, activity.layoutInflater) as T
        return viewBinding!!
    }
}


/**RecyclerView*/

inline fun <reified T : ViewBinding> viewBinding(parent: ViewGroup):BindingViewHolder<T>{
    val cls:Class<T> = (T::class.java)
    val method: Method = cls.getMethod("inflate", LayoutInflater::class.java,
                                            ViewGroup::class.java, Boolean::class.java)
    val binding = method.invoke(null, LayoutInflater.from(parent.context), parent, false) as T
    return BindingViewHolder(binding)
}
class BindingViewHolder<T:ViewBinding>(val binding: T) : RecyclerView.ViewHolder(binding.root)

inline fun <reified T : ViewBinding> DialogFragment.viewBinding():DialogViewBindingProperty<T>{
    val cls:Class<T> = (T::class.java)
    val method: Method = cls.getMethod("inflate", LayoutInflater::class.java)
    return DialogViewBindingProperty(method)
}
class DialogViewBindingProperty<T : ViewBinding>(
    private val method: Method
): ReadOnlyProperty<DialogFragment, T>{

    private var viewBinding:T? = null

    override fun getValue(dialogFragment: DialogFragment, property: KProperty<*>): T {
        viewBinding?.let { return it }

        viewBinding = method.invoke(null, dialogFragment.layoutInflater) as T
        return viewBinding!!
    }
}

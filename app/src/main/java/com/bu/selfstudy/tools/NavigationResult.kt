package com.bu.selfstudy.tools

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController

fun <T> Fragment.getNavigationResult(key: String = "result") =
    findNavController(this).currentBackStackEntry?.savedStateHandle?.get<T>(key)

fun <T> Fragment.getNavigationResultLiveData(key: String = "result") =
    findNavController(this).currentBackStackEntry?.savedStateHandle?.getLiveData<T>(key)

fun <T> Fragment.setNavigationResult(key: String = "result" ,result: T) {
    findNavController(this).previousBackStackEntry?.savedStateHandle?.set(key, result)
}
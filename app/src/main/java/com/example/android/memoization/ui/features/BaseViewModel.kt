package com.example.android.memoization.ui.features

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow

abstract class BaseViewModel <T, B> : ViewModel() {

    private val _toastMessage = MutableLiveData<Any>()
    open val toastMessage: LiveData<Any> = _toastMessage

    fun updateToastMessage(message: Any) {
        _toastMessage.value = message
    }

    abstract fun getDataToDisplay(): Flow<T>
    abstract fun setArgs(args: B?)
    abstract fun onBackPressed(navController: NavController)



}
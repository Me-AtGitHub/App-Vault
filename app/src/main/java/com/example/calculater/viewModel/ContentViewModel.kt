package com.example.calculater.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.calculater.utils.CommonFunctions
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ContentViewModel : ViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable -> }
    private val fileLiveData = MutableLiveData<MutableList<File>>()

    fun getFiles() {
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            fileLiveData.postValue(CommonFunctions.getFilesOne(null))
        }
    }


}
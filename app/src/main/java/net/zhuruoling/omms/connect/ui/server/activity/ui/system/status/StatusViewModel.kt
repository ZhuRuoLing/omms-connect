package net.zhuruoling.omms.connect.ui.server.activity.ui.system.status

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StatusViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is status Fragment"
    }
    val text: LiveData<String> = _text
}
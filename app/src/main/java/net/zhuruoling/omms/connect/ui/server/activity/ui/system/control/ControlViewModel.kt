package net.zhuruoling.omms.connect.ui.server.activity.ui.system.control

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ControlViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is ctrl Fragment"
    }
    val text: LiveData<String> = _text
}
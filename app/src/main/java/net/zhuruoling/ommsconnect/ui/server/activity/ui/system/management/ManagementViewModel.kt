package net.zhuruoling.ommsconnect.ui.server.activity.ui.system.management

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ManagementViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is mgmt Fragment"
    }
    val text: LiveData<String> = _text
}
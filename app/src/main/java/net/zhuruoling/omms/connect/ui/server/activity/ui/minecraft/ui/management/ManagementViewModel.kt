package net.zhuruoling.omms.connect.ui.server.activity.ui.minecraft.ui.management

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ManagementViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is mgmt Fragment"
    }
    val text: LiveData<String> = _text
}
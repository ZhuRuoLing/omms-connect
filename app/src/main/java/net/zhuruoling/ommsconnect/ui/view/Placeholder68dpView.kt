package net.zhuruoling.ommsconnect.ui.view

import android.content.Context
import net.zhuruoling.ommsconnect.client.Connection.getClientSession
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.TextView
import net.zhuruoling.ommsconnect.WhitelistEditActivity
import android.view.LayoutInflater
import net.zhuruoling.ommsconnect.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.content.DialogInterface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import net.zhuruoling.omms.client.util.Result
import com.blankj.utilcode.util.ToastUtils
import kotlinx.coroutines.*

class Placeholder68dpView : ConstraintLayout {

    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.placeholder_68, this)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.placeholder_68, this)
    }

}
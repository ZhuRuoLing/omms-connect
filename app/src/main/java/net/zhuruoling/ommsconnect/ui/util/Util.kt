package net.zhuruoling.ommsconnect.ui.util

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.zhuruoling.omms.client.controller.Controller

fun genControllerIntroText(controller: Controller): String {
    val stringBuilder = StringBuilder()
    stringBuilder.append("Type:")
    stringBuilder.append(controller.type)
    stringBuilder.append(" ")
    return stringBuilder.toString()
}

fun getSystemType(origin: String): String {
    return if (origin.contains("Windows")) "WINDOWS" else (if (origin.contains("Linux") || origin.contains(
            "linux"
        )
    ) "LINUX" else "")
}

enum class ServerEntryType {
    OS, MINECRAFT, BRIDGE, UNDEFINED
}


fun showErrorDialog(info:String, context:Context){
        val alertDialog = context.let {
            MaterialAlertDialogBuilder(it)
                .setCancelable(true)
                .setTitle("Loading")
                .setMessage(info)
                .create()
        }
        alertDialog.show()

}
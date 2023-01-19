package net.zhuruoling.omms.connect.ui.util

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.zhuruoling.omms.client.controller.Controller
import net.zhuruoling.omms.connect.storage.PreferencesStorage

fun genControllerText(controller: Controller): String {
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
                .setTitle("Error")
                .setMessage(info)
                .create()
        }
        alertDialog.show()

}

fun getUtilCommands(context: Context): List<String> {
    return PreferencesStorage.withContext(context, "util_command")
        .getStringSet("util_commands",
        mutableSetOf()).toList()
}
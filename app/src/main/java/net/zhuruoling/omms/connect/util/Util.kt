package net.zhuruoling.omms.connect.util

import android.content.Context
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import net.zhuruoling.omms.client.controller.Controller
import net.zhuruoling.omms.client.util.Result
import net.zhuruoling.omms.connect.R
import net.zhuruoling.omms.connect.storage.PreferencesStorage
import java.util.concurrent.CountDownLatch

private val gson = GsonBuilder().serializeNulls().create()

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

fun <T> fromJson(src: String, klass: Class<out T>): T{
    return gson.fromJson(src, klass)
}

fun toJson(obj: Any): String{
    return gson.toJson(obj)
}

fun toHumanReadableErrorMessage(base: String, result: Result, context: Context):String{
    val errorString = when(result){
        Result.PERMISSION_DENIED -> context.getString(R.string.error_permission_denied)
        Result.FAIL -> context.getString(R.string.error_unknown_error)
        else -> result.name
    }
    return String.format(base, errorString)
}

fun toHumanReadableErrorMessageResId(@StringRes base: Int, result: Result, context: Context):String{
    return toHumanReadableErrorMessage(context.getString(base), result, context)
}

//fun toHumanReadableErrorMessageResId(@StringRes base: Int, message: String, context: Context):String{
//    return toHumanReadableErrorMessage(context.getString(base), message, context)
//}

fun formatResString(@StringRes format: Int, vararg objects: Any?, context: Context):String{
    return context.getString(format).format(*objects)
}

fun awaitExecute(block: (CountDownLatch) -> Unit) {
    val latch = CountDownLatch(1)
    block(latch)
    latch.await()
}
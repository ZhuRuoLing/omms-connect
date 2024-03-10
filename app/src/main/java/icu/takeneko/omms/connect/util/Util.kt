package icu.takeneko.omms.connect.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import icu.takeneko.omms.client.data.controller.Controller
import icu.takeneko.omms.client.util.Result
import icu.takeneko.omms.connect.R
import icu.takeneko.omms.connect.storage.PreferencesStorage
import java.util.concurrent.CountDownLatch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private val gson = GsonBuilder().serializeNulls().create()

fun genControllerText(controller: Controller): String {
    return "(${controller.name}) Type: ${controller.type} "
}

enum class ServerEntryType {
    OS, MINECRAFT, UNDEFINED
}


fun showErrorDialog(info: String, context: Context) {
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
        .getStringSet(
            "util_commands",
            mutableSetOf()
        ).toList()
}

fun <T> fromJson(src: String, klass: Class<out T>): T {
    return gson.fromJson(src, klass)
}

fun toJson(obj: Any): String {
    return gson.toJson(obj)
}


//fun (@StringRes format: Int, vararg objects: Any?, context: Context): String {
//    return context.getString(format).format(*objects)
//}

fun awaitExecute(block: (CountDownLatch) -> Unit) {
    val latch = CountDownLatch(1)
    block(latch)
    latch.await()
}

fun AutoCloseable?.closeFinally(cause: Throwable?) =
    when {
        this == null -> {}
        cause == null -> close()
        else -> try {
            close()
        } catch (closeException: Throwable) {
            cause.addSuppressed(closeException)
        }
    }

@OptIn(ExperimentalContracts::class)
inline fun <T : AutoCloseable?, R> T.with(block: T.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        this.closeFinally(exception)
    }
}

inline fun String.ifNotEmpty(block: String.() -> Unit) =
    if (this.isNotEmpty())
        block(this)
    else {
    }

fun getRealPathFromUri(context: Context, uri: Uri): String? {
    var filePath: String? = ""
    val scheme = uri.scheme
    if (scheme == null) {
        filePath = uri.path
    } else {
        if (ContentResolver.SCHEME_FILE == scheme) {
            filePath = uri.path
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, proj, null, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val columnIndex: Int =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    filePath = cursor.getString(columnIndex)
                }
                cursor.close()
            }
            if (filePath.isNullOrEmpty()) {
                filePath = getFilePathForNonMediaUri(context, uri)
            }
        }
    }
    return filePath
}

private fun getFilePathForNonMediaUri(context: Context, uri: Uri): String {
    var filePath = ""
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    if (cursor != null) {
        if (cursor.moveToFirst()) {
            val columnIndex: Int = cursor.getColumnIndexOrThrow("_data")
            filePath = cursor.getString(columnIndex)
        }
        cursor.close()
    }
    return filePath
}
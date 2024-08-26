package icu.takeneko.omms.connect.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import icu.takeneko.omms.client.exception.ConnectionFailedException
import icu.takeneko.omms.client.exception.PermissionDeniedException
import icu.takeneko.omms.client.exception.ServerInternalErrorException
import icu.takeneko.omms.client.exception.VersionNotMatchException
import icu.takeneko.omms.connect.R
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException

fun Context.format(@StringRes format: Int, vararg objects: Any?):String{
    return getString(format).format(*objects)
}

fun Int.asColor(context: Context) = context.getColor(this)


fun Fragment.format(@StringRes format: Int, vararg objects: Any?):String = requireContext().format(format, *objects)

fun Context.toErrorMessage(e:Throwable):String{
    return when(e){
        is VersionNotMatchException -> {
            if (e.serverVersion != 0L){
                format(R.string.error_version_mismatch, e.serverVersion, e.clientVersion)
            }else{
                format(R.string.error_version_mismatch_no_server_version, e.clientVersion)
            }
        }

        is TimeoutException -> {
            format(R.string.error_operation_timeout)
        }

        is ExecutionException -> {
            val cause = e.cause
            if (cause == null){
                format(R.string.error_unknown, e.toString())
            }else{
                // unwrap exception cause
                toErrorMessage(cause)
            }
        }

        is IOException -> {
            format(R.string.error_io_exception, e.message)
        }

        is GeneralSecurityException -> {
            format(R.string.error_security_exception)
        }

        is ConnectionFailedException -> {
            format(R.string.error_connect_fail, e.message)
        }

        is ServerInternalErrorException -> {
            format(R.string.error_server_internal_error, e.message)
        }

        is PermissionDeniedException -> {
            format(R.string.error_permission_denied)
        }

        else -> {
            format(R.string.error_unknown, e.toString())
        }
    }
}
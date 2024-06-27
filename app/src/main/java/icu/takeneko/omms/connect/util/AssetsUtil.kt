package icu.takeneko.omms.connect.util

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import icu.takeneko.omms.connect.R
import icu.takeneko.omms.connect.resource.ServerIconResourceManager
import java.util.*

object AssetsUtil {
    @JvmStatic
    fun getServerIcon(type: String, parent: Activity): Drawable? =
        when (type.uppercase(Locale.ROOT)) {
            "FABRIC" -> AppCompatResources.getDrawable(parent, R.mipmap.ic_server_fabric)
            else -> ServerIconResourceManager[type]
        }
}

fun Context.getIconFromDesc(desc: String):Drawable? =
    when (desc.uppercase(Locale.ROOT)) {
    "FABRIC" -> AppCompatResources.getDrawable(this, R.mipmap.ic_server_fabric)
    else -> ServerIconResourceManager[desc]
}
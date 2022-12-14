package net.zhuruoling.ommsconnect.ui.util

import android.app.Activity
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import net.zhuruoling.ommsconnect.R
import java.util.*

object Assets {
    @JvmStatic
    fun getServerIcon(type: String, parent: Activity): Drawable? =
        when (type.uppercase(Locale.ROOT)) {
            "FABRIC" -> AppCompatResources.getDrawable(parent, R.mipmap.ic_server_fabric)
            "WINDOWS" -> AppCompatResources.getDrawable(parent, R.drawable.ic_server_windows)
            "LINUX" -> AppCompatResources.getDrawable(parent, R.drawable.ic_server_linux)
            else -> AppCompatResources.getDrawable(parent, R.drawable.ic_server_default)
        }
}
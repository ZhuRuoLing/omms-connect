package icu.takeneko.omms.connect.resource

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.os.FileUtils
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import icu.takeneko.omms.connect.R
import icu.takeneko.omms.connect.storage.PreferencesStorage
import icu.takeneko.omms.connect.util.ifNotEmpty
import icu.takeneko.omms.connect.util.with

object ServerIconResourceManager {
    private val storage = mutableMapOf<String, Drawable>()
    operator fun get(id: String): Drawable {
        return storage[id] ?: storage["DEFAULT"]!!
    }

    fun importImageFromUri(context: Context, id: String, uri: Uri) {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        if (dir.exists() || dir.mkdir()) {
            dir.resolve("${id}_icon").run {
                if (exists()) delete()
                if (!exists()) createNewFile()
                Log.i("OMMS", "Import icon from file: ${id}_icon <= ${dir.absolutePath}")
                outputStream().with {
                    FileUtils.copy(context.contentResolver.openInputStream(uri)!!, this)
                    this.flush()
                }
                addImage(context, id, this.absolutePath)
            }
        }
        load(context)
    }

    private fun loadFromFile(id: String, path: String) {
        Log.i("OMMS", "Load icon from file: $id <= $path")
        storage += id to Drawable.createFromPath(path)!!
    }

    private fun addImage(context: Context, id: String, path: String) {
        PreferencesStorage.withContext(context, "server_icon").with {
            if (!contains("icons")) {
                putStringSet("icons", mutableSetOf("${id}_icon"))
            } else {
                val set = getStringSet("icons", mutableSetOf()).apply {
                    add("${id}_icon")
                }
                putStringSet("icons", set)
            }
            putString("${id}_icon", path)
        }
        load(context)
    }

    fun load(context: Context) {
        storage.clear()
        addDefaultServerIcon(context)
        PreferencesStorage.withContext(context, "server_icon").with {
            if (!this.contains("icons")) {
                putStringSet("icons", mutableSetOf())
                commit()
            } else {
                val newSet = mutableSetOf<String>()
                getStringSet("icons", mutableSetOf()).forEach {
                    if (contains(it)) newSet.add(it)
                }
                putStringSet("icons", newSet)
                newSet.forEach {
                    getString(it, "").ifNotEmpty {
                        loadFromFile(it, this)
                    }
                }
            }
        }
    }

    private fun addDefaultServerIcon(context: Context) {
        storage += "FABRIC" to AppCompatResources.getDrawable(context, R.mipmap.ic_server_fabric)!!
        storage += "WINDOWS" to AppCompatResources.getDrawable(
            context,
            R.drawable.ic_server_windows
        )!!
        storage += "LINUX" to AppCompatResources.getDrawable(context, R.drawable.ic_server_linux)!!
        storage += "DEFAULT" to AppCompatResources.getDrawable(
            context,
            R.drawable.ic_server_default
        )!!
    }

    fun removeIcon(context: Context, id: String): Boolean {
        if (id in storage) return false
        PreferencesStorage.withContext(context, "server_icon").with {
            if (id in this) {
                this -= id
                commit()
            }
            if ("icons" in this) {
                val newSet = getStringSet("icons", mutableSetOf())
                if (id in newSet) {
                    newSet -= id
                }
                this.putStringSet("icons", newSet)
                commit()
            }
        }
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        if (dir.exists() || dir.mkdir()) {
            dir.resolve(id).run {
                if (exists())delete()
            }
        }
        load(context)
        return true
    }

    fun forEach(function: String.(Drawable) -> Unit) {
        storage.forEach(function)
    }

}
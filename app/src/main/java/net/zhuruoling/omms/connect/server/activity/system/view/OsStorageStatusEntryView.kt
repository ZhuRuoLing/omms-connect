package net.zhuruoling.omms.connect.server.activity.system.view


import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView
import net.zhuruoling.omms.client.system.FileSystemInfo
import net.zhuruoling.omms.connect.R
import kotlin.math.ceil

class OsStorageStatusEntryView : LinearLayout {
    private lateinit var usedSpaceIndicator: CircularProgressIndicator
    private lateinit var filesystemTitle: MaterialTextView
    private lateinit var filesystemSubTitle: MaterialTextView

    constructor(context: Context?) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.os_storage_status_entry, this)
        initViews()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.command_text_card, this)
        initViews()
    }

    private fun initViews() {
        usedSpaceIndicator = findViewById(R.id.os_storage_usage)
        filesystemTitle = findViewById(R.id.os_storage_title)
        filesystemSubTitle = findViewById(R.id.os_storage_subtitle)
    }

    fun loadFilesystemData(data: FileSystemInfo.FileSystem) {
        val spaceUsedPercent = 100 - ceil((data.free.toDouble() / data.total.toDouble()) * 100).toInt()
        usedSpaceIndicator.progress = spaceUsedPercent
        filesystemTitle.text = data.mountPoint
        val totalSpaceInGB = String.format("%.2f GB",data.total.toDouble() / 1024.0 / 1024.0 / 1024.0)
        val freeSpaceInGB = String.format("%.2f GB",data.free.toDouble() / 1024.0 / 1024.0 / 1024.0)
        filesystemSubTitle.text = "$freeSpaceInGB left, $totalSpaceInGB total (${spaceUsedPercent}%)"
    }

}
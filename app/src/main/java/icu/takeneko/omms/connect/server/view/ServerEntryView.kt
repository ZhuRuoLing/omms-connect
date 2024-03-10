package icu.takeneko.omms.connect.server.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.GsonUtils
import icu.takeneko.omms.client.data.controller.Controller
import icu.takeneko.omms.client.data.system.SystemInfo
import icu.takeneko.omms.connect.R
import icu.takeneko.omms.connect.server.activity.minecraft.ui.MinecraftServerControlActivity
import icu.takeneko.omms.connect.server.activity.system.ServerOSControlActivity
import icu.takeneko.omms.connect.util.Assets
import icu.takeneko.omms.connect.util.ServerEntryType
import java.lang.Exception

class ServerEntryView : ConstraintLayout {

    private var imageView: ImageView
    private var serverNameTextView: TextView
    private var serverIntroTextView: TextView
    private var controller: Controller? = null
    private var systemInfo: SystemInfo? = null
    private var serverEntryType = ServerEntryType.UNDEFINED
    private var entryType = ""

    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.server_entry_view, this)
        this.imageView = findViewById(R.id.server_image)
        serverNameTextView = findViewById(R.id.server_name_text)
        serverIntroTextView = findViewById(R.id.server_intro_text)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.server_entry_view, this)
        this.imageView = findViewById(R.id.server_image)
        serverNameTextView = findViewById(R.id.server_name_text)
        serverIntroTextView = findViewById(R.id.server_intro_text)
    }

    fun setValue(name: String, introText: String, type: String, parent: Activity): ServerEntryView {
        try {
            this.serverNameTextView.text = name
            this.serverIntroTextView.text = introText
            this.entryType = type
            this.imageView.setImageDrawable(Assets.getServerIcon(type, parent))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this
    }

    fun withController(controller: Controller): ServerEntryView {

        this.controller = controller
        this.serverEntryType = ServerEntryType.MINECRAFT
        return this
    }

    fun withSystemInfo(systemInfo: SystemInfo): ServerEntryView {
        this.systemInfo = systemInfo
        serverEntryType = ServerEntryType.OS
        return this
    }

    fun prepare(parent: Fragment): ServerEntryView {
        this.setOnClickListener(
            if (serverEntryType == ServerEntryType.OS) OnClickListener {
                parent.startActivity(
                    Intent(
                        parent.activity,
                        ServerOSControlActivity::class.java
                    ).putExtra("data", GsonUtils.toJson(systemInfo))
                        .putExtra("system_type", entryType)
                )
            }
            else OnClickListener {
                parent.startActivity(
                    Intent(
                        parent.activity,
                        MinecraftServerControlActivity::class.java
                    ).putExtra("data", GsonUtils.toJson(controller))
                        .putExtra("server_type", entryType)
                )
            }
        )

        return this
    }


}
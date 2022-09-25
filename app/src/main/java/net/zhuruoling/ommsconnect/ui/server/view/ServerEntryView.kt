package net.zhuruoling.ommsconnect.ui.server.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import net.zhuruoling.omms.client.controller.Controller
import net.zhuruoling.omms.client.system.SystemInfo
import net.zhuruoling.ommsconnect.R
import net.zhuruoling.ommsconnect.ui.util.Assets
import java.lang.Exception

class ServerEntryView : ConstraintLayout {

    private var imageView: ImageView
    private var serverNameTextView: TextView
    private var serverIntroTextView: TextView
    private var controller: Controller? = null
    private var systemInfo: SystemInfo? = null

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
        try{
            this.serverNameTextView.text = name
            this.serverIntroTextView.text = introText
            this.imageView.setImageDrawable(Assets.getServerIcon(type, parent))
        }
        catch (e:Exception){
            e.printStackTrace()
        }
        return this
    }

    fun withController(controller: Controller): ServerEntryView{
        this.controller = controller
        return  this
    }

    fun withSystemInfo(systemInfo: SystemInfo): ServerEntryView{
        this.systemInfo = systemInfo
        return this
    }

}
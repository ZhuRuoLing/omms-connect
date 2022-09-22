package net.zhuruoling.ommsconnect.ui.server.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import net.zhuruoling.ommsconnect.R
import net.zhuruoling.ommsconnect.ui.util.Assets

class ServerEntryView : ConstraintLayout {

    private var imageView: ImageView
    private var serverNameTextView: TextView
    private var serverIntroTextView: TextView

    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.server_entry_view, this)
        this.imageView = getViewById(R.id.server_image) as ImageView
        serverNameTextView = getViewById(R.id.server_name_text) as TextView
        serverIntroTextView = getViewById(R.id.server_intro_text) as TextView
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.server_entry_view, this)
        this.imageView = getViewById(R.id.server_image) as ImageView
        serverNameTextView = getViewById(R.id.server_name_text) as TextView
        serverIntroTextView = getViewById(R.id.server_intro_text) as TextView
    }

    fun setValue(name: String, introText: String, type: String, parent: Activity) {
        this.serverNameTextView.text = name
        this.serverIntroTextView.text = introText
        this.imageView.setImageDrawable(Assets.getServerIcon(type, parent))

    }


}
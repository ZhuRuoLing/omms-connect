package icu.takeneko.omms.connect.settings

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import icu.takeneko.omms.connect.R

class ServerIconCardView : LinearLayout {
    private var iconIdText: TextView
    private var iconImage: ImageView
    private var deleteFab: FloatingActionButton

    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.controller_icon_card_view, this)
        iconIdText = findViewById(R.id.text_icon_id)
        iconImage = findViewById(R.id.image_controller_icon)
        deleteFab = findViewById(R.id.server_icon_delete_fab)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        deleteFab.setOnClickListener(l)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.controller_icon_card_view, this)
        iconIdText = findViewById(R.id.text_icon_id)
        iconImage = findViewById(R.id.image_controller_icon)
        deleteFab = findViewById(R.id.server_icon_delete_fab)
    }

    fun withDrawable(text: String, drawable: Drawable): ServerIconCardView {
        iconImage.setImageDrawable(drawable)
        iconIdText.text = text

        return this
    }

}
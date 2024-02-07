package icu.takeneko.omms.connect.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import icu.takeneko.omms.connect.R
import icu.takeneko.omms.connect.settings.UtilCommandEditActivity

class CommandTextCard: LinearLayout {
    private val button: MaterialButton
    private lateinit var activity: UtilCommandEditActivity
    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.command_text_card, this)
        button = findViewById(R.id.button_command_text)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.command_text_card, this)
        button = findViewById(R.id.button_command_text)
    }

    fun setValue(content: String, activity: UtilCommandEditActivity): CommandTextCard {
        button.text = content
        this.activity = activity
        button.setOnClickListener {
            activity.showCommandEditAction(false, button.text.toString())
        }
        return this
    }

}
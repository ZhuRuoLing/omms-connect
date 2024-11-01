package icu.takeneko.omms.connect.whitelist.view

import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.TextView
import android.view.LayoutInflater
import icu.takeneko.omms.connect.R
import com.blankj.utilcode.util.CacheMemoryUtils
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.activity.result.ActivityResultLauncher
import java.util.ArrayList

class WhitelistEntryView : ConstraintLayout {
    private var nameText: TextView
    private var introText: TextView
    private var name = ""
    private var content = emptyList<String>()
    private lateinit var activityResultLauncher: ActivityResultLauncher<Int>

    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.whitelist_entry_view, this)
        nameText = findViewById(R.id.whitelistNameText)
        introText = findViewById(R.id.whitelistIntroductionText)
        setOnClickListener { launchActivity() }
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.whitelist_entry_view, this)
        nameText = findViewById(R.id.whitelistNameText)
        introText = findViewById(R.id.whitelistIntroductionText)
        setOnClickListener { launchActivity() }
    }

    private fun launchActivity() {
        CacheMemoryUtils.getInstance().put("from_whitelist", nameText.text)
        CacheMemoryUtils.getInstance().put("whitelist_content", content)
        activityResultLauncher.launch(114514)
        //ActivityUtils.startActivity(Intent(this.context, WhitelistEditActivity::class.java))
    }

    @SuppressLint("DefaultLocale")
    fun setAttribute(
        context: Context,
        name: String,
        content: List<String>,
        activityResultLauncher: ActivityResultLauncher<Int>
    ): WhitelistEntryView {
        this.name = name
        this.content = content
        this.activityResultLauncher = activityResultLauncher
        nameText.text = this.name
        introText.text = context.getString(R.string.hint_whitelist_player_count, this.content.size)
        return this
    }
}
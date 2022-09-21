package net.zhuruoling.ommsconnect.ui.whitelist.view

import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.TextView
import android.view.LayoutInflater
import net.zhuruoling.ommsconnect.R
import com.blankj.utilcode.util.CacheMemoryUtils
import com.blankj.utilcode.util.ActivityUtils
import android.content.Intent
import net.zhuruoling.ommsconnect.WhitelistEditActivity
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import java.util.ArrayList

class WhitelistEntryView : ConstraintLayout {
    private var nameText: TextView
    private var introText: TextView
    private var name = ""
    private var content = ArrayList<String>()

    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.whitelist_entry_view, this)
        nameText = findViewById(R.id.whitelistNameText)
        introText = findViewById(R.id.whitelistIntroductionText)
        setOnClickListener { v: View? -> launchActivity() }
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.whitelist_entry_view, this)
        nameText = findViewById(R.id.whitelistNameText)
        introText = findViewById(R.id.whitelistIntroductionText)
        setOnClickListener { v: View? -> launchActivity() }
    }

    private fun launchActivity() {
        CacheMemoryUtils.getInstance().put("from_whitelist", nameText.text)
        CacheMemoryUtils.getInstance().put("whitelist_content", content)
        ActivityUtils.startActivity(Intent(this.context, WhitelistEditActivity::class.java))
    }

    @SuppressLint("DefaultLocale")
    fun setAttribute(name: String, content: ArrayList<String>): WhitelistEntryView {
        this.name = name
        this.content = content
        nameText.text = this.name
        introText.text = String.format("%d players", this.content.size)
        return this
    }
}
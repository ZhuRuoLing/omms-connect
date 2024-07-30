package icu.takeneko.omms.connect.announcement

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import icu.takeneko.omms.client.data.announcement.Announcement
import icu.takeneko.omms.connect.R


class AnnouncementEntryView : LinearLayout {
    private lateinit var titleText: TextView
    private lateinit var introText: TextView

    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.announcement_entry_view, this)
        this.titleText = findViewById(R.id.announcement_name_text)
        this.introText = findViewById(R.id.announcement_in_text)
    }

    fun withAnnouncement(context: Context,announcement: Announcement): AnnouncementEntryView {
        titleText.text = announcement.title
        val size = announcement.content.run {
            var size = 0
            this.forEach { size += it.length }
            size
        }
        introText.text = context.getString(R.string.label_announcement_brief, announcement.id, size)
        return this
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.command_text_card, this)
    }

}
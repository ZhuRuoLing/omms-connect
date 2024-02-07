package icu.takeneko.omms.connect.announcement

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import icu.takeneko.omms.connect.databinding.ActivityAnnouncementEditBinding

class AnnouncementEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnnouncementEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnnouncementEditBinding.inflate(layoutInflater)

    }
}
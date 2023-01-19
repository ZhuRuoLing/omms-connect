package net.zhuruoling.omms.connect

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.blankj.utilcode.util.ActivityUtils
import net.zhuruoling.omms.client.util.Util
import net.zhuruoling.omms.connect.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            this.finish()
        }
        binding.utilCommandButton.setOnClickListener {
            ActivityUtils.startActivity(UtilCommandEditActivity::class.java)
        }
    }

}
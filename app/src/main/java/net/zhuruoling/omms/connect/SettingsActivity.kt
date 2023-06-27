package net.zhuruoling.omms.connect

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.marginLeft
import androidx.core.view.marginStart
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ClipboardUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.zhuruoling.omms.connect.databinding.ActivitySettingsBinding
import net.zhuruoling.omms.connect.resource.ServerIconManageActivity
import net.zhuruoling.omms.connect.resource.ServerIconResourceManager
import net.zhuruoling.omms.connect.storage.PreferencesStorage
import net.zhuruoling.omms.connect.util.showErrorDialog
import net.zhuruoling.omms.connect.util.importDataFromJson
import net.zhuruoling.omms.connect.util.toExportDataJson

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.autoRoll.isChecked = PreferencesStorage.withContext(this, "console")
            .getBoolean("autoRoll", true)
        binding.autoRoll.setOnCheckedChangeListener { _, isChecked ->
            PreferencesStorage.withContext(this, "console")
                .putBoolean("autoRoll", isChecked).commit()
        }
        binding.textSize.setText(
            PreferencesStorage.withContext(this, "console").getFloat("textSize", 10f).toString()
        )
        binding.textSize.addTextChangedListener {
            if (it.isNullOrEmpty()) return@addTextChangedListener
            val size = it.toString().toFloat()
            PreferencesStorage.withContext(this, "console")
                .putFloat("textSize", if (size < 1) 1f else size).commit()
        }
        binding.toolbar.setNavigationOnClickListener {
            this.finish()
        }
        binding.utilCommandButton.setOnClickListener {
            ActivityUtils.startActivity(UtilCommandEditActivity::class.java)
        }
        binding.exportData.setOnClickListener {
            val content = toExportDataJson(this)
            ClipboardUtils.copyText(content)
            Snackbar.make(this, this.binding.root, "Copied to Clipboard!", Snackbar.LENGTH_LONG)
                .show()
        }
        binding.addServerIcon.setOnClickListener {
            ActivityUtils.startActivity(ServerIconManageActivity::class.java)
        }

        binding.importData.setOnClickListener {
            val textView = TextInputEditText(this)
            val dialog = MaterialAlertDialogBuilder(this)
                .setView(textView)
                .setCancelable(true)
                .setTitle("Import Data")
                .setPositiveButton("Done") { _, _ ->
                    Log.i("omms-crystal", "Back ${textView.text}")
                    try {
                        importDataFromJson(this, textView.text.toString())
                        Snackbar.make(
                            this,
                            this.binding.root,
                            "Done! App will restart.",
                            Snackbar.LENGTH_LONG
                        ).show()
                        val intent =
                            packageManager.getLaunchIntentForPackage(application.packageName)
                        val restartIntent =
                            PendingIntent.getActivity(applicationContext, 0, intent, FLAG_IMMUTABLE)
                        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        alarmManager.set(
                            AlarmManager.RTC,
                            System.currentTimeMillis() + 1000,
                            restartIntent
                        )
                        android.os.Process.killProcess(android.os.Process.myPid())
                    } catch (e: Exception) {
                        showErrorDialog("Exception occurred while parsing data. $e", this)
                    }
                }
                .setNegativeButton("Cancel") { _, _ ->
                    Log.i("omms-crystal", "Back ${textView.text}")
                }
            dialog.show()
        }
    }

}
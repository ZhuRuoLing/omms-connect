package net.zhuruoling.omms.connect

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ClipboardUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import net.zhuruoling.omms.connect.databinding.ActivitySettingsBinding
import net.zhuruoling.omms.connect.ui.util.showErrorDialog
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
        binding.toolbar.setNavigationOnClickListener {
            this.finish()
        }
        binding.utilCommandButton.setOnClickListener {
            ActivityUtils.startActivity(UtilCommandEditActivity::class.java)
        }
        binding.exportData.setOnClickListener {
            val content = toExportDataJson(this)
            ClipboardUtils.copyText(content)
            Snackbar.make(this, this.binding.root, "Copied to Clipboard!", Snackbar.LENGTH_LONG).show()
        }
        binding.importData.setOnClickListener {
            val textView = TextInputEditText(this)
            val dialog = MaterialAlertDialogBuilder(this)
                .setView(textView)
                .setCancelable(true)
                .setTitle("Import Data")
                .setPositiveButton("Done") { _, _ ->
                    Log.i("omms-crystal", "Back ${textView.text}")
                    try{
                        importDataFromJson(this, textView.text.toString())
                        Snackbar.make(this, this.binding.root, "Done! App will restart.", Snackbar.LENGTH_LONG).show()
                        val intent = packageManager.getLaunchIntentForPackage(application.packageName)
                        val restartIntent = PendingIntent.getActivity(applicationContext, 0, intent, FLAG_IMMUTABLE)
                        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis()+1000, restartIntent)
                        android.os.Process.killProcess(android.os.Process.myPid())
                    }catch (e: Exception){
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
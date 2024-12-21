package icu.takeneko.omms.connect

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import icu.takeneko.omms.connect.client.Connection
import icu.takeneko.omms.connect.client.Connection.Result
import icu.takeneko.omms.connect.client.ConnectionStatus
import icu.takeneko.omms.connect.databinding.ActivityMainBinding
import icu.takeneko.omms.connect.resource.ServerIconResourceManager
import icu.takeneko.omms.connect.settings.SettingsActivity
import icu.takeneko.omms.connect.storage.PreferencesStorage
import icu.takeneko.omms.connect.util.toErrorMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
        alertDialog.dismiss()
    }

    private val externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    private lateinit var alertDialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        ServerIconResourceManager.load(this)
        alertDialog = MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setTitle("Loading")
            .setMessage(R.string.working)
            .create()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val button = binding.buttonLogin
        binding.remeberCodeCheckbox.isEnabled = false
        binding.remeberServerCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.remeberCodeCheckbox.isEnabled = isChecked
            if (!isChecked and binding.remeberCodeCheckbox.isChecked) {
                binding.remeberCodeCheckbox.isChecked = false
            }
        }
        val preferencesStorage = PreferencesStorage.withContext(this, "login")
        button.setOnClickListener {
            val ip: String = binding.textIpaddr.text.toString()
            val port = binding.textPort.text.toString()
            val code = binding.textCode.text.toString()
            if (ip.isEmpty() or port.isEmpty() or code.isEmpty()) {
                ToastUtils.showLong(R.string.empty)
                return@setOnClickListener
            }
            if (binding.remeberServerCheckbox.isChecked) {
                preferencesStorage.putString("server_ip", ip).putString("server_port", port)
            } else {
                preferencesStorage.clear()
            }
            if (binding.remeberCodeCheckbox.isChecked) {
                preferencesStorage.putString("server_code", code)
            }

            preferencesStorage.commit()
            login(ip, Integer.valueOf(port), code)
            alertDialog.show()
        }
        if (preferencesStorage.contains("server_ip") and preferencesStorage.contains("server_port")) {
            val ip = preferencesStorage.getString("server_ip", "")
            val port = preferencesStorage.getString("server_port", "")
            binding.textIpaddr.text = SpannableStringBuilder(ip)
            binding.textPort.text = SpannableStringBuilder(port)
            binding.remeberServerCheckbox.isChecked = true
        }
        if (preferencesStorage.contains("server_code")) {
            val code = preferencesStorage.getString("server_code", "")
            binding.textCode.text = SpannableStringBuilder(code)
            binding.remeberCodeCheckbox.isChecked = true
        }
        binding.settingButton.setOnClickListener {
            ActivityUtils.startActivity(SettingsActivity::class.java)
        }
    }

    private fun login(ip: String, port: Int, code: String) {
        externalScope.launch(defaultDispatcher) {
            ensureActive()
            when (val result = Connection.connect(ip, port, code, true)) {
                is Result.Success<ConnectionStatus> -> {
                    ToastUtils.showLong(R.string.hint_connected)
                    startActivity(Intent(this@MainActivity, SessionActivity::class.java))
                    runOnUiThread {
                        alertDialog.dismiss()
                    }
                }
                is Result.Error -> {
                    val error = result.exception
                    runOnUiThread {
                        alertDialog.dismiss()
                        val dialog = MaterialAlertDialogBuilder(this@MainActivity)
                            .setCancelable(true)
                            .setTitle(R.string.error_exception_connect)
                            .setMessage(this@MainActivity.toErrorMessage(error))
                            .setIcon(R.drawable.ic_baseline_error_24)
                            .create()
                        dialog.show()
                    }
                }
            }
        }
    }
}
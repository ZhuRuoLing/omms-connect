package net.zhuruoling.ommsconnect

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import net.zhuruoling.ommsconnect.client.Connection
import net.zhuruoling.ommsconnect.client.Response
import net.zhuruoling.ommsconnect.client.Connection.Result
import net.zhuruoling.ommsconnect.databinding.ActivityMainBinding

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
        alertDialog = MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setTitle("Loading")
            .setMessage(R.string.working)
            .create()
        val sharedPreferences:SharedPreferences = getSharedPreferences("server",
            MODE_PRIVATE)
        val editor:SharedPreferences.Editor = sharedPreferences.edit()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val button = binding.buttonLogin
        binding.remeberCodeCheckbox.isClickable = false
        binding.remeberServerCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.remeberCodeCheckbox.isClickable = isChecked
            if (!isChecked and binding.remeberCodeCheckbox.isChecked){
                binding.remeberCodeCheckbox.isChecked = false
            }
        }
        button.setOnClickListener {
            val ip: String = binding.textIpaddr.text.toString()
            val port = binding.textPort.text.toString()
            val code = binding.textCode.text.toString()
            if (ip.isEmpty() or port.isEmpty() or code.isEmpty()) {
                ToastUtils.showLong(R.string.empty)
                return@setOnClickListener
            }


            if (binding.remeberServerCheckbox.isChecked){
                editor.putString("server_ip",ip)
                editor.putString("server_port",port)
            }
            else{
                editor.clear()
            }
            if (binding.remeberCodeCheckbox.isChecked){
                editor.putString("server_code",code)
            }

            editor.apply()
            login(ip, Integer.valueOf(port), Integer.valueOf(code))
            alertDialog.show()
            }
        if (sharedPreferences.contains("server_ip") and sharedPreferences.contains("server_port")){
            val ip = sharedPreferences.getString("server_ip","")
            val port = sharedPreferences.getString("server_port","")
            binding.textIpaddr.text = SpannableStringBuilder(ip)
            binding.textPort.text = SpannableStringBuilder(port)
            binding.remeberServerCheckbox.isChecked = true
        }
        if (sharedPreferences.contains("server_code")){
            val code = sharedPreferences.getString("server_code","")
            binding.textCode.text = SpannableStringBuilder(code)
            binding.remeberCodeCheckbox.isChecked = true
        }

    }

    private fun login(ip: String, port: Int, code: Int) {
        externalScope.launch(defaultDispatcher) {
            ensureActive()
            when (val result = Connection.init(ip, port, code)) {
                is Result.Success<Response> -> {
                    ToastUtils.showLong(R.string.success)
                    startActivity(Intent(this@MainActivity,SessionActivity::class.java))
                    runOnUiThread {
                        alertDialog.dismiss()
                    }
                }
                else -> {
                    runOnUiThread {
                        alertDialog.dismiss()
                        val dialog = MaterialAlertDialogBuilder(this@MainActivity)
                            .setCancelable(true)
                            .setTitle("Loading")
                            .setMessage(String.format("Cannot connect to server, reason %s", (result as Result.Error).exception.toString()))
                            .create()
                        dialog.show()
                        ToastUtils.showLong(R.string.fail)
                    }

                }
            }
        }
    }
}
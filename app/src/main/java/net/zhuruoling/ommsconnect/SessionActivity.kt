package net.zhuruoling.ommsconnect

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import kotlinx.coroutines.*
import net.zhuruoling.ommsconnect.client.Connection
import net.zhuruoling.ommsconnect.client.Response
import net.zhuruoling.ommsconnect.databinding.ActivitySessionBinding

class SessionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySessionBinding
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
    }

    private val externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivitySessionBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStop() {
        if (!ActivityUtils.isActivityAlive(this)){
            end()
        }
        super.onStop()
    }

    override fun onDestroy() {
        end()
        super.onDestroy()
    }

    private fun end() {
        externalScope.launch(defaultDispatcher) {
            ensureActive()
            when (Connection.end()) {
                is Connection.Result.Success<Response> -> {
                    ToastUtils.showLong("Disconnected.")
                }
                else -> {
                    throw RuntimeException("Failed to end connection.")
                }
            }
        }
    }
}
package icu.takeneko.omms.connect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import icu.takeneko.omms.connect.client.Connection
import icu.takeneko.omms.connect.databinding.ActivitySession0Binding
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class SessionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySession0Binding
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
    }
    private val externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySession0Binding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_session0)
        navView.setupWithNavController(navController)
    }

    override fun onStop() {
        if (!ActivityUtils.isActivityAlive(this)) {
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
            Connection.end()
        }
    }

}
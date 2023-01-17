package net.zhuruoling.omms.connect

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import kotlinx.coroutines.*
import net.zhuruoling.omms.connect.client.Connection
import net.zhuruoling.ommsconnect.R
import net.zhuruoling.omms.connect.client.Response
import net.zhuruoling.ommsconnect.databinding.ActivitySession0Binding

class SessionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySession0Binding
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
    }
    val externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)
    val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySession0Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_session0)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_whitelist, R.id.announcement
            )
        )
        //setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
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
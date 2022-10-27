package net.zhuruoling.ommsconnect.ui.server

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import net.zhuruoling.omms.client.controller.Controller
import net.zhuruoling.omms.client.system.SystemInfo
import net.zhuruoling.omms.client.util.Result
import net.zhuruoling.ommsconnect.client.Connection
import net.zhuruoling.ommsconnect.databinding.FragmentServerBinding
import net.zhuruoling.ommsconnect.ui.server.view.ServerEntryView
import net.zhuruoling.ommsconnect.ui.util.genControllerIntroText
import net.zhuruoling.ommsconnect.ui.util.getSystemType
import net.zhuruoling.ommsconnect.ui.view.Placeholder68dpView

class ServerFragment : Fragment() {

    private var _binding: FragmentServerBinding? = null
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
    }
    val externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)
    val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val serverViewModel =
            ViewModelProvider(this).get(ServerViewModel::class.java)

        _binding = FragmentServerBinding.inflate(inflater, container, false)

        if (Connection.isConnected) {
            tryInit()
        }
        return binding.root
    }

    private fun tryInit() {

        var controllers = hashMapOf<String, Controller>()
        var systemInfo: SystemInfo? = null
        val alertDialog = this.context?.let {
            MaterialAlertDialogBuilder(it)
                .setCancelable(false)
                .setTitle("Loading")
                .setMessage("Please Wait...")
                .create()
        }
        externalScope.launch(Dispatchers.IO) {
            launch(Dispatchers.Main) {
                alertDialog?.show()
            }
            ensureActive()
            Connection.getClientSession().apply {
                var result = this.fetchCotrollersFromServer()
                if (result == Result.OK) {
                    controllers = this.controllerMap
                    this@ServerFragment.externalScope.launch(Dispatchers.Main) {
                        this@ServerFragment.binding.serverText.text =
                            "${controllers.count()} controllers added to this server."
                    }
                } else {
                    ToastUtils.showLong("Cannot fetch Controllers from server, Caused By: ${result.name}")
                }
                result = this.fetchSystemInfoFromServer()
                if (result == Result.OK) {
                    systemInfo = this.systemInfo
                } else {
                    ToastUtils.showLong("Cannot fetch systemInfo from server, Caused By: ${result.name}")
                }
            }
            launch(Dispatchers.Main) {
                try {
                    context?.let { it1 ->
                        val view = activity?.let { fragmentActivity ->
                            systemInfo.let { systemInfo ->
                                systemInfo?.let { systemInfo1 ->
                                    ServerEntryView(it1).setValue(
                                        name = "Operating System",
                                        introText = "${systemInfo.osName} ${systemInfo.osVersion} ${systemInfo.osArch}",
                                        type = getSystemType(systemInfo.osName),
                                        parent = fragmentActivity
                                    )
                                        .withSystemInfo(systemInfo1)
                                        .prepare(this@ServerFragment)
                                }
                            }
                        }
                        this@ServerFragment.binding.serverList.addView(view)
                    }

                    controllers.forEach {
                        var introText = genControllerIntroText(it.value)
                        context?.let { it1 ->
                            val view = activity?.let { it2 ->
                                ServerEntryView(it1).setValue(
                                    it.value.name, introText, it.value.type, it2
                                )
                                    .withController(it.value)
                                    .prepare(this@ServerFragment)
                            }
                            this@ServerFragment.binding.serverList.addView(view)
                        }
                    }

                    this@ServerFragment.binding.serverList.addView(this@ServerFragment.context?.let {
                        Placeholder68dpView(
                            it
                        )
                    })
                    alertDialog?.dismiss()
                } catch (e: Exception) {
                    Log.e("omms", "err", e)
                    alertDialog?.dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}












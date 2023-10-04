package net.zhuruoling.omms.connect.server

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import net.zhuruoling.omms.client.controller.Controller
import net.zhuruoling.omms.client.system.SystemInfo
import net.zhuruoling.omms.connect.R
import net.zhuruoling.omms.connect.client.Connection
import net.zhuruoling.omms.connect.databinding.FragmentServerBinding
import net.zhuruoling.omms.connect.server.view.ServerEntryView
import net.zhuruoling.omms.connect.util.formatResString
import net.zhuruoling.omms.connect.util.genControllerText
import net.zhuruoling.omms.connect.util.getSystemType
import net.zhuruoling.omms.connect.util.showErrorDialog
import net.zhuruoling.omms.connect.view.Placeholder68dpView
import net.zhuruoling.omms.connect.util.awaitExecute

class ServerFragment : Fragment() {

    private var _binding: FragmentServerBinding? = null
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
        Log.e("OMMS", "Unexpected exception in coroutines.", e)
    }
    private val externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServerBinding.inflate(inflater, container, false)
        if (Connection.isConnected) {
            load()
        }
        binding.serverSwipeRefresh.isRefreshing = false
        binding.serverSwipeRefresh.setOnRefreshListener {
            if (Connection.isConnected) {
                load(false)
            }
        }
        return binding.root
    }

    private fun load(showDialog: Boolean = true) {
        var hasController = false
        var hasSystemInfo = false
        var controllers = mutableMapOf<String, Controller>()
        var systemInfo: SystemInfo? = null
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setTitle(R.string.label_loading)
            .setMessage(R.string.label_wait)
            .create()

        externalScope.launch(Dispatchers.IO) {
            if (showDialog) {
                launch(Dispatchers.Main) {
                    alertDialog.show()
                }
            }
            ensureActive()
            Connection.getClientSession().apply {
                try {
                    ensureActive()
                    try {
                        awaitExecute {
                            this.fetchControllersFromServer { map ->
                                controllers = map
                                awaitExecute { latch ->
                                    this@launch.launch(Dispatchers.Main) {
                                        this@ServerFragment.binding.serverText.text =
                                            formatResString(
                                                R.string.label_controller_count,
                                                controllers.count(),
                                                context = requireContext()
                                            )
                                        latch.countDown()
                                    }

                                }
                                hasController = true
                                it.countDown()
                            }
                        }
                    } catch (e: java.lang.Exception) {
                        Log.e("OMMS", "wtf", e)
                        hasController = false
                        launch(Dispatchers.Main) {
                            if (showDialog) {
                                alertDialog.dismiss()
                            } else {
                                this@ServerFragment.binding.serverSwipeRefresh.isRefreshing = false
                            }
                            showErrorDialog(
                                formatResString(
                                    R.string.error_controller_fetch_error,
                                    e.message,
                                    context =  requireContext()
                                ),
                                this@ServerFragment.requireContext()
                            )
                        }
                    }
                    awaitExecute{latch ->
                        try{
                            this.fetchSystemInfoFromServer {
                                systemInfo = it
                                hasSystemInfo = true
                                latch.countDown()
                            }
                        }catch (e:java.lang.Exception){
                            hasSystemInfo = false
                            launch(Dispatchers.Main) {
                                if (showDialog) {
                                    alertDialog.dismiss()
                                } else {
                                    this@ServerFragment.binding.serverSwipeRefresh.isRefreshing = false
                                }
                                showErrorDialog(
                                    formatResString(
                                        R.string.error_system_info_fetch_error,
                                        e.message,
                                        context = requireContext()
                                    ),
                                    this@ServerFragment.requireContext()
                                )
                            }
                            latch.countDown()
                        }
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        showErrorDialog(e.toString(), this@ServerFragment.requireContext())
                    }
                    return@launch
                }
            }
            launch(Dispatchers.Main) {
                this@ServerFragment.binding.serverList.removeAllViews()
                try {
                    if (hasSystemInfo) {
                        val osEntryView = ServerEntryView(requireContext()).setValue(
                            name = getString(R.string.label_server_os),
                            introText = "${systemInfo!!.osName} ${systemInfo!!.osVersion} ${systemInfo!!.osArch}",
                            type = getSystemType(systemInfo!!.osName),
                            parent = requireActivity()
                        ).withSystemInfo(systemInfo!!).prepare(this@ServerFragment)
                        this@ServerFragment.binding.serverList.addView(osEntryView)
                    }
                    if (hasController) {
                        controllers.forEach {
                            val text = genControllerText(it.value)
                            val controllerEntryView =
                                ServerEntryView(this@ServerFragment.requireContext()).setValue(
                                    it.value.displayName, text, it.value.type ?: "", requireActivity()
                                ).withController(it.value).prepare(this@ServerFragment)
                            this@ServerFragment.binding.serverList.addView(controllerEntryView)
                        }
                    }

                    this@ServerFragment.binding.serverList.addView(Placeholder68dpView(this@ServerFragment.requireContext()))
                    if (showDialog) {
                        alertDialog.dismiss()
                    } else {
                        this@ServerFragment.binding.serverSwipeRefresh.isRefreshing = false
                    }
                } catch (e: Exception) {
                    showErrorDialog(
                        formatResString(
                            R.string.error_unknown_error,
                            e.toString(),
                            context = requireContext()
                        ), this@ServerFragment.requireContext()
                    )
                    Log.e("omms", "Exception at ServerFragment", e)
                    if (showDialog) {
                        alertDialog.dismiss()
                    } else {
                        this@ServerFragment.binding.serverSwipeRefresh.isRefreshing = false
                    }

                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}












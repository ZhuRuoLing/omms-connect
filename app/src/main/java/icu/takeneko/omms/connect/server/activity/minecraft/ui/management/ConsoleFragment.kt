package icu.takeneko.omms.connect.server.activity.minecraft.ui.management

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.text.PrecomputedTextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import icu.takeneko.omms.client.data.controller.Controller
import icu.takeneko.omms.client.exception.ConsoleExistsException
import icu.takeneko.omms.client.session.ControllerConsoleClient
import icu.takeneko.omms.connect.R
import icu.takeneko.omms.connect.client.Connection
import icu.takeneko.omms.connect.databinding.FragmentMcConsoleBinding
import icu.takeneko.omms.connect.server.activity.minecraft.ui.ConsoleWorker
import icu.takeneko.omms.connect.settings.SettingsActivity
import icu.takeneko.omms.connect.storage.PreferencesStorage
import icu.takeneko.omms.connect.util.fromJson
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus


class ConsoleFragment : Fragment(), ControllerConsoleClient {

    private var _binding: FragmentMcConsoleBinding? = null
    private val binding get() = _binding!!
    private var consoleConnected = false
    private lateinit var controller: Controller
    private var autoRoll = false
    private var consoleTextSize = 12.00f
    private var consoleId = ""
    private lateinit var consoleWorker: ConsoleWorker
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
    }
    private val externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMcConsoleBinding.inflate(inflater, container, false)
        binding.consoleConnectStateButton.setOnClickListener {
            consoleConnected = if (consoleConnected) {
                binding.consoleConnectStateButton.isEnabled = false
                disconnect {
                    setButtonState(false)
                }
                false
            } else {
                binding.consoleConnectStateButton.isEnabled = false
                connect {
                    setButtonState(true)
                    consoleWorker.clear()
                    externalScope.launch {
                        this@ConsoleFragment.binding.mcOutputText.text = ""
                    }
                }
                true
            }
        }
        binding.send.setOnClickListener {
            if (consoleConnected) {
                val line = binding.consoleCommandText.text.toString()
                consoleInput(line)
                binding.consoleCommandText.setText("")
            }
        }
        binding.more.setOnClickListener {
            ActivityUtils.startActivity(SettingsActivity::class.java)
        }
        binding.send.setOnLongClickListener {
            consoleWorker.dumpLogs()
            true
        }
        val data = requireActivity().intent.getStringExtra("data")!!
        controller = fromJson(data, Controller::class.java)
        autoRoll = PreferencesStorage.withContext(requireContext(), "console")
            .getBoolean("autoRoll", true)
        consoleTextSize =
            PreferencesStorage.withContext(requireContext(), "console")
                .getFloat("textSize", 10f)
        binding.mcOutputText.textSize = consoleTextSize
        consoleWorker = ConsoleWorker(this)
        consoleWorker.start()
        return binding.root
    }

    fun retrieveTextMetricsParams() =
        TextViewCompat.getTextMetricsParams(binding.mcOutputText)


    fun displayLog(precomputedTextCompat: PrecomputedTextCompat) {
        lifecycleScope.launch(Dispatchers.Main) {
            TextViewCompat.setPrecomputedText(binding.mcOutputText, precomputedTextCompat)
            if (autoRoll) {
                scrollToEnd()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        autoRoll = PreferencesStorage.withContext(requireContext(), "console")
            .getBoolean("autoRoll", true)
        consoleTextSize =
            PreferencesStorage.withContext(requireContext(), "console")
                .getFloat("textSize", 10f)
        binding.mcOutputText.textSize = consoleTextSize
    }

    override fun onDestroyView() {
        super.onDestroyView()
        consoleWorker.shutdown()
        if (consoleConnected) disconnect { }
        _binding = null
    }

    private fun setButtonState(state: Boolean) {
        externalScope.launch(Dispatchers.Main) {
            val button = binding.consoleConnectStateButton
            if (state) {//connected
                button.setIconResource(R.drawable.baseline_link_off_24)
                button.setText(R.string.label_disconnect_console)
            } else {//disconnected
                button.setIconResource(R.drawable.baseline_link_24)
                button.setText(R.string.label_connect_console)
            }
            binding.consoleConnectStateButton.isEnabled = true
        }
    }

    private fun connect(callback: () -> Unit) {
        if (Connection.getClientSession().isActive) {
            externalScope.launch(Dispatchers.IO) {
                Connection.getClientSession().onPermissionDeniedCallback.setCallback {
                    binding.mcOutputText.setText(R.string.error_permission_denied)
                    Connection.getClientSession().onPermissionDeniedCallback.setCallback { }
                }
                Connection.getClientSession()
                    .startControllerConsole(controller.name, this@ConsoleFragment)
                    .whenComplete { t, u ->
                        when (u) {
                            is ConsoleExistsException -> {
                                consoleWorker.append(getString(R.string.hint_console_exists))
                            }
                            null -> {
                                consoleId = t
                                callback()
                            }
                        }
                    }
            }
        }

    }

    private fun disconnect(callback: () -> Unit) {
        if (consoleId.isEmpty()) return
        externalScope.launch(Dispatchers.IO) {
            Connection.getClientSession().stopControllerConsole(consoleId)
                .whenComplete { t, u ->
                    if (u != null) {
                        binding.mcOutputText.setText(R.string.hint_console_not_exist)
                    } else {
                        this@ConsoleFragment.print(getString(R.string.hint_console_stopped))
                    }
                    callback()
                }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun print(line: String) {
        externalScope.launch(Dispatchers.IO) {
            consoleWorker.append(line)
        }
    }

    private fun consoleInput(line: String) {
        externalScope.launch(Dispatchers.IO) {
            Connection.getClientSession().controllerConsoleInput(consoleId, line)
                .whenComplete { _, u ->
                    if (u != null) {
                        binding.mcOutputText.setText(R.string.hint_console_not_exist)
                    } else {
                        print("> $line")
                    }
                }
            scrollToEnd()
        }
    }

    private fun scrollToEnd() {
        externalScope.launch(Dispatchers.Main) {
            binding.scroll.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    override fun onLaunched(controllerId: String, consoleId: String) {

    }

    override fun onLogReceived(consoleId: String, log: String) {
        print(log)
    }

    override fun onStopped(consoleId: String) {

    }
}
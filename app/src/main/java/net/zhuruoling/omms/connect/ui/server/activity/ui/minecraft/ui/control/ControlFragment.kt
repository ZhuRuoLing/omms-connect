package net.zhuruoling.omms.connect.ui.server.activity.ui.minecraft.ui.control

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.text.toUpperCase
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.CacheMemoryUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import net.zhuruoling.omms.client.controller.Controller
import net.zhuruoling.omms.client.controller.ControllerTypes
import net.zhuruoling.omms.client.controller.Status
import net.zhuruoling.omms.connect.client.Connection
import net.zhuruoling.omms.connect.databinding.FragmentMcControlBinding
import net.zhuruoling.omms.connect.ui.util.fromJson
import net.zhuruoling.omms.connect.ui.util.getUtilCommands
import net.zhuruoling.omms.connect.ui.util.showErrorDialog
import net.zhuruoling.omms.connect.ui.util.toJson
import java.util.*

class ControlFragment : Fragment() {

    private var _binding: FragmentMcControlBinding? = null
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
    }
    private val externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var controller: Controller
    private lateinit var status: Status
    var statusReady = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMcControlBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val data = requireActivity().intent.getStringExtra("data")!!
        controller = fromJson(data, Controller::class.java)
        val defaultStatus = Status(
            ControllerTypes.valueOf(controller.type.uppercase(Locale.getDefault())),
            0,
            0,
            mutableListOf()
        )
        defaultStatus.isAlive = false
        defaultStatus.isQueryable = false
        status = fromJson(
            CacheMemoryUtils.getInstance().get("${controller.name}:status", toJson(defaultStatus)),
            Status::class.java
        )
        this.binding.mcCommandSend.setOnClickListener {
            val command = binding.mcCommandText.text ?: ""
            if (command.isEmpty()) {
                return@setOnClickListener
            }
            sendCommandForFeedback(command.toString())
        }
        this.binding.mcCommandSelect.setOnClickListener {
            val utilCommands = getUtilCommands(requireContext())
                .toTypedArray()
                .ifEmpty {
                    val alertDialog = MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Select Command")
                        .setMessage("No Util Command")
                        .setPositiveButton("OK", null)
                    return@setOnClickListener
                }
            var index = -1
            val alertDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Command")
                .setSingleChoiceItems(utilCommands, -1) { _, i ->
                    index = i
                }
                .setPositiveButton("OK") { _, _ ->
                    if (index == -1)
                        return@setPositiveButton
                    this@ControlFragment.binding.mcCommandText.text = Editable.Factory.getInstance().newEditable(utilCommands[index])
                }
                .setNegativeButton("Back") { _, _ ->

                }
            alertDialog.show()
        }
        return root
    }

    private fun sendCommandForFeedback(command: String) {
        binding.mcOutputText.text = "> "
        if (!status.isAlive) {
            showErrorDialog("This Controller is NOT Running.", requireContext())
            return
        }
        if (!Connection.isConnected) {
            showErrorDialog("Disconnected from server.", requireContext())
            return
        }
        externalScope.launch(Dispatchers.IO) {
            launch(Dispatchers.Main) {
                binding.mcOutputText.text = binding.mcOutputText.text.toString() + command + "\n"
                binding.mcOutputText.text =
                    binding.mcOutputText.text.toString() + "[Waiting For Response: "
            }
            val ret = Connection.getClientSession()
                .sendCommandToController(this@ControlFragment.controller.name, command)
            launch(Dispatchers.Main) {
                binding.mcOutputText.text =
                    binding.mcOutputText.text.toString() + ret.a.name + "]\n"
                binding.mcOutputText.text = binding.mcOutputText.text.toString() + ret.b + "\n"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

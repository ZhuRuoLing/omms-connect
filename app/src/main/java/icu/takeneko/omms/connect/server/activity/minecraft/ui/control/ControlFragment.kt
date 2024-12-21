package icu.takeneko.omms.connect.server.activity.minecraft.ui.control

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.CacheMemoryUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import icu.takeneko.omms.client.data.controller.Controller
import icu.takeneko.omms.client.data.controller.Status
import icu.takeneko.omms.client.exception.ControllerNotFoundException
import icu.takeneko.omms.client.exception.RequestUnauthorisedException
import icu.takeneko.omms.connect.R
import icu.takeneko.omms.connect.client.Connection
import icu.takeneko.omms.connect.databinding.FragmentMcControlBinding
import icu.takeneko.omms.connect.util.format
import icu.takeneko.omms.connect.util.fromJson
import icu.takeneko.omms.connect.util.getUtilCommands
import icu.takeneko.omms.connect.util.showErrorDialog
import icu.takeneko.omms.connect.util.toJson
import kotlinx.coroutines.*

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
            controller.type,
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
                    this@ControlFragment.binding.mcCommandText.text =
                        Editable.Factory.getInstance().newEditable(utilCommands[index])
                }
                .setNegativeButton("Back") { _, _ ->

                }
            alertDialog.show()
        }
        return root
    }

    @SuppressLint("SetTextI18n")
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
        binding.mcOutputText.text = binding.mcOutputText.text.toString() + command + "\n"
        binding.mcOutputText.text = binding.mcOutputText.text.toString() + "[Waiting For Response]\n"
        externalScope.launch(Dispatchers.IO) {
            Connection.getClientSession().onPermissionDeniedCallback.setCallback {
                binding.mcOutputText.text =
                    requireContext().getText(R.string.error_permission_denied)
            }
            Connection.getClientSession().sendCommandToController(controller.name, command)
                .whenComplete { t, u ->
                    when (u) {
                        null -> lifecycleScope.launch(Dispatchers.Main) {
                            binding.mcOutputText.text = binding.mcOutputText.text.toString() +
                                    t.joinToString("\n") +
                                    "\n"
                        }

                        is ControllerNotFoundException -> lifecycleScope.launch(Dispatchers.Main) {
                            binding.mcOutputText.text.toString() + "\n" + format(
                                R.string.error_controller_not_exist, controller.name
                            )
                        }

                        is RequestUnauthorisedException -> lifecycleScope.launch(Dispatchers.Main) {
                            binding.mcOutputText.text =
                                requireContext().getText(R.string.error_server_controller_auth_error)
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

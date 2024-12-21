package icu.takeneko.omms.connect.server.activity.minecraft.ui.status

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.Icon
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.CacheMemoryUtils
import com.blankj.utilcode.util.ToastUtils
import icu.takeneko.omms.client.data.controller.Controller
import icu.takeneko.omms.client.data.controller.Status
import icu.takeneko.omms.connect.R
import icu.takeneko.omms.connect.client.Connection
import icu.takeneko.omms.connect.databinding.FragmentMcStatusBinding
import icu.takeneko.omms.connect.util.fromJson
import icu.takeneko.omms.connect.util.showErrorDialog
import icu.takeneko.omms.connect.util.toJson
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus


class StatusFragment : Fragment() {

    private var _binding: FragmentMcStatusBinding? = null
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
    }
    private val externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)
    private val binding get() = _binding!!
    private lateinit var controller: Controller
    private lateinit var status: Status

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMcStatusBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val data = requireActivity().intent.getStringExtra("data")!!
        controller = fromJson(data, Controller::class.java)
        getStatus()
        binding.mcStatusSwipeRefresh.setOnRefreshListener {
            getStatus()
        }
        return root
    }

    @SuppressLint("SetTextI18n")
    private fun getStatus() {
        if (!Connection.isConnected) {
            showErrorDialog("Disconnected from server.", requireContext())
            return
        }
        externalScope.launch(Dispatchers.IO) {
            showLoadDialog()
            try {
                val status = Connection.getClientSession().fetchControllerStatus(controller.name).get()
                launch(Dispatchers.Main) {
                    this@StatusFragment.status = status
                    putStatusToCache()
                    binding.statusIcon.setImageIcon(
                        Icon.createWithResource(
                            requireContext(),
                            getStatusIconId(status)
                        )
                    )
                    binding.statusCard.setCardBackgroundColor(
                        ColorStateList.valueOf(
                            getStatusColor(status)
                        ).withAlpha(0xff)
                    )
                    binding.mcStatusTitle.setText(if (status.isQueryable)
                        if (status.isAlive)
                            R.string.label_state_running
                        else
                            R.string.label_state_stopped
                    else R.string.label_state_not_queryable)

                    if (status.isQueryable and status.isAlive) {
                        binding.mcTextPlayerCount.text =
                            "${status.playerCount}/${status.maxPlayerCount}"
                        binding.mcTextPlayerList.text =
                            if (status.players.isEmpty()) requireContext().getText(R.string.label_no_player) else status.players.joinToString(
                                separator = "\n"
                            )
                    } else {
                        binding.mcTextPlayerCount.setText(R.string.unavailable)
                        binding.mcTextPlayerList.setText(R.string.unavailable)
                    }
                    dismissLoadAnim()
                }

            } catch (e: Exception) {
                showAlertAndDismissDialog("Exception occurred while loading status: $e")
            }
        }
    }

    private fun putStatusToCache() {
        CacheMemoryUtils.getInstance().put("${controller.name}:status", toJson(status))
    }

    private fun dismissLoadAnim() {
        this@StatusFragment.binding.mcStatusSwipeRefresh.isRefreshing = false
    }

    private fun getStatusColor(status: Status): Int {
        return if (status.isQueryable)
            if (status.isAlive) 0x11c011 else 0x909090
        else 0xFFB300
    }

    private fun getStatusIconId(status: Status): Int {
        return if (status.isQueryable)
            if (status.isAlive) R.drawable.ic_baseline_check_circle_24 else R.drawable.ic_baseline_not_running
        else R.drawable.ic_baseline_question_24
    }

    private fun showAlertAndDismissDialog(info: String) {
        externalScope.launch(Dispatchers.Main) {
            showErrorDialog(info, requireContext())
            dismissLoadAnim()
        }
    }

    private fun showLoadDialog() {
        externalScope.launch(Dispatchers.Main) {
            this@StatusFragment.binding.mcStatusSwipeRefresh.isRefreshing = true
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
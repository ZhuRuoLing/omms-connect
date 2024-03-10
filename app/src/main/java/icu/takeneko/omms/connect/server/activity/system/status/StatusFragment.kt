package icu.takeneko.omms.connect.server.activity.system.status

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ToastUtils
import kotlinx.coroutines.*
import icu.takeneko.omms.client.data.system.SystemInfo
import icu.takeneko.omms.connect.R
import icu.takeneko.omms.connect.client.Connection
import icu.takeneko.omms.connect.databinding.FragmentOsStatusBinding
import icu.takeneko.omms.connect.server.activity.system.view.OsStorageStatusEntryView
import icu.takeneko.omms.connect.util.Assets
import icu.takeneko.omms.connect.server.getSystemType
import icu.takeneko.omms.connect.util.showErrorDialog
import icu.takeneko.omms.connect.util.awaitExecute
import icu.takeneko.omms.connect.util.format
import kotlin.math.ceil
class StatusFragment : Fragment() {

    private var _binding: FragmentOsStatusBinding? = null
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
    }
    private val externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default

    private val binding get() = _binding!!
    private lateinit var systemInfo: SystemInfo
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOsStatusBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val original = requireActivity().intent.getStringExtra("data")
        systemInfo = GsonUtils.fromJson(original, SystemInfo::class.java)
        refreshSystemInfo(false)
        binding.osStatusScrollRefresh.setOnRefreshListener {
            refreshSystemInfo(true)
        }
        return root
    }

    @SuppressLint("SetTextI18n")
    private fun refreshSystemInfo(fetch: Boolean) {
        if (!Connection.isConnected) {
            showErrorDialog("Disconnected from Server.", requireContext())
            return
        }
        externalScope.launch(Dispatchers.IO) {
            launch(Dispatchers.Main) {
                binding.osStatusScrollRefresh.isRefreshing = true
                binding.osMemoryUsage.isIndeterminate = true
                binding.osSwapUsage.isIndeterminate = true
            }
            if (fetch) {
                try {
                    awaitExecute {latch ->
                        Connection.getClientSession().fetchSystemInfoFromServer{
                            systemInfo = Connection.getClientSession().systemInfo
                            latch.countDown()
                        }
                    }
                }catch (e:java.lang.Exception){
                    showErrorDialog(format(R.string.error_system_info_fetch_error,e.toString()), requireContext())
                }
            }
            launch(Dispatchers.Main) {
                ensureActive()
                val icon = Assets.getServerIcon(getSystemType(systemInfo.osName), requireActivity())
                binding.osIcon.setImageDrawable(icon)
                binding.osStatusTitle.text = systemInfo.networkInfo.hostName
                binding.osStatusScrollRefresh.isRefreshing = false

                binding.osMemoryUsage.isIndeterminate = false
                val memoryUsage =
                    ceil((systemInfo.memoryInfo.memoryUsed.toDouble() / systemInfo.memoryInfo.memoryTotal.toDouble()) * 100).toInt()
                binding.osMemoryUsage.progress = memoryUsage
                val totalSpaceMemInGB = String.format("%.1f GB",systemInfo.memoryInfo.memoryTotal.toDouble() / 1024.0 / 1024.0 / 1024.0)
                val usedSpaceMemInGB = String.format("%.1f GB",systemInfo.memoryInfo.memoryUsed.toDouble() / 1024.0 / 1024.0 / 1024.0)
                binding.osMemoryText.text = "$usedSpaceMemInGB/$totalSpaceMemInGB\n$memoryUsage%"

                binding.osSwapUsage.isIndeterminate = false
                val swapUsage =
                    ceil((systemInfo.memoryInfo.swapUsed.toDouble() / systemInfo.memoryInfo.swapTotal.toDouble()) * 100).toInt()
                binding.osSwapUsage.progress = swapUsage
                val totalSpaceSwpInGB = String.format("%.1f GB",systemInfo.memoryInfo.swapTotal.toDouble() / 1024.0 / 1024.0 / 1024.0)
                val usedSpaceSwpInGB = String.format("%.1f GB",systemInfo.memoryInfo.swapUsed.toDouble() / 1024.0 / 1024.0 / 1024.0)
                binding.osSwapText.text = "$usedSpaceSwpInGB/$totalSpaceSwpInGB\n$swapUsage%"

                if (systemInfo.processorInfo.cpuLoadAvg == -1.0){
                    binding.osLoadIndicator.progress = 0
                    binding.osLoadAvgText.setText(R.string.unavailable)
                    //5.38 / 8
                }else{
                    val progress = ceil((systemInfo.processorInfo.cpuLoadAvg * 100) / systemInfo.processorInfo.logicalProcessorCount).toInt()
                    binding.osLoadIndicator.progress = progress
                    binding.osLoadAvgText.text = String.format("%.2f",systemInfo.processorInfo.cpuLoadAvg)
                }

                binding.osStorageInfo.removeAllViews()
                systemInfo.fileSystemInfo.fileSystemList.forEach{
                    val view = OsStorageStatusEntryView(requireContext())
                    view.loadFilesystemData(it)
                    this@StatusFragment.binding.osStorageInfo.addView(view)
                }


            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
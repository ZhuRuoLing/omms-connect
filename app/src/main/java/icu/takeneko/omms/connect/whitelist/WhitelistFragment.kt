package icu.takeneko.omms.connect.whitelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import icu.takeneko.omms.connect.R
import icu.takeneko.omms.connect.client.Connection
import icu.takeneko.omms.connect.databinding.FragmentWhitelistBinding
import icu.takeneko.omms.connect.util.ResultContract
import icu.takeneko.omms.connect.util.formatResString
import icu.takeneko.omms.connect.util.showErrorDialog
import icu.takeneko.omms.connect.view.Placeholder68dpView
import icu.takeneko.omms.connect.whitelist.view.WhitelistEntryView
import icu.takeneko.omms.connect.util.awaitExecute


class WhitelistFragment : Fragment() {

    private var _binding: FragmentWhitelistBinding? = null
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
    }
    private val externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)

    private val activityResultLauncher =
        registerForActivityResult(ResultContract("requireRefresh", 114514)) {
            if (it) {
                this.binding.whitelistSwipeRefresh.isRefreshing = true
                refreshWhitelist(false)
            }
        }


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var whitelistMap = HashMap<String, ArrayList<String>>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWhitelistBinding.inflate(inflater, container, false)
        val root: View = binding.root
        if (Connection.isConnected) {
            refreshWhitelist()
        }
        binding.whitelistSwipeRefresh.setOnRefreshListener {
            refreshWhitelist(showDialog = false)
        }
        return root
    }

    private fun refreshWhitelist(showDialog: Boolean = true) {
        val alertDialog = this.context?.let {
            MaterialAlertDialogBuilder(it)
                .setCancelable(false)
                .setTitle(R.string.label_loading)
                .setMessage(R.string.label_wait)
                .create()
        }
        externalScope.launch(Dispatchers.IO) {
            if (showDialog) {
                launch(Dispatchers.Main) {
                    alertDialog?.show()
                }
            }
            ensureActive()
            try {
                awaitExecute { latch ->
                    Connection.getClientSession().apply {
                        this.fetchWhitelistFromServer {
                            this@WhitelistFragment.whitelistMap = it
                            latch.countDown()
                        }
                    }
                }
            } catch (e: Exception) {
                if (showDialog) {
                    alertDialog?.dismiss()
                } else {
                    this@WhitelistFragment.binding.whitelistSwipeRefresh.isRefreshing = false
                }
                showErrorDialog("Cannot fetch whitelists from server, reason: $e", requireContext())
            }
            launch(Dispatchers.Main) {
                ensureActive()
                try {
                    this@WhitelistFragment.binding.linearLayout.removeAllViews()
                    this@WhitelistFragment.whitelistMap.forEach {
                        val view =
                            WhitelistEntryView(requireContext()).setAttribute(
                                it.key,
                                it.value,
                                activityResultLauncher
                            )
                        this@WhitelistFragment.binding.linearLayout.addView(view)
                    }
                    this@WhitelistFragment.binding.linearLayout.addView(
                        Placeholder68dpView(
                            requireContext()
                        )
                    )
//                    this@WhitelistFragment.binding.linearLayout.addView(this@WhitelistFragment.context?.let {
//                        Placeholder68dpView(
//                            it
//                        )
//                    })
                    this@WhitelistFragment.binding.whitelistTitle.text = formatResString(
                        R.string.label_whitelists_count,
                        whitelistMap.count(),
                        context = requireContext()
                    )
                    if (showDialog) {
                        alertDialog?.dismiss()
                    } else {
                        this@WhitelistFragment.binding.whitelistSwipeRefresh.isRefreshing = false
                    }
                } catch (e: Exception) {
                    showErrorDialog(e.toString(), requireContext())
                    if (showDialog) {
                        alertDialog?.dismiss()
                    } else {
                        this@WhitelistFragment.binding.whitelistSwipeRefresh.isRefreshing = false

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


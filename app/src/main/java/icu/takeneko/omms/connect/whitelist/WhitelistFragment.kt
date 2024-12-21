package icu.takeneko.omms.connect.whitelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import icu.takeneko.omms.connect.R
import icu.takeneko.omms.connect.client.Connection
import icu.takeneko.omms.connect.databinding.FragmentWhitelistBinding
import icu.takeneko.omms.connect.util.ResultContract
import icu.takeneko.omms.connect.util.format
import icu.takeneko.omms.connect.util.showErrorDialog
import icu.takeneko.omms.connect.util.toErrorMessage
import icu.takeneko.omms.connect.whitelist.view.WhitelistEntryView
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus


class WhitelistFragment : Fragment() {

    private var _binding: FragmentWhitelistBinding? = null
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong(requireContext().toErrorMessage(e))
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
    private var whitelistMap = mutableMapOf<String, MutableList<String>>()
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
                .setIcon(R.drawable.ic_wait_24)
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
                whitelistMap = Connection.getClientSession().fetchWhitelistFromServer().get()
            } catch (e: Exception) {
                if (showDialog) {
                    alertDialog?.dismiss()
                } else {
                    this@WhitelistFragment.binding.whitelistSwipeRefresh.isRefreshing = false
                }
                showErrorDialog(
                    requireContext().getString(
                        R.string.hint_whitelist_fetch_failed,
                        "$e"
                    ), requireContext()
                )
            }
            launch(Dispatchers.Main) {
                ensureActive()
                try {
                    this@WhitelistFragment.binding.linearLayout.removeAllViews()
                    this@WhitelistFragment.whitelistMap.forEach {
                        val view =
                            WhitelistEntryView(requireContext()).setAttribute(
                                requireContext(),
                                it.key,
                                it.value,
                                activityResultLauncher
                            )
                        this@WhitelistFragment.binding.linearLayout.addView(view)
                    }
                    this@WhitelistFragment.binding.whitelistTitle.text =
                        this@WhitelistFragment.format(
                            R.string.label_whitelists_count,
                            whitelistMap.count()
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


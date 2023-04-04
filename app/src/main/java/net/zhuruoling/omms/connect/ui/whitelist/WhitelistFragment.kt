package net.zhuruoling.omms.connect.ui.whitelist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import net.zhuruoling.omms.connect.R
import net.zhuruoling.omms.connect.client.Connection
import net.zhuruoling.omms.connect.databinding.FragmentWhitelistBinding
import net.zhuruoling.omms.connect.ui.util.formatResString
import net.zhuruoling.omms.connect.ui.util.showErrorDialog
import net.zhuruoling.omms.connect.ui.view.Placeholder68dpView
import net.zhuruoling.omms.connect.ui.whitelist.activity.WhitelistEditActivity
import net.zhuruoling.omms.connect.ui.whitelist.view.WhitelistEntryView
import net.zhuruoling.omms.connect.util.awaitExecute


class WhitelistFragment : Fragment() {

    private var _binding: FragmentWhitelistBinding? = null
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
    }
    private val externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)

    private val activityResultLauncher = registerForActivityResult(ResultContract()) {
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
                awaitExecute{latch ->
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

    class ResultContract : ActivityResultContract<Int, Boolean>() {
        override fun createIntent(context: Context, input: Int): Intent {
            val intent = Intent(context, WhitelistEditActivity::class.java)
            intent.putExtra("check", input)
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return if (resultCode == 114514) {
                intent!!.getBooleanExtra("requireRefresh", false)
            } else {
                false
            }
        }

    }

}


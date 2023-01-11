package net.zhuruoling.ommsconnect.ui.whitelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import net.zhuruoling.ommsconnect.client.Connection
import net.zhuruoling.ommsconnect.databinding.FragmentWhitelistBinding
import net.zhuruoling.ommsconnect.ui.util.showErrorDialog
import net.zhuruoling.ommsconnect.ui.view.Placeholder68dpView
import net.zhuruoling.ommsconnect.ui.whitelist.view.WhitelistEntryView


class WhitelistFragment : Fragment() {

    private var _binding: FragmentWhitelistBinding? = null
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
    }
    private val externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)
    val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default

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
            ToastUtils.showShort("Refreshing!")
            refreshWhitelist(showDialog = false)
        }
        return root
    }

    private fun refreshWhitelist(showDialog: Boolean = true) {
        val alertDialog = this.context?.let {
            MaterialAlertDialogBuilder(it)
                .setCancelable(false)
                .setTitle("Loading")
                .setMessage("Please Wait...")
                .create()
        }

        externalScope.launch(Dispatchers.IO) {
            if (showDialog) {
                launch(Dispatchers.Main) {
                    alertDialog?.show()
                }
            }
            ensureActive()
            try{
                Connection.getClientSession().apply {
                    this.fetchWhitelistFromServer()
                    this@WhitelistFragment.whitelistMap = this.whitelistMap
                }
            }catch (e:Exception){
                if (showDialog){
                    alertDialog?.dismiss()
                }else{
                    this@WhitelistFragment.binding.whitelistSwipeRefresh.isRefreshing = false
                }
                showErrorDialog("Cannot fetch whitelists from server, reason: $e", requireContext())
            }
            launch(Dispatchers.Main) {
                ensureActive()
                try {
                    this@WhitelistFragment.binding.linearLayout.removeAllViews()
                    this@WhitelistFragment.whitelistMap.forEach {
                        context?.let { it1 ->
                            val view = WhitelistEntryView(
                                it1
                            ).setAttribute(it.key, it.value)
                            this@WhitelistFragment.binding.linearLayout.addView(view)
                        }
                    }
                    this@WhitelistFragment.binding.linearLayout.addView(this@WhitelistFragment.context?.let {
                        Placeholder68dpView(
                            it
                        )
                    })
//                    this@WhitelistFragment.binding.linearLayout.addView(this@WhitelistFragment.context?.let {
//                        Placeholder68dpView(
//                            it
//                        )
//                    })
                    this@WhitelistFragment.binding.whitelistTitle.text =
                        "${this@WhitelistFragment.whitelistMap.size} whitelists were added to this server.";
                    if (showDialog) {
                        alertDialog?.dismiss()
                    }else{
                        this@WhitelistFragment.binding.whitelistSwipeRefresh.isRefreshing = false
                        ToastUtils.showShort("Done!")
                    }
                } catch (e: Exception) {
                    showErrorDialog(e.toString(), requireContext())
                    if (showDialog){
                        alertDialog?.dismiss()
                    }else{
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
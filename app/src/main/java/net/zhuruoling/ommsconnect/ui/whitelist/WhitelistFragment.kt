package net.zhuruoling.ommsconnect.ui.whitelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import net.zhuruoling.ommsconnect.client.Connection
import net.zhuruoling.ommsconnect.databinding.FragmentWhitelistBinding
import net.zhuruoling.ommsconnect.ui.view.Placeholder68dpView
import net.zhuruoling.ommsconnect.ui.whitelist.view.WhitelistEntryView
import java.lang.Exception


class WhitelistFragment : Fragment() {

    private var _binding: FragmentWhitelistBinding? = null
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
    }
    val externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)
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
        ViewModelProvider(this).get(WhitelistViewModel::class.java)

        _binding = FragmentWhitelistBinding.inflate(inflater, container, false)
        val root: View = binding.root
        if (Connection.isConnected) {
            initGui()
        }
        return root
    }

    private fun initGui() {
        val alertDialog = this.context?.let {
            MaterialAlertDialogBuilder(it)
                .setCancelable(false)
                .setTitle("Loading")
                .setMessage("Please Wait...")
                .create()
        }
        externalScope.launch(Dispatchers.IO) {
            launch(Dispatchers.Main) {
                alertDialog?.show()
            }
            ensureActive()
            Connection.getClientSession().apply {
                this.fetchWhitelistFromServer()
                this@WhitelistFragment.whitelistMap = this.whitelistMap
            }
            launch(Dispatchers.Main) {
                try {
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
                    this@WhitelistFragment.binding.linearLayout.addView(this@WhitelistFragment.context?.let {
                        Placeholder68dpView(
                            it
                        )
                    })
                    this@WhitelistFragment.binding.whitelistTitle.text =
                        "${this@WhitelistFragment.whitelistMap.size} whitelists were added to this server.";
                    alertDialog?.dismiss()
                } catch (e: Exception) {
                    alertDialog?.dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
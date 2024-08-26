package icu.takeneko.omms.connect.server.activity.system.chatbridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import icu.takeneko.omms.client.data.chatbridge.Broadcast
import icu.takeneko.omms.connect.client.Connection
import icu.takeneko.omms.connect.databinding.FragmentOsChatbridgeBinding
import icu.takeneko.omms.connect.server.activity.system.view.ChatMessageView
import icu.takeneko.omms.connect.view.Placeholder68dpView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatbridgeFragment : Fragment() {

    private var _binding: FragmentOsChatbridgeBinding? = null

    private val binding get() = _binding!!
    private var chatText
        get() = binding.chatText.text?.toString() ?: ""
        set(value) = binding.chatText.setText(value)

    private var isRefreshing
        get() = binding.chatSwipeRefresh.isRefreshing
        set(value) {
            binding.chatSwipeRefresh.isRefreshing = value
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOsChatbridgeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.chatSend.setOnClickListener {
            val message = chatText
            if (message.isBlank())return@setOnClickListener
            Connection.getClientSession().sendChatbridgeMessage(
                "GLOBAL",
                message
            ) { channel, messageSent ->

            }
            chatText = ""
        }
        binding.chatSwipeRefresh.setOnRefreshListener {
            isRefreshing = false
        }
        binding.chatScroll.fullScroll(View.FOCUS_DOWN)
        Connection.registerBroadcastListener {
            lifecycleScope.launch(Dispatchers.Main) {
                addNewBroadcast(it)
            }
        }
        Connection.chatMessageCache.forEach {
            addNewBroadcast(it)
        }
        return root
    }

    private fun addNewBroadcast(br: Broadcast) {
        binding.chatMessages.addView(ChatMessageView(requireContext()).also { it.updateContent(br, requireContext()) })
        binding.chatScroll.fullScroll(View.FOCUS_DOWN)
    }

    override fun onDestroyView() {
        Connection.unregisterBroadcastListener()
        super.onDestroyView()
        _binding = null
    }
}
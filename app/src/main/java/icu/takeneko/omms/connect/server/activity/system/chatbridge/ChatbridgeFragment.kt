package icu.takeneko.omms.connect.server.activity.system.chatbridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import icu.takeneko.omms.connect.databinding.FragmentOsChatbridgeBinding

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
            chatText = ""
        }
        binding.chatSwipeRefresh.setOnRefreshListener {
            isRefreshing = false
        }
        binding.chatScroll.fullScroll(View.FOCUS_DOWN)
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
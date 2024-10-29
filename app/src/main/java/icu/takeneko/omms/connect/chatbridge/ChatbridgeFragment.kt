package icu.takeneko.omms.connect.chatbridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import icu.takeneko.omms.connect.databinding.FragmentChatbridgeBinding

class ChatbridgeFragment : Fragment() {

    private var _binding: FragmentChatbridgeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatbridgeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.announcementSwipeRefresh.isRefreshing = false
        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
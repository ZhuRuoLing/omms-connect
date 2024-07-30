package icu.takeneko.omms.connect.server.activity.system.chatbridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import icu.takeneko.omms.connect.databinding.FragmentOsChatbridgeBinding

//import icu.takeneko.ommsconnect.ui.server.activity.databinding.FragmentHomeBinding

class ChatbridgeFragment : Fragment() {

    private var _binding: FragmentOsChatbridgeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOsChatbridgeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
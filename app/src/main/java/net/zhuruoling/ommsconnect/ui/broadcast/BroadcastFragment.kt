package net.zhuruoling.ommsconnect.ui.broadcast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import net.zhuruoling.ommsconnect.client.Connection
import net.zhuruoling.ommsconnect.databinding.FragmentAnnouncementBinding

class BroadcastFragment : Fragment() {

    private var _binding: FragmentAnnouncementBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val broadcastViewModel =
            ViewModelProvider(this).get(BroadcastViewModel::class.java)

        _binding = FragmentAnnouncementBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
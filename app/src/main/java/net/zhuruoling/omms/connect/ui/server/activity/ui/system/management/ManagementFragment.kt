package net.zhuruoling.omms.connect.ui.server.activity.ui.system.management

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import net.zhuruoling.omms.connect.databinding.FragmentOsManagementBinding

//import net.zhuruoling.ommsconnect.ui.server.activity.databinding.FragmentHomeBinding

class ManagementFragment : Fragment() {

    private var _binding: FragmentOsManagementBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(ManagementViewModel::class.java)

        _binding = FragmentOsManagementBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
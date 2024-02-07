package icu.takeneko.omms.connect.server.activity.system.management

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import icu.takeneko.omms.connect.databinding.FragmentOsManagementBinding

//import icu.takeneko.ommsconnect.ui.server.activity.databinding.FragmentHomeBinding

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

        _binding = FragmentOsManagementBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
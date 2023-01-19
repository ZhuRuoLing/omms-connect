package net.zhuruoling.omms.connect.ui.server.activity.ui.minecraft.ui.management

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import net.zhuruoling.omms.connect.databinding.FragmentMcManagementBinding


class ManagementFragment : Fragment() {

    private var _binding: FragmentMcManagementBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMcManagementBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val textView: TextView = binding.textHome
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
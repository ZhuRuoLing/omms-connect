package net.zhuruoling.omms.connect.server.activity.system.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import net.zhuruoling.omms.connect.databinding.FragmentOsControlBinding

//import net.zhuruoling.ommsconnect.ui.server.activity.databinding.FragmentSlideshowBinding

class ControlFragment : Fragment() {

    private var _binding: FragmentOsControlBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOsControlBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSlideshow

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
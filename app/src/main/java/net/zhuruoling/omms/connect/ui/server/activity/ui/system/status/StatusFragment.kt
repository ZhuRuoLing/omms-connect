package net.zhuruoling.omms.connect.ui.server.activity.ui.system.status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import net.zhuruoling.ommsconnect.databinding.FragmentOsStatusBinding

//import net.zhuruoling.ommsconnect.ui.server.activity.databinding.FragmentGalleryBinding

class StatusFragment : Fragment() {

    private var _binding: FragmentOsStatusBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(StatusViewModel::class.java)

        _binding = FragmentOsStatusBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        galleryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
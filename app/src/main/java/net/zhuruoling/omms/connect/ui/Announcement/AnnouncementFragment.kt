package net.zhuruoling.omms.connect.ui.Announcement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.zhuruoling.omms.client.util.Result
import net.zhuruoling.omms.connect.client.Connection
import net.zhuruoling.omms.connect.ui.util.showErrorDialog
import net.zhuruoling.ommsconnect.databinding.FragmentAnnouncementBinding

class AnnouncementFragment : Fragment() {

    private var _binding: FragmentAnnouncementBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnnouncementBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.announcementSwipeRefresh.isRefreshing = false
        binding.announcementSwipeRefresh.setOnRefreshListener {
            refresh(false)
        }
        refresh(true)
        return root
    }

    private fun refresh(showDialog: Boolean){
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setTitle("Loading")
            .setMessage("Please Wait...")
            .create()
        if (showDialog) {
            alertDialog.show()
        }
        if (Connection.isConnected){
            try {
                val ret = Connection.getClientSession().fetchAnnouncementFromServer()
                if (ret != Result.OK){
                    showErrorDialog("Central Server returned error code $ret", requireContext())
                    return
                }
            }catch (e:Exception){
                showErrorDialog("Error in fetching announcement.\n$e",requireContext())
                return
            }
        }else{
            showErrorDialog("Disconnected from server.", requireContext())
            return
        }
        binding.announcementList.removeAllViews()



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
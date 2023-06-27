package net.zhuruoling.omms.connect.resource

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.zhuruoling.omms.connect.R
import net.zhuruoling.omms.connect.databinding.ActivityServerIconManageBinding

class ServerIconManageActivity : AppCompatActivity() {
    private val pickImageContract = ActivityResultContracts.PickVisualMedia()
    private lateinit var binding: ActivityServerIconManageBinding

    private val activityResultLauncher =
        registerForActivityResult(pickImageContract) {
            Log.i("OMMS", it.toString())
            if (it == null) return@registerForActivityResult
            lifecycleScope.launch(Dispatchers.Main) {
                val textView = TextInputEditText(this@ServerIconManageActivity)
                val linearLayout = LinearLayout(this@ServerIconManageActivity)
                linearLayout.orientation = LinearLayout.VERTICAL
                val imageView = ImageView(this@ServerIconManageActivity)
                imageView.setImageURI(it)
                linearLayout.addView(imageView)
                linearLayout.addView(LinearLayout(this@ServerIconManageActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    addView(TextView(this@ServerIconManageActivity).apply { this.text = "Icon Id: " })
                    addView(textView)
                })
                val dialog = MaterialAlertDialogBuilder(this@ServerIconManageActivity)
                    .setView(linearLayout)
                    .setCancelable(true)
                    .setTitle("Import Image")
                    .setPositiveButton("Done") { _, _ ->
                        val result = textView.text.toString()
                        if (result.isEmpty()){
                            MaterialAlertDialogBuilder(this@ServerIconManageActivity)
                                .setTitle("Error")
                                .setCancelable(true)
                                .setMessage("Id of this icon is empty")
                                .setPositiveButton("Done",null)
                                .show()
                            return@setPositiveButton
                        }
                        launchImportImage(result, it)
                    }
                dialog.show()
            }
        }

    private fun launchImportImage(id: String, uri: Uri) {
        Log.i("OMMS", id)
        val dialog = MaterialAlertDialogBuilder(this@ServerIconManageActivity)
        dialog.setCancelable(false).setTitle("Importing Image")
        val d = dialog.create()
        d.show()
        lifecycleScope.launch(Dispatchers.IO) {
            ServerIconResourceManager.importImageFromUri(this@ServerIconManageActivity, id, uri)
            lifecycleScope.launch(Dispatchers.Main){
                d.cancel()
                MaterialAlertDialogBuilder(this@ServerIconManageActivity)
                    .setTitle("Done")
                    .setCancelable(true)
                    .setPositiveButton("OK",null)
                    .show()
            }
            reload(false)
        }
    }

    fun reload(load:Boolean = true){
        if (load) ServerIconResourceManager.load(this@ServerIconManageActivity)
        ServerIconResourceManager.forEach{
            Log.i("OMMS", "Icon load: $this")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerIconManageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.serverIconAddFab.setOnClickListener {
            activityResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        lifecycleScope.launch(Dispatchers.IO){
            reload()
        }
    }
}
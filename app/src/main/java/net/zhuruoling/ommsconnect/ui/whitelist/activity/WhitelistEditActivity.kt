package net.zhuruoling.ommsconnect.ui.whitelist.activity

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.CacheMemoryUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import net.zhuruoling.ommsconnect.R
import net.zhuruoling.ommsconnect.client.Connection
import net.zhuruoling.ommsconnect.databinding.ActivityWhitelistEditBinding
import net.zhuruoling.ommsconnect.ui.whitelist.view.WhitelistCompoentView

class WhitelistEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWhitelistEditBinding

    var fromWhitelist: String = ""
    var players: ArrayList<String> = arrayListOf()
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
        Log.e("wdnmd","FUCK", e)
    }
    private var externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityWhitelistEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.fab.setOnClickListener { view -> showActions() }
        fromWhitelist = CacheMemoryUtils.getInstance().get("from_whitelist")
        players = CacheMemoryUtils.getInstance().get("whitelist_content")
        binding.whitelistNameTitle.text = "${players.size} players were added to this whitelist."
        if (players.isNotEmpty())players.forEach {
            this.binding.whitelistCompoentContainer.addView(WhitelistCompoentView(this).setAttribute(it, fromWhitelist, this))
        }
    }

    fun showActions(){
        val textView = TextInputEditText(this)

        val dialog = MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_baseline_more_horiz_24)
            .setTitle("Add to whitelist")
            .setView(textView)
            .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                val alertDialogBuilder = MaterialAlertDialogBuilder(this)
                    .setIcon(R.drawable.ic_baseline_more_horiz_24)
                    .setTitle("Working...")
                    .setPositiveButton("OK",null)
                val dialog = alertDialogBuilder.show()
                externalScope.launch(Dispatchers.IO) {
                    val session = Connection.getClientSession()
                    val result = session.addToWhitelist(fromWhitelist, textView.text.toString())
                    this.launch(Dispatchers.Main){
                        if (result.name != "OK") {
                            dialog.dismiss()
                            MaterialAlertDialogBuilder(this@WhitelistEditActivity)
                                .setIcon(R.drawable.ic_baseline_error_24)
                                .setTitle("Error")
                                .setMessage("Failed to add ${textView.text.toString()} to whitelist, reason: $result")
                                .setPositiveButton("OK",null)
                                .show()
                        }
                        else{
                            dialog.dismiss()
                            MaterialAlertDialogBuilder(this@WhitelistEditActivity)
                                .setIcon(R.drawable.ic_notifications_black_24dp)
                                .setTitle("Success")
                                .setMessage("Added ${textView.text.toString()} to whitelist, reason: $result")
                                .setPositiveButton("OK",null)
                                .show()
                        }
                    }

                }
            }
        dialog.show()
    }


    fun init(from: String, players: ArrayList<String>){
        this.fromWhitelist = from
        this.players = players
    }

}
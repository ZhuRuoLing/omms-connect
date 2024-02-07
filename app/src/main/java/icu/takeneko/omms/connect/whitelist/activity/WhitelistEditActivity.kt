package icu.takeneko.omms.connect.whitelist.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.CacheMemoryUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import icu.takeneko.omms.connect.R
import icu.takeneko.omms.connect.client.Connection
import icu.takeneko.omms.connect.databinding.ActivityWhitelistEditBinding
import icu.takeneko.omms.connect.whitelist.view.WhitelistPlayerView
import icu.takeneko.omms.connect.util.awaitExecute

class WhitelistEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWhitelistEditBinding

    private var fromWhitelist: String = ""
    private var players: ArrayList<String> = arrayListOf()
    var requireRefresh = false
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
        Log.e("OMMS", "Failed connect to server", e)
    }
    private var externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)
    override fun  onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityWhitelistEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.fab.setOnClickListener { showActions() }
        fromWhitelist = CacheMemoryUtils.getInstance().get("from_whitelist")
        players = CacheMemoryUtils.getInstance().get("whitelist_content")
        binding.whitelistNameTitle.text = fromWhitelist
        binding.whitelistInfoText.text = "${players.size} players were added to this whitelist."
        refreshPlayerList()
        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT) {
                setResult(114514, Intent().putExtra("requireRefresh", requireRefresh))
                finish()
            }
        }
    }

    fun refreshPlayerList() {
        players.sort()
        externalScope.launch(Dispatchers.Main) {
            this@WhitelistEditActivity.binding.whitelistCompoentContainer.removeAllViews()
            if (players.isNotEmpty()) players.forEach {
                this@WhitelistEditActivity.binding.whitelistCompoentContainer.addView(
                    WhitelistPlayerView(this@WhitelistEditActivity).setAttribute(
                        it,
                        fromWhitelist,
                        this@WhitelistEditActivity
                    )
                )
            }
        }
    }

    private fun addPlayer(player: String) {
        players.add(player)
        players.sort()
    }

    fun removePlayer(player: String) {
        players.remove(player)
        players.sort()
    }


    private fun showActions() {
        val textView = TextInputEditText(this)
        val dialog = MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_baseline_more_horiz_24)
            .setTitle("Add to whitelist")
            .setView(textView)
            .setPositiveButton("OK") { _: DialogInterface, _: Int ->
                val alertDialogBuilder = MaterialAlertDialogBuilder(this)
                    .setIcon(R.drawable.ic_baseline_more_horiz_24)
                    .setTitle("Working...")
                    .setPositiveButton("OK", null)
                val dialog = alertDialogBuilder.show()
                externalScope.launch(Dispatchers.IO) {
                    val session = Connection.getClientSession()
                    awaitExecute { latch ->
                        session.setOnPermissionDeniedCallback {
                            dialog.dismiss()
                            MaterialAlertDialogBuilder(this@WhitelistEditActivity)
                                .setIcon(R.drawable.ic_baseline_error_24)
                                .setTitle("Error")
                                .setMessage("Failed to add ${textView.text.toString()} to whitelist: Permission Denied")
                                .setPositiveButton("OK", null)
                                .show()
                            latch.countDown()
                            session.setOnPermissionDeniedCallback(null)
                        }
                        session.addToWhitelist(fromWhitelist, textView.text.toString(), {
                            launch(Dispatchers.Main) {
                                dialog.dismiss()
                                MaterialAlertDialogBuilder(this@WhitelistEditActivity)
                                    .setIcon(R.drawable.ic_notifications_black_24dp)
                                    .setTitle("Success")
                                    .setMessage("Added ${textView.text.toString()} to whitelist.")
                                    .setPositiveButton("OK", null)
                                    .show()
                                addPlayer(textView.text.toString())
                                refreshPlayerList()
                                latch.countDown()
                            }
                        }, {
                            launch(Dispatchers.Main) {
                                dialog.dismiss()
                                MaterialAlertDialogBuilder(this@WhitelistEditActivity)
                                    .setIcon(R.drawable.ic_baseline_error_24)
                                    .setTitle("Error")
                                    .setMessage("Failed to add ${textView.text.toString()} to whitelist, this player already exists.")
                                    .setPositiveButton("OK", null)
                                    .show()
                                latch.countDown()
                            }
                        })
                        requireRefresh = true
                    }

                }
            }.setOnCancelListener {
                requireRefresh = false
            }
        dialog.show()
    }

    override fun onDestroy() {
        setResult(114514, Intent().putExtra("requireRefresh", requireRefresh))
        super.onDestroy()
    }


    fun init(from: String, players: ArrayList<String>) {
        this.fromWhitelist = from
        this.players = players
        this.players.sort()
    }

}
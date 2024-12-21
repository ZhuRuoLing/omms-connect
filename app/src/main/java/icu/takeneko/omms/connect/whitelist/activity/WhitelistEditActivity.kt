package icu.takeneko.omms.connect.whitelist.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.ActionBar.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.CacheMemoryUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import icu.takeneko.omms.connect.R
import icu.takeneko.omms.connect.client.Connection
import icu.takeneko.omms.connect.databinding.ActivityWhitelistEditBinding
import icu.takeneko.omms.connect.util.format
import icu.takeneko.omms.connect.whitelist.view.WhitelistPlayerView
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.util.concurrent.ExecutionException

class WhitelistEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWhitelistEditBinding

    private var fromWhitelist: String = ""
    private var players: List<String> = mutableListOf()
    var requireRefresh = false
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
        Log.e("OMMS", "Failed connect to server", e)
    }
    private var externalScope: CoroutineScope = lifecycleScope.plus(coroutineExceptionHandler)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityWhitelistEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.fab.setOnClickListener { showActions() }
        fromWhitelist = CacheMemoryUtils.getInstance().get("from_whitelist")
        players = CacheMemoryUtils.getInstance().get("whitelist_content")
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            setResult(114514, Intent().putExtra("requireRefresh", requireRefresh))
            finish()
        }
        binding.toolbar.setTitle(getString(R.string.label_manage_whitelist, fromWhitelist))
        binding.whitelistInfoText.text =
            getString(R.string.label_players_added_whitelist, players.size.toString())
        refreshPlayerList()
        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT) {
                setResult(114514, Intent().putExtra("requireRefresh", requireRefresh))
                finish()
            }
        }
    }

    fun refreshPlayerList() {
        players.sorted()
        externalScope.launch(Dispatchers.Main) {
            this@WhitelistEditActivity.binding.whitelistCompoentContainer.removeAllViews()
            if (players.isNotEmpty()) {
                players.forEach {
                    this@WhitelistEditActivity.binding.whitelistCompoentContainer.addView(
                        WhitelistPlayerView(this@WhitelistEditActivity).setAttribute(
                            it,
                            fromWhitelist,
                            this@WhitelistEditActivity
                        )
                    )
                }
                binding.textNothing.visibility = View.GONE
            }else{
                binding.textNothing.visibility = View.VISIBLE
            }
        }
    }

    private fun addPlayer(player: String) {
        players += player
        players.sorted()
    }

    fun removePlayer(player: String) {
        players -= player
        players.sorted()
    }


    private fun showActions() {
        val textView = TextInputEditText(this)
        val linearLayout = LinearLayout(this)
        linearLayout.setPadding(
            24,
            24,
            24,
            24
        )
        linearLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        textView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        linearLayout.addView(textView)
        val dialog = MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_baseline_add_24)
            .setTitle(R.string.label_add_player)
            .setView(linearLayout)
            .setPositiveButton(R.string.ok) { _: DialogInterface, _: Int ->
                val alertDialogBuilder = MaterialAlertDialogBuilder(this)
                    .setIcon(R.drawable.ic_baseline_more_horiz_24)
                    .setTitle(R.string.label_working)
                    .setPositiveButton(R.string.ok, null)
                val dialog = alertDialogBuilder.show()
                val player = textView.text.toString()
                externalScope.launch(Dispatchers.IO) {
                    val session = Connection.getClientSession()
                    session.onPermissionDeniedCallback.setCallback {
                        dialog.dismiss()
                        MaterialAlertDialogBuilder(this@WhitelistEditActivity)
                            .setIcon(R.drawable.ic_baseline_error_24)
                            .setTitle(R.string.error)
                            .setMessage(format(
                                R.string.hint_whitelist_remove_permission_denied,
                                player
                            ))
                            .setPositiveButton(R.string.ok, null)
                            .show()
                        session.onPermissionDeniedCallback.setCallback { }
                    }
                    try {
                        session.addToWhitelist(fromWhitelist, textView.text.toString()).get()
                        launch(Dispatchers.Main) {
                            dialog.dismiss()
                            MaterialAlertDialogBuilder(this@WhitelistEditActivity)
                                .setIcon(R.drawable.ic_baseline_check_24)
                                .setTitle(R.string.success)
                                .setMessage(format(R.string.hint_whitelist_player_added, player))
                                .setPositiveButton(R.string.ok, null)
                                .show()
                            addPlayer(textView.text.toString())
                            refreshPlayerList()
                        }
                    } catch (ex: ExecutionException) {
                        launch(Dispatchers.Main) {
                            dialog.dismiss()
                            MaterialAlertDialogBuilder(this@WhitelistEditActivity)
                                .setIcon(R.drawable.ic_baseline_error_24)
                                .setTitle(R.string.error)
                                .setMessage(getString(R.string.hint_whitelist_add_failed, player))
                                .setPositiveButton(R.string.ok, null)
                                .show()
                        }
                    }
                    requireRefresh = true
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
        this.players.sorted()
    }

}
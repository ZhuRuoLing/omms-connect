package icu.takeneko.omms.connect.whitelist.view

import android.content.Context
import icu.takeneko.omms.connect.client.Connection.getClientSession
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.TextView
import icu.takeneko.omms.connect.whitelist.activity.WhitelistEditActivity
import android.view.LayoutInflater
import icu.takeneko.omms.connect.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.content.DialogInterface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ToastUtils
import kotlinx.coroutines.*
import icu.takeneko.omms.connect.util.formatResString
import icu.takeneko.omms.connect.util.awaitExecute

class WhitelistPlayerView : ConstraintLayout {
    private lateinit var playerNameText: TextView
    private lateinit var fromWhitelist: String
    private lateinit var activity: WhitelistEditActivity
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
        Log.e("wdnmd", "FUCK", e)
    }

    private lateinit var externalScope: CoroutineScope

    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.whitelist_player_view, this)

        setOnClickListener { view: View -> displayActions(view) }
        playerNameText = findViewById(R.id.playerNameText)
        setOnClickListener { view: View -> displayActions(view) }
    }

    private fun displayActions(view: View) {
        val dialog = MaterialAlertDialogBuilder(activity)
            .setCancelable(true)
            .setTitle("Confirm")
            .setMessage(
                String.format(
                    "Are you sure to remove this player?\n player:%s\n whitelist: %s",
                    playerNameText.text,
                    fromWhitelist
                )
            )
            .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                externalScope.launch(Dispatchers.IO) {
                    val playerName = playerNameText.text.toString()
                    val session = getClientSession()
                    awaitExecute { latch ->
                        session.setOnPermissionDeniedCallback {
                            MaterialAlertDialogBuilder(this@WhitelistPlayerView.activity)
                                .setIcon(R.drawable.ic_baseline_error_24)
                                .setTitle("Error")
                                .setMessage(
                                    formatResString(
                                        R.string.hint_whitelist_remove_permission_denied,
                                        playerNameText.text,
                                        context = context
                                    )
                                )
                                .setMessage("Failed to remove ${playerNameText.text} from whitelist: Permission Denied")
                                .setPositiveButton("OK", null)
                                .show()
                            latch.countDown()
                            session.setOnPermissionDeniedCallback(null)
                        }
                        session.removeFromWhitelist(fromWhitelist, playerName, {
                            this.launch(Dispatchers.Main) {
                                MaterialAlertDialogBuilder(activity)
                                    .setPositiveButton("Ok", null)
                                    .setTitle("Success")
                                    .setMessage(
                                        formatResString(
                                            R.string.hint_whitelist_player_removed,
                                            it.b,
                                            context = this@WhitelistPlayerView.context
                                        )
                                    ).show()
                                activity.requireRefresh = true
                                activity.removePlayer(playerName)
                                activity.refreshPlayerList()
                                latch.countDown()
                            }
                        }, {
                            MaterialAlertDialogBuilder(activity)
                                .setPositiveButton("Ok", null)
                                .setTitle("Fail")
                                .setMessage(
                                    formatResString(
                                        R.string.hint_whitelist_remove_player_not_exist,
                                        it.b,
                                        context = this@WhitelistPlayerView.context
                                    )
                                ).show()
                            latch.countDown()
                        })
                    }
                }
            }
            .setNegativeButton("No") { _: DialogInterface?, _: Int ->

            }
            .create()
        dialog.show()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.whitelist_player_view, this)
        setOnClickListener { view: View -> displayActions(view) }
    }

    fun setAttribute(
        playerName: String,
        fromWhitelist: String,
        activity: WhitelistEditActivity
    ): WhitelistPlayerView {
        playerNameText.text = playerName
        this.fromWhitelist = fromWhitelist
        this.activity = activity
        externalScope = activity.lifecycleScope.plus(coroutineExceptionHandler)
        return this
    }
}
package icu.takeneko.omms.connect.whitelist.view

import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import icu.takeneko.omms.connect.R
import icu.takeneko.omms.connect.client.Connection.getClientSession
import icu.takeneko.omms.connect.util.format
import icu.takeneko.omms.connect.whitelist.activity.WhitelistEditActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.util.concurrent.ExecutionException

class WhitelistPlayerView : ConstraintLayout {
    private lateinit var playerNameText: TextView
    private lateinit var fromWhitelist: String
    private lateinit var activity: WhitelistEditActivity
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
        Log.wtf("OMMS", "this should not happen", e)
    }

    private lateinit var externalScope: CoroutineScope

    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.whitelist_player_view, this)

        setOnClickListener { displayActions(it) }
        playerNameText = findViewById(R.id.playerNameText)
        setOnClickListener { displayActions(it) }
    }

    private fun displayActions(view: View) {
        val dialog = MaterialAlertDialogBuilder(activity)
            .setCancelable(true)
            .setTitle(R.string.confirm)
            .setIcon(R.drawable.ic_baseline_question_24)
            .setMessage(
                activity.format(
                    R.string.hint_whitelist_remove_confirm,
                    playerNameText.text,
                    fromWhitelist
                )
            )
            .setPositiveButton(R.string.positive) { _: DialogInterface?, _: Int ->
                externalScope.launch(Dispatchers.IO) {
                    val playerName = playerNameText.text.toString()
                    val session = getClientSession()
                    session.onPermissionDeniedCallback.setCallback {
                        MaterialAlertDialogBuilder(this@WhitelistPlayerView.activity)
                            .setIcon(R.drawable.ic_baseline_error_24)
                            .setTitle(R.string.error)
                            .setMessage(
                                activity.format(
                                    R.string.hint_whitelist_remove_permission_denied,
                                    playerNameText.text
                                )
                            )
                            .setPositiveButton("OK", null)
                            .show()
                        session.onPermissionDeniedCallback.setCallback {}
                    }
                    try {
                        session.removeFromWhitelist(fromWhitelist, playerName).get()
                        this.launch(Dispatchers.Main) {
                            MaterialAlertDialogBuilder(activity)
                                .setPositiveButton(R.string.ok, null)
                                .setTitle(R.string.success)
                                .setMessage(
                                    activity.format(
                                        R.string.hint_whitelist_player_removed,
                                        playerName
                                    )
                                ).show()
                            activity.requireRefresh = true
                            activity.removePlayer(playerName)
                            activity.refreshPlayerList()
                        }
                    } catch (e: ExecutionException) {
                        launch(Dispatchers.Main) {
                            MaterialAlertDialogBuilder(activity)
                                .setPositiveButton(R.string.ok, null)
                                .setTitle(R.string.error)
                                .setMessage(
                                    activity.format(
                                        R.string.hint_whitelist_remove_player_not_exist,
                                        playerName
                                    )
                                ).show()
                        }
                    }
                }
            }
            .setNegativeButton(R.string.negative) { _: DialogInterface?, _: Int ->

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
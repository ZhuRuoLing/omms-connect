package net.zhuruoling.ommsconnect.ui.whitelist.view

import android.content.Context
import net.zhuruoling.ommsconnect.client.Connection.getClientSession
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.TextView
import net.zhuruoling.ommsconnect.WhitelistEditActivity
import android.view.LayoutInflater
import net.zhuruoling.ommsconnect.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.content.DialogInterface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import net.zhuruoling.omms.client.util.Result
import com.blankj.utilcode.util.ToastUtils
import kotlinx.coroutines.*

class WhitelistCompoentView : ConstraintLayout {
    private var playerNameText: TextView? = null
    private var fromWhitelist: String? = null
    private var activity: WhitelistEditActivity? = null
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        ToastUtils.showLong("Failed connect to server\nreason:$e")
        Log.e("wdnmd","FUCK", e)
    }

    private lateinit var externalScope: CoroutineScope

    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.whitelist_compoent_view, this)
        setOnClickListener { view: View -> displayActions(view) }
        playerNameText = findViewById(R.id.playerNameText)
        setOnClickListener { view: View -> displayActions(view) }
    }

    private fun displayActions(view: View) {
        val dialog = MaterialAlertDialogBuilder(activity!!)
            .setCancelable(true)
            .setTitle("Confirm")
            .setMessage(
                String.format(
                    "Are you sure to remove this player?\n player:%s\n whitelist: %s",
                    playerNameText!!.text,
                    fromWhitelist
                )
            )
            .setPositiveButton("Yes") { dialog1: DialogInterface?, which: Int ->
                externalScope.launch(Dispatchers.IO) {
                    val playerName = playerNameText!!.text.toString()
                    val session = getClientSession()

                    val result = session.removeFromWhitelist(fromWhitelist, playerName)
                    if (result != Result.OK) {
                        MaterialAlertDialogBuilder(activity!!)
                            .setPositiveButton("Ok", null)
                            .setTitle("Fail")
                            .setMessage(
                                String.format(
                                    "Failed to remove %s \n reason:%s",
                                    playerName,
                                    result.name
                                )
                            ).show()

                    } else {
                        this.launch (Dispatchers.Main){
                            MaterialAlertDialogBuilder(activity!!)
                                .setPositiveButton("Ok", null)
                                .setTitle("Success")
                                .setMessage(
                                    String.format(
                                        "Successfully to removed %s",
                                        playerName,
                                    )
                                ).show()
                        }
                    }
                }
            }
            .setNegativeButton("No", null)
            .create()
        dialog.show()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.whitelist_compoent_view, this)
        setOnClickListener { view: View -> displayActions(view) }
    }

    fun setAttribute(
        playerName: String,
        fromWhitelist: String,
        activity: WhitelistEditActivity
    ): WhitelistCompoentView {
        playerNameText!!.text = playerName
        this.fromWhitelist = fromWhitelist
        this.activity = activity
        externalScope = activity.lifecycleScope.plus(coroutineExceptionHandler)
        return this
    }
}
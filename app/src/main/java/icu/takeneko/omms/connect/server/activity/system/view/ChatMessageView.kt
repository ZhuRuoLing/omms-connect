package icu.takeneko.omms.connect.server.activity.system.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import icu.takeneko.omms.client.data.chatbridge.Broadcast
import icu.takeneko.omms.connect.R
import icu.takeneko.omms.connect.util.asColor

class ChatMessageView : LinearLayout {

    private lateinit var card: MaterialCardView
    private lateinit var chatServer: MaterialTextView
    private lateinit var chatId: MaterialTextView
    private lateinit var chatName: MaterialTextView
    private lateinit var chatContent: MaterialTextView

    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.chat_message_view, this)
        initViews()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.chat_message_view, this)
        initViews()
    }

    private fun initViews() {
        card = findViewById(R.id.card_chat_background)
        chatId = findViewById(R.id.text_chat_id)
        chatName = findViewById(R.id.text_chat_name)
        chatServer = findViewById(R.id.text_chat_server)
        chatContent = findViewById(R.id.text_chat_content)
    }

    fun updateContent(br: Broadcast, context: Context) {
        card.setCardBackgroundColor(br.findExactBackgroundResource().asColor(context))
        chatName.text = br.player
        chatServer.text = "[${br.server}]"
        chatId.text = br.id
        chatContent.text = br.content
    }

    private fun Broadcast.findExactBackgroundResource():Int{
        if (this.player == "Server" || this.player == "-Server-")
            return R.color.server_chat_background
        if (this.server == "OMMS CENTRAL")
            return R.color.client_chat_background
        return R.color.player_chat_background
    }
}


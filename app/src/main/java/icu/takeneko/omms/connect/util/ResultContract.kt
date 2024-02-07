package icu.takeneko.omms.connect.util

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import icu.takeneko.omms.connect.whitelist.activity.WhitelistEditActivity

class ResultContract(private val key:String, private val value: Int) : ActivityResultContract<Int, Boolean>() {
    override fun createIntent(context: Context, input: Int): Intent {
        val intent = Intent(context, WhitelistEditActivity::class.java)
        intent.putExtra("check", input)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return if (resultCode == value) {
            intent!!.getBooleanExtra(key, false)
        } else {
            false
        }
    }

}
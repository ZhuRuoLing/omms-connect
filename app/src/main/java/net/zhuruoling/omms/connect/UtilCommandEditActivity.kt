package net.zhuruoling.omms.connect

import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import net.zhuruoling.omms.connect.databinding.ActivityUtilCommandEditBinding
import net.zhuruoling.omms.connect.storage.PreferencesStorage
import net.zhuruoling.omms.connect.ui.util.showErrorDialog
import net.zhuruoling.omms.connect.ui.view.CommandTextCard

class UtilCommandEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUtilCommandEditBinding
    private var commandList = mutableListOf<String>()
    private lateinit var storage: PreferencesStorage
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityUtilCommandEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationOnClickListener {
            this.finish()
        }
        binding.fab.setOnClickListener {
            showCommandEditAction(true,null)
        }
        storage = PreferencesStorage.withContext(this, "util_command")
        refreshCommandList()
    }

    private fun putCommandListIntoPreferences(){
        storage.putStringSet("util_commands", commandList.toSortedSet())
        storage.commit()
    }

    private fun refreshCommandList(){
        this.binding.utilCommandList.removeAllViews()
        Log.i("omms", "refresh list")
        commandList = storage.getStringSet("util_commands", mutableSetOf()).toMutableList()
        Log.i("omms", commandList.joinToString(separator = ", "))
        commandList.forEach {
            this.binding.utilCommandList.addView(makeButton(it))
        }
    }

    private fun applyCommandEdit(before: String, after: String) {
        if (commandList.contains(after)){
            showErrorDialog("Command \"$after\" already exists.", this)
            return
        }
        commandList.remove(before)
        commandList.add(after)
        putCommandListIntoPreferences()
        refreshCommandList()
    }

    private fun applyCommandRemove(command: String) {
        commandList.remove(command)
        putCommandListIntoPreferences()
        refreshCommandList()
    }

    private fun applyCommandAdd(command: String) {
        if (commandList.contains(command)){
            showErrorDialog("Command \"$command\" already exists.", this)
            return
        }
        commandList.add(command)
        putCommandListIntoPreferences()
        refreshCommandList()
    }

    private fun makeButton(text: String): CommandTextCard {
        return CommandTextCard(this).setValue(text, this)
    }

    fun showCommandEditAction(add: Boolean, before: String?) {
        val textView = TextInputEditText(this)
        if (!add) {
            assert(before != null)
            textView.text = Editable.Factory.getInstance().newEditable(before)
        }
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(textView)
            .setCancelable(true)
            .setTitle("Edit Command")
            .setPositiveButton("Done") { _, _ ->
                val result = textView.text.toString()
                if (result.isNotEmpty()) {
                    if (add) {
                        Log.i("omms", "Done $result")
                        applyCommandAdd(result)
                    } else {
                        Log.i("omms", "Done $before -> $result")
                        if (before != result) {
                            applyCommandEdit(before!!, result)
                        }
                    }
                }
            }
            .setNeutralButton("Back") { _, _ ->
                Log.i("omms-crystal", "Back")
            }
        if (!add) {
            dialog.setNegativeButton("Remove") { _, _ ->
                Log.i("omms-crystal", "Remove $before")
                applyCommandRemove(before!!)
            }
        }
        dialog.show()
    }
}
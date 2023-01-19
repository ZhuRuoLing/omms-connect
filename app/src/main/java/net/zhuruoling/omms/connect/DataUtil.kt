package net.zhuruoling.omms.connect

import android.content.Context
import android.graphics.Region
import java.util.Base64
import net.zhuruoling.omms.connect.storage.PreferencesStorage
import net.zhuruoling.omms.connect.ui.util.fromJson
import net.zhuruoling.omms.connect.ui.util.toJson

private data class ExportDataStorage(
    val loginData: LoginData?,
    val utilCommands: MutableList<String>
)

private data class LoginData(val ip: String, val port: String, val code: String?)

fun toExportDataJson(context: Context): String {
    val preferencesStorage = PreferencesStorage.withContext(context, "login")
    val preferencesStorage2 = PreferencesStorage.withContext(context, "util_command")
    val data = ExportDataStorage(
        if (preferencesStorage.contains("server_ip")) LoginData(
            preferencesStorage.getString("server_ip", ""),
            preferencesStorage.getString("server_port", "50000"),
            if (preferencesStorage.contains("server_code")) preferencesStorage.getString(
                "server_code",
                ""
            ) else null
        ) else null,
        if (preferencesStorage2.contains("util_commands")) preferencesStorage2.getStringSet(
            "util_commands",
            mutableSetOf()
        ).toMutableList() else mutableListOf()
    )
    return Base64.getEncoder().encodeToString(toJson(data).encodeToByteArray())
}

fun importDataFromJson(context: Context, json: String) {
    val data = fromJson(String(Base64.getDecoder().decode(json)), ExportDataStorage::class.java)
    val loginPreferencesStorage = PreferencesStorage.withContext(context, "login")
    val utilCommandPreferencesStorage = PreferencesStorage.withContext(context, "util_command")
    if (data.loginData != null) {
        loginPreferencesStorage.putString("server_ip", data.loginData.ip)
        loginPreferencesStorage.putString("server_port", data.loginData.port)
        if (data.loginData.code != null) {
            loginPreferencesStorage.putString("server_code", data.loginData.code)
        }
    }
    utilCommandPreferencesStorage.putStringSet("util_commands", data.utilCommands.toSortedSet())
    loginPreferencesStorage.commit()
    utilCommandPreferencesStorage.commit()
}
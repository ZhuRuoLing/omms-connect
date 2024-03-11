package icu.takeneko.omms.connect.util

import androidx.annotation.DrawableRes
import icu.takeneko.omms.connect.R

fun getSystemType(origin: String): String {
    return if (origin.contains("Windows")) "WINDOWS" else (if (origin.contains("Linux") || origin.contains(
            "linux"
        )
    ) "LINUX" else "")
}

fun determineSystemType(desc: String): SystemType {
    val d = desc.lowercase()
    if ("windows" in d)
        return SystemType.WINDOWS
    if ("linux" in d)
        return SystemType.LINUX
    if ("darwin" in d || "mac os x" in d)
        return SystemType.MACOS
    return SystemType.UNKNOWN
}

enum class SystemType(@DrawableRes val iconId:Int) {
    WINDOWS(R.drawable.ic_server_windows),
    LINUX(R.drawable.ic_server_linux),
    MACOS(R.drawable.ic_baseline_question_24),
    UNKNOWN(R.drawable.ic_baseline_question_24)
}
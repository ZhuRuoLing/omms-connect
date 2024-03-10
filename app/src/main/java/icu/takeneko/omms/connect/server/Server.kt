package icu.takeneko.omms.connect.server

fun getSystemType(origin: String): String {
    return if (origin.contains("Windows")) "WINDOWS" else (if (origin.contains("Linux") || origin.contains(
            "linux"
        )
    ) "LINUX" else "")
}
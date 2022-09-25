package net.zhuruoling.ommsconnect.ui.util

import net.zhuruoling.omms.client.controller.Controller

fun genControllerIntroText(controller: Controller): String{
    val stringBuilder = StringBuilder()
    stringBuilder.append("Type:")
    stringBuilder.append(controller.type)
    stringBuilder.append(" ")
    return stringBuilder.toString()
}

fun getSystemType(origin: String): String{
    return if (origin.contains("Windows")) "WINDOWS" else (if (origin.contains("Linux") || origin.contains("linux")) "LINUX" else "")
}
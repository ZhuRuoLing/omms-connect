package net.zhuruoling.omms.connect.util

import java.util.concurrent.CountDownLatch

fun awaitExecute(block: (CountDownLatch) -> Unit) {
    val latch = CountDownLatch(1)
    block(latch)
    latch.await()
}
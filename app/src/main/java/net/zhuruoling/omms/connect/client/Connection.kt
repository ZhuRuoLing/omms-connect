package net.zhuruoling.omms.connect.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import net.zhuruoling.omms.client.session.ClientInitialSession
import net.zhuruoling.omms.client.session.ClientSession
import net.zhuruoling.omms.connect.util.awaitExecute
import java.net.InetAddress
import java.util.concurrent.CountDownLatch
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

object Connection {
    private lateinit var clientSession: ClientSession
    var isConnected = false

    sealed class Result<out R> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val exception: Exception) : Result<Nothing>()
    }

    suspend fun init(ip: String, port: Int, code: Int, forceConnect: Boolean): Result<Response> {
        if (forceConnect) {
            if (isConnected) {
                end()
            }
        }
        if (isConnected) {
            return Result.Error(RuntimeException("Already connected to this server."))
        }
        return withContext(Dispatchers.IO) {
            try {
                val clientInitialSession = ClientInitialSession(InetAddress.getByName(ip), port)
                this.ensureActive()
                val task = FutureTask {
                    val session = clientInitialSession.init(code)
                    isConnected = true
                    return@FutureTask session
                }
                task.run()
                val res = task[5000, TimeUnit.MILLISECONDS]
                clientSession = res
                return@withContext Result.Success(Response.SUCCESS)
            } catch (e: Exception) {
                return@withContext Result.Error(e)
            }
        }
    }

    fun getClientSession(): ClientSession {
        return clientSession
    }

    suspend fun end() {
        return withContext(Dispatchers.IO) {
            try {
                val latch = CountDownLatch(1)
                clientSession.close {
                    isConnected = false
                    latch.countDown()
                }
                latch.await(1000, TimeUnit.MILLISECONDS)
            } catch (_: java.lang.Exception) {

            }
        }
    }
}
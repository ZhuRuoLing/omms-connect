package net.zhuruoling.omms.connect.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import net.zhuruoling.omms.client.session.ClientInitialSession
import net.zhuruoling.omms.client.session.ClientSession
import java.net.InetAddress
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

object Connection {
    private lateinit var clientSession: ClientSession
    var isConnected = false

    sealed class Result<out R> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val exception: Exception) : Result<Nothing>()
    }

    suspend fun init(ip: String, port: Int, code: Int): Result<Response> {
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

    suspend fun end(): Result<Response> {
        return withContext(Dispatchers.IO) {
            try {
                clientSession.close()
                isConnected = false
                return@withContext Result.Success(Response.DISCONNECTED)
            } catch (e: RuntimeException) {
                return@withContext Result.Error(e)
            }
        }
    }
}
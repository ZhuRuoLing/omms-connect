package icu.takeneko.omms.connect.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import icu.takeneko.omms.client.session.ClientInitialSession
import icu.takeneko.omms.client.session.ClientSession
import java.net.InetAddress
import java.util.concurrent.CountDownLatch
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

object Connection {
    private lateinit var clientSession: ClientSession
    val isConnected
        get() = connectionStatus == ConnectionStatus.CONNECTED
    private var connectionStatus = ConnectionStatus.DISCONNECTED

    sealed class Result<out R> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val exception: Throwable) : Result<Nothing>()
    }

    suspend fun connect(
        ip: String,
        port: Int,
        code: String,
        forceConnect: Boolean
    ): Result<ConnectionStatus> {
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
                    val tok = ClientInitialSession.generateToken(code)
                    val session = clientInitialSession.init(tok)
                    connectionStatus = ConnectionStatus.CONNECTED
                    return@FutureTask session
                }
                task.run()
                val res = task[5000, TimeUnit.MILLISECONDS]
                clientSession = res
                return@withContext Result.Success(connectionStatus)
            } catch (e: Throwable) {
                connectionStatus = ConnectionStatus.ERROR
                return@withContext Result.Error(e)
            }
        }
    }

    fun getClientSession(): ClientSession {
        return clientSession
    }

    suspend fun end() {
        return withContext(Dispatchers.IO) {
            connectionStatus = ConnectionStatus.DISCONNECTED
            clientSession.close {
                connectionStatus = ConnectionStatus.DISCONNECTED
            }
        }
    }
}
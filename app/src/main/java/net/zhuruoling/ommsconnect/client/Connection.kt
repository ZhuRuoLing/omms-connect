package net.zhuruoling.ommsconnect.client

import kotlinx.coroutines.*
import net.zhuruoling.omms.client.server.session.ClientInitialSession
import net.zhuruoling.omms.client.server.session.ClientSession
import java.net.InetAddress
import kotlin.RuntimeException

object Connection {
    private lateinit var clientSession: ClientSession
    var isConnected = false
    sealed class Result<out R> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val exception: Exception) : Result<Nothing>()
    }

    suspend fun init(ip: String, port: Int, code:Int):Result<Response> {
        if (isConnected){
            return Result.Error(RuntimeException("Already connected to this server."))
        }
        val clientInitialSession = ClientInitialSession(InetAddress.getByName(ip), port)
        return withContext(Dispatchers.IO){
            try {
                clientSession = clientInitialSession.init(code)
                isConnected = true
                return@withContext Result.Success(Response.SUCCESS)
            }
            catch (e:RuntimeException){
                return@withContext Result.Error(e)
            }
        }
    }

    fun getClientSession():ClientSession{
        return clientSession
    }

    suspend fun end(): Result<Response>{
        return withContext(Dispatchers.Default){
            try {
                clientSession.close()
                isConnected = false
                return@withContext Result.Success(Response.DISCONNECTED)
            }
            catch (e:RuntimeException){
                return@withContext Result.Error(e)
            }
        }
    }
}
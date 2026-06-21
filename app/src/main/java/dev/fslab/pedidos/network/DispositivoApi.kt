package dev.fslab.pedidos.network

import dev.fslab.pedidos.model.BasicResponse
import dev.fslab.pedidos.model.RegistrarDispositivoRequest
import dev.fslab.pedidos.model.RegistrarDispositivoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Endpoint para registro de dispositivo
 */
interface DispositivoApi {

    /**
     * POST /dispositivos/registrar
     */
    @POST("dispositivos/registrar")
    suspend fun registrar(
        @Body payload: RegistrarDispositivoRequest
    ): Response<RegistrarDispositivoResponse>

    /**
     * POST /dispositivos/desativar-token
     */
    @POST("dispositivos/desativar-token")
    suspend fun desativar(
        @Body payload: Map<String, String>
    ): Response<BasicResponse>
}

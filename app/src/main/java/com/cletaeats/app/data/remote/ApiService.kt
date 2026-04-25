package com.cletaeats.app.data.remote

import com.cletaeats.app.data.model.BasicResponse
import com.cletaeats.app.data.model.ClienteDireccionRequest
import com.cletaeats.app.data.model.ClienteDireccionResponse
import com.cletaeats.app.data.model.LoginRequest
import com.cletaeats.app.data.model.LoginResponse
import com.cletaeats.app.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): BasicResponse

    @GET("api/clientes/{clienteId}/direcciones")
    suspend fun listarDirecciones(
        @Path("clienteId") clienteId: Long
    ): List<ClienteDireccionResponse>

    @POST("api/clientes/{clienteId}/direcciones")
    suspend fun crearDireccion(
        @Path("clienteId") clienteId: Long,
        @Body request: ClienteDireccionRequest
    ): ClienteDireccionResponse

    @PUT("api/clientes/{clienteId}/direcciones/{direccionId}")
    suspend fun actualizarDireccion(
        @Path("clienteId") clienteId: Long,
        @Path("direccionId") direccionId: Long,
        @Body request: ClienteDireccionRequest
    ): ClienteDireccionResponse

    @DELETE("api/clientes/{clienteId}/direcciones/{direccionId}")
    suspend fun eliminarDireccion(
        @Path("clienteId") clienteId: Long,
        @Path("direccionId") direccionId: Long
    )

    @PATCH("api/clientes/{clienteId}/direcciones/{direccionId}/predeterminada")
    suspend fun marcarDireccionPredeterminada(
        @Path("clienteId") clienteId: Long,
        @Path("direccionId") direccionId: Long
    ): ClienteDireccionResponse
}
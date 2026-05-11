package com.cletaeats.app.data.remote

import com.cletaeats.app.data.model.BasicResponse
import com.cletaeats.app.data.model.CalificacionRequest
import com.cletaeats.app.data.model.CalificacionResponse
import com.cletaeats.app.data.model.ClienteDireccionRequest
import com.cletaeats.app.data.model.ClienteDireccionResponse
import com.cletaeats.app.data.model.ComboResponse
import com.cletaeats.app.data.model.LoginRequest
import com.cletaeats.app.data.model.LoginResponse
import com.cletaeats.app.data.model.PedidoCreateRequest
import com.cletaeats.app.data.model.PedidoEstadoRequest
import com.cletaeats.app.data.model.PedidoResponse
import com.cletaeats.app.data.model.QuejaRequest
import com.cletaeats.app.data.model.QuejaResponse
import com.cletaeats.app.data.model.RegisterRequest
import com.cletaeats.app.data.model.RestauranteResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import com.cletaeats.app.data.model.UbicacionRepartidorRequest
import com.cletaeats.app.data.model.UbicacionRepartidorResponse

interface ApiService {

    // AUTH

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): BasicResponse

    // DIRECCIONES CLIENTE

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

    // RESTAURANTES Y COMBOS

    @GET("api/restaurantes")
    suspend fun listarRestaurantes(
        @Query("soloActivos") soloActivos: Boolean = true
    ): List<RestauranteResponse>

    @GET("api/restaurantes/{id}")
    suspend fun obtenerRestaurante(
        @Path("id") id: Long
    ): RestauranteResponse

    @GET("api/restaurantes/{restauranteId}/combos")
    suspend fun listarCombosPorRestaurante(
        @Path("restauranteId") restauranteId: Long,
        @Query("soloActivos") soloActivos: Boolean = true
    ): List<ComboResponse>

    // PEDIDOS CLIENTE

    @POST("api/clientes/pedidos")
    suspend fun crearPedido(
        @Body request: PedidoCreateRequest
    ): PedidoResponse

    @GET("api/clientes/pedidos/mis-pedidos")
    suspend fun listarMisPedidosCliente(): List<PedidoResponse>

    @GET("api/clientes/pedidos/{pedidoId}")
    suspend fun obtenerPedidoCliente(
        @Path("pedidoId") pedidoId: Long
    ): PedidoResponse

    // FEEDBACK CLIENTE

    @POST("api/clientes/pedidos/{pedidoId}/calificacion")
    suspend fun registrarCalificacion(
        @Path("pedidoId") pedidoId: Long,
        @Body request: CalificacionRequest
    ): CalificacionResponse

    @POST("api/clientes/pedidos/{pedidoId}/queja")
    suspend fun registrarQueja(
        @Path("pedidoId") pedidoId: Long,
        @Body request: QuejaRequest
    ): QuejaResponse

    // PEDIDOS REPARTIDOR

    @GET("api/repartidores/pedidos/mis-pedidos")
    suspend fun listarMisPedidosRepartidor(): List<PedidoResponse>

    @GET("api/repartidores/pedidos/{pedidoId}")
    suspend fun obtenerPedidoRepartidor(
        @Path("pedidoId") pedidoId: Long
    ): PedidoResponse

    @PATCH("api/repartidores/pedidos/{pedidoId}/estado")
    suspend fun actualizarEstadoPedidoRepartidor(
        @Path("pedidoId") pedidoId: Long,
        @Body request: PedidoEstadoRequest
    ): PedidoResponse

    @PATCH("api/repartidores/ubicacion")
    suspend fun actualizarUbicacionRepartidor(
        @Body request: UbicacionRepartidorRequest
    ): UbicacionRepartidorResponse

    @GET("api/clientes/pedidos/{pedidoId}/tracking")
    suspend fun obtenerTrackingPedido(
        @Path("pedidoId") pedidoId: Long
    ): UbicacionRepartidorResponse
}
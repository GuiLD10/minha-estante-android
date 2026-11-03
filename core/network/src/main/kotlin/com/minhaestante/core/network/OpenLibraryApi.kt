package com.minhaestante.core.network

import com.minhaestante.core.network.dto.BuscaResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Contrato HTTP da Open Library. O Retrofit gera a implementação a partir das anotações.
 *
 * `suspend`: o Retrofit executa a chamada fora da thread principal e suspende a coroutine
 * até a resposta chegar. Falha de conexão → lança `IOException`; resposta com código
 * diferente de 2xx → lança `HttpException`.
 */
internal interface OpenLibraryApi {

    @GET("search.json")
    suspend fun buscar(
        @Query("q") termo: String,
        @Query("limit") limite: Int,
        @Query("fields") campos: String,
    ): BuscaResponse
}

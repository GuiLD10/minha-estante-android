package com.minhaestante.core.network

import com.minhaestante.core.model.ResultadoBusca

/**
 * Porta de entrada pública deste módulo: busca livros na rede e já devolve modelos de domínio.
 *
 * Assim, `:core:data` não precisa conhecer Retrofit, DTOs nem a [OpenLibraryApi] (todos `internal`).
 * Erros de rede (`IOException`, `HttpException`) **não** são tratados aqui: sobem para quem chamou.
 */
interface BuscaRemoteDataSource {
    suspend fun buscar(termo: String): List<ResultadoBusca>
}

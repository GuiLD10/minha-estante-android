package com.minhaestante.core.data.repository

import com.minhaestante.core.model.ResultadoBusca

/**
 * Busca de livros na Open Library.
 *
 * Os resultados vêm sempre com `naEstante = false`: quem cruza com os ids salvos
 * é o ViewModel, combinando com [EstanteRepository.observarIdsNaEstante].
 *
 * Erros de rede **não** são capturados aqui: `IOException` e `HttpException` sobem
 * para o ViewModel, que decide como mostrá-los.
 */
interface BuscaRepository {
    suspend fun buscar(termo: String): List<ResultadoBusca>
}

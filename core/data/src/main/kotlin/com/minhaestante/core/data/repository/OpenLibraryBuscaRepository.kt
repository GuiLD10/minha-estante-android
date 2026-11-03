package com.minhaestante.core.data.repository

import com.minhaestante.core.model.ResultadoBusca
import com.minhaestante.core.network.BuscaRemoteDataSource
import javax.inject.Inject

/**
 * Implementação de [BuscaRepository] que consulta apenas a rede (a busca não tem cache offline).
 *
 * Hoje ela só repassa a chamada, mas é aqui que entraria qualquer combinação de fontes
 * (ex.: cache). Os ViewModels continuam dependendo apenas da interface.
 */
internal class OpenLibraryBuscaRepository @Inject constructor(
    private val remoteDataSource: BuscaRemoteDataSource,
) : BuscaRepository {

    override suspend fun buscar(termo: String): List<ResultadoBusca> =
        remoteDataSource.buscar(termo)
}

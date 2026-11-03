package com.minhaestante.core.network

import com.minhaestante.core.model.ResultadoBusca
import com.minhaestante.core.network.dto.toResultadoBusca
import javax.inject.Inject

/** Implementação de [BuscaRemoteDataSource] usando o Retrofit. */
internal class RetrofitBuscaRemoteDataSource @Inject constructor(
    private val api: OpenLibraryApi,
) : BuscaRemoteDataSource {

    override suspend fun buscar(termo: String): List<ResultadoBusca> =
        api.buscar(termo = termo, limite = LIMITE_RESULTADOS, campos = CAMPOS)
            .docs
            .map { it.toResultadoBusca() }

    private companion object {
        const val LIMITE_RESULTADOS = 20

        /** Pede só os campos que usamos: a resposta fica bem menor. */
        const val CAMPOS =
            "key,title,author_name,first_publish_year,cover_i,number_of_pages_median"
    }
}

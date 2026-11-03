package com.minhaestante.feature.busca

import com.minhaestante.core.data.repository.BuscaRepository
import com.minhaestante.core.model.ResultadoBusca

/**
 * Busca em memória. O teste define a [resposta] (pode devolver uma lista, lançar exceção
 * ou suspender) e confere em [termosBuscados] quais buscas foram feitas.
 */
class FakeBuscaRepository : BuscaRepository {

    val termosBuscados = mutableListOf<String>()

    var resposta: suspend (termo: String) -> List<ResultadoBusca> = { emptyList() }

    override suspend fun buscar(termo: String): List<ResultadoBusca> {
        termosBuscados += termo
        return resposta(termo)
    }
}

fun resultadoTeste(id: String, naEstante: Boolean = false) = ResultadoBusca(
    id = id,
    titulo = "Livro $id",
    autores = listOf("Autor"),
    anoPublicacao = 2000,
    capaId = null,
    totalPaginas = 100,
    naEstante = naEstante,
)

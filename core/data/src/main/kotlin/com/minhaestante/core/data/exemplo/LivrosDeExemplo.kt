package com.minhaestante.core.data.exemplo

import com.minhaestante.core.data.repository.EstanteRepository
import com.minhaestante.core.model.ResultadoBusca
import com.minhaestante.core.model.StatusLeitura

/*
 * CÓDIGO TEMPORÁRIO (etapa 3): popula a estante com livros de exemplo enquanto
 * ainda não existe a tela de Busca. Deve ser removido na etapa 5.
 */

private val exemplos = listOf(
    ResultadoBusca(
        id = "/works/OL27448W",
        titulo = "The Lord of the Rings",
        autores = listOf("J. R. R. Tolkien"),
        anoPublicacao = 1954,
        capaId = 14625765L,
        totalPaginas = 1193,
        naEstante = false,
    ),
    ResultadoBusca(
        id = "/works/OL1168083W",
        titulo = "Dom Casmurro",
        autores = listOf("Machado de Assis"),
        anoPublicacao = 1899,
        capaId = null,
        totalPaginas = 256,
        naEstante = false,
    ),
    ResultadoBusca(
        id = "/works/OL45804W",
        titulo = "Fantastic Mr Fox",
        autores = listOf("Roald Dahl"),
        anoPublicacao = 1970,
        capaId = 6498519L,
        totalPaginas = 96,
        naEstante = false,
    ),
    ResultadoBusca(
        id = "/works/OL453936W",
        titulo = "Good Omens",
        autores = listOf("Neil Gaiman", "Terry Pratchett"),
        anoPublicacao = 1990,
        capaId = null,
        totalPaginas = null,
        naEstante = false,
    ),
)

/**
 * Insere os exemplos se ainda não existirem. Como `adicionar` ignora duplicados,
 * na prática isso só tem efeito na primeira execução.
 */
suspend fun inserirLivrosDeExemplo(repository: EstanteRepository) {
    exemplos.forEach { exemplo ->
        val inseriu = repository.adicionar(exemplo)
        if (inseriu) ajustarLeitura(repository, exemplo.id)
    }
}

/** Coloca alguns livros em status diferentes para o filtro ter o que mostrar. */
private suspend fun ajustarLeitura(repository: EstanteRepository, id: String) {
    when (id) {
        "/works/OL27448W" -> repository.atualizarProgresso(id, pagina = 420)
        "/works/OL45804W" -> repository.atualizarStatus(id, StatusLeitura.LIDO)
        "/works/OL453936W" -> repository.atualizarProgresso(id, pagina = 75)
    }
}

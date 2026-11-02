package com.minhaestante.core.model

/**
 * Cria um [Livro] com valores padrão, para que cada teste declare
 * apenas os campos que realmente importam para ele.
 */
fun livro(
    totalPaginas: Int? = 300,
    paginaAtual: Int = 0,
    status: StatusLeitura = StatusLeitura.QUERO_LER,
    autores: List<String> = listOf("J. R. R. Tolkien"),
) = Livro(
    id = "/works/OL27448W",
    titulo = "O Senhor dos Anéis",
    autores = autores,
    anoPublicacao = 1954,
    capaId = 12345L,
    totalPaginas = totalPaginas,
    status = status,
    paginaAtual = paginaAtual,
    adicionadoEm = 1_700_000_000_000L,
)

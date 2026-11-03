package com.minhaestante.core.model

/**
 * Item retornado pela busca na Open Library.
 *
 * Diferente de [Livro], não tem status nem progresso: só passa a ter
 * quando o usuário o adiciona à estante (ver [paraLivro]).
 */
data class ResultadoBusca(
    val id: String,
    val titulo: String,
    val autores: List<String>,
    val anoPublicacao: Int?,
    val capaId: Long?,
    val totalPaginas: Int?,
    /** Calculado cruzando com os ids já salvos na estante. */
    val naEstante: Boolean,
)

/**
 * Cria o [Livro] que será salvo na estante: começa em [StatusLeitura.QUERO_LER], na página zero.
 *
 * @param agora momento atual em epoch millis. Recebido como parâmetro (em vez de chamar
 * `System.currentTimeMillis()` aqui dentro) para que a função continue pura e testável.
 */
fun ResultadoBusca.paraLivro(agora: Long): Livro = Livro(
    id = id,
    titulo = titulo,
    autores = autores,
    anoPublicacao = anoPublicacao,
    capaId = capaId,
    totalPaginas = totalPaginas,
    status = StatusLeitura.QUERO_LER,
    paginaAtual = PAGINA_INICIAL,
    adicionadoEm = agora,
)

/** Mesma regra de [Livro.autoresFormatados], usada na lista de resultados da Busca. */
fun ResultadoBusca.autoresFormatados(): String = formatarAutores(autores)

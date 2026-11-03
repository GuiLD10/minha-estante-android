package com.minhaestante.core.model

/**
 * Livro salvo na estante do usuário.
 *
 * É um modelo de domínio: não sabe nada de rede (DTO) nem de banco (Entity).
 * Todas as regras abaixo são funções puras: recebem um [Livro] e devolvem
 * um **novo** [Livro] (graças ao `copy`), sem alterar o original.
 */
data class Livro(
    /** "key" da Open Library, ex.: "/works/OL45804W". */
    val id: String,
    val titulo: String,
    val autores: List<String>,
    val anoPublicacao: Int?,
    /** "cover_i" da Open Library. */
    val capaId: Long?,
    /** "number_of_pages_median"; pode ser desconhecido. */
    val totalPaginas: Int?,
    val status: StatusLeitura,
    val paginaAtual: Int,
    /** Momento em que foi adicionado, em epoch millis. */
    val adicionadoEm: Long,
)

const val AUTOR_DESCONHECIDO = "Autor desconhecido"

private const val PERCENTUAL_MAXIMO = 100
internal const val PAGINA_INICIAL = 0
private const val SEPARADOR_AUTORES = ", "

/**
 * Total de páginas apenas quando é útil para cálculos.
 * Zero ou valores negativos são tratados como desconhecidos (evita divisão por zero).
 */
private val Livro.totalPaginasConhecido: Int?
    get() = totalPaginas?.takeIf { it > 0 }

/**
 * Percentual lido (0 a 100), ou `null` quando o total de páginas é desconhecido.
 */
fun Livro.progressoPercentual(): Int? {
    val total = totalPaginasConhecido ?: return null
    return (paginaAtual * PERCENTUAL_MAXIMO / total).coerceIn(0, PERCENTUAL_MAXIMO)
}

/**
 * Atualiza a página atual aplicando as regras de negócio:
 * - a página é limitada a `0..totalPaginas` (ou apenas `>= 0` se o total for desconhecido);
 * - ao chegar na última página, o livro passa para [StatusLeitura.LIDO];
 * - se estava em [StatusLeitura.QUERO_LER] e a página for maior que zero, passa para [StatusLeitura.LENDO].
 */
fun Livro.comProgresso(pagina: Int): Livro {
    val total = totalPaginasConhecido
    val paginaLimitada = if (total != null) {
        pagina.coerceIn(PAGINA_INICIAL, total)
    } else {
        pagina.coerceAtLeast(PAGINA_INICIAL)
    }

    val novoStatus = when {
        total != null && paginaLimitada == total -> StatusLeitura.LIDO
        status == StatusLeitura.QUERO_LER && paginaLimitada > PAGINA_INICIAL -> StatusLeitura.LENDO
        else -> status
    }

    return copy(paginaAtual = paginaLimitada, status = novoStatus)
}

/**
 * Muda o status ajustando a página atual de forma coerente:
 * - [StatusLeitura.LIDO] → página atual vai para o total (se conhecido);
 * - [StatusLeitura.QUERO_LER] → página atual volta para zero;
 * - [StatusLeitura.LENDO] → mantém a página atual.
 */
fun Livro.comStatus(novo: StatusLeitura): Livro {
    val novaPagina = when (novo) {
        StatusLeitura.LIDO -> totalPaginasConhecido ?: paginaAtual
        StatusLeitura.QUERO_LER -> PAGINA_INICIAL
        StatusLeitura.LENDO -> paginaAtual
    }
    return copy(status = novo, paginaAtual = novaPagina)
}

/** Autores separados por vírgula, ou [AUTOR_DESCONHECIDO] se não houver nenhum. */
fun Livro.autoresFormatados(): String = formatarAutores(autores)

/**
 * Regra compartilhada entre [Livro] e [ResultadoBusca]: ignora nomes em branco,
 * junta com vírgula ou devolve [AUTOR_DESCONHECIDO].
 */
internal fun formatarAutores(autores: List<String>): String {
    val validos = autores.filter { it.isNotBlank() }
    return if (validos.isEmpty()) AUTOR_DESCONHECIDO else validos.joinToString(SEPARADOR_AUTORES)
}

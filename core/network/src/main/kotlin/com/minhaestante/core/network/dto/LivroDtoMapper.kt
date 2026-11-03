package com.minhaestante.core.network.dto

import com.minhaestante.core.model.ResultadoBusca

/**
 * Converte o DTO da rede para o modelo de domínio.
 *
 * `naEstante` sempre começa `false`: a rede não sabe o que está salvo no banco.
 * Quem cruza com os ids da estante é o ViewModel da Busca.
 */
internal fun LivroDto.toResultadoBusca(): ResultadoBusca = ResultadoBusca(
    id = key,
    titulo = title,
    autores = autores,
    anoPublicacao = ano,
    capaId = capaId,
    totalPaginas = paginas,
    naEstante = false,
)

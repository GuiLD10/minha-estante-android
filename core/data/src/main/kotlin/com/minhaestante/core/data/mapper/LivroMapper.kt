package com.minhaestante.core.data.mapper

import com.minhaestante.core.database.entity.LivroEntity
import com.minhaestante.core.model.Livro

/**
 * Mappers entre a camada de banco e o domínio.
 *
 * Mesmo com campos idênticos hoje, manter [LivroEntity] e [Livro] separados permite
 * mudar o banco (renomear coluna, mudar tipo) sem afetar ViewModels e telas.
 */
internal fun LivroEntity.toDomain(): Livro = Livro(
    id = id,
    titulo = titulo,
    autores = autores,
    anoPublicacao = anoPublicacao,
    capaId = capaId,
    totalPaginas = totalPaginas,
    status = status,
    paginaAtual = paginaAtual,
    adicionadoEm = adicionadoEm,
)

internal fun Livro.toEntity(): LivroEntity = LivroEntity(
    id = id,
    titulo = titulo,
    autores = autores,
    anoPublicacao = anoPublicacao,
    capaId = capaId,
    totalPaginas = totalPaginas,
    status = status,
    paginaAtual = paginaAtual,
    adicionadoEm = adicionadoEm,
)

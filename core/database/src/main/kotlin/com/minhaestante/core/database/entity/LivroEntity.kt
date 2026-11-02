package com.minhaestante.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.minhaestante.core.model.StatusLeitura

/**
 * Linha da tabela `livros`.
 *
 * É pública porque o módulo `:core:data` precisa dela para mapear de/para o modelo
 * de domínio [com.minhaestante.core.model.Livro]. Mesmo assim, nunca chega à UI.
 *
 * `autores` e `status` não são tipos que o SQLite entende; eles são convertidos
 * por [com.minhaestante.core.database.converter.Converters].
 */
@Entity(tableName = "livros")
data class LivroEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val autores: List<String>,
    val anoPublicacao: Int?,
    val capaId: Long?,
    val totalPaginas: Int?,
    val status: StatusLeitura,
    val paginaAtual: Int,
    val adicionadoEm: Long,
)

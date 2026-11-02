package com.minhaestante.core.data.repository

import com.minhaestante.core.data.mapper.toDomain
import com.minhaestante.core.data.mapper.toEntity
import com.minhaestante.core.database.dao.LivroDao
import com.minhaestante.core.model.Livro
import com.minhaestante.core.model.ResultadoBusca
import com.minhaestante.core.model.StatusLeitura
import com.minhaestante.core.model.comProgresso
import com.minhaestante.core.model.comStatus
import com.minhaestante.core.model.paraLivro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Implementação da estante que usa apenas o Room (offline-first).
 *
 * O Repository não decide regras de negócio: ele lê o livro, aplica a regra pura
 * de `:core:model` (`comStatus`, `comProgresso`) e grava o resultado.
 *
 * `internal`: fora deste módulo só se conhece a interface [EstanteRepository].
 */
internal class OfflineFirstEstanteRepository(
    private val livroDao: LivroDao,
) : EstanteRepository {

    override fun observarEstante(filtro: StatusLeitura?): Flow<List<Livro>> {
        val entidades = if (filtro == null) {
            livroDao.observarTodos()
        } else {
            livroDao.observarPorStatus(filtro)
        }
        return entidades.map { lista -> lista.map { it.toDomain() } }
    }

    override fun observarLivro(id: String): Flow<Livro?> =
        livroDao.observarPorId(id).map { it?.toDomain() }

    override fun observarIdsNaEstante(): Flow<Set<String>> =
        livroDao.observarIds().map { it.toSet() }

    override suspend fun adicionar(resultado: ResultadoBusca): Boolean {
        val livro = resultado.paraLivro(agora = System.currentTimeMillis())
        return livroDao.inserir(livro.toEntity()) != ID_JA_EXISTENTE
    }

    override suspend fun atualizarStatus(id: String, status: StatusLeitura) {
        atualizarLivro(id) { it.comStatus(status) }
    }

    override suspend fun atualizarProgresso(id: String, pagina: Int) {
        atualizarLivro(id) { it.comProgresso(pagina) }
    }

    override suspend fun remover(id: String) {
        livroDao.remover(id)
    }

    override suspend fun livrosSendoLidos(): List<Livro> =
        livroDao.buscarLendo().map { it.toDomain() }

    /**
     * Lê o livro atual (com `first()`, que pega só o primeiro valor do Flow),
     * aplica a [transformacao] e grava. Se o livro não existir, não faz nada.
     */
    private suspend fun atualizarLivro(id: String, transformacao: (Livro) -> Livro) {
        val atual = livroDao.observarPorId(id).first()?.toDomain() ?: return
        livroDao.atualizar(transformacao(atual).toEntity())
    }

    private companion object {
        /** Valor que o Room retorna em `@Insert(onConflict = IGNORE)` quando a linha já existe. */
        const val ID_JA_EXISTENTE = -1L
    }
}

package com.minhaestante.core.data.repository

import com.minhaestante.core.model.Livro
import com.minhaestante.core.model.ResultadoBusca
import com.minhaestante.core.model.StatusLeitura
import kotlinx.coroutines.flow.Flow

/**
 * Fonte da verdade da estante do usuário.
 *
 * ViewModels dependem desta **interface**, nunca da implementação. Assim, nos testes,
 * podemos trocar o banco real por um fake em memória.
 */
interface EstanteRepository {

    /** Livros da estante, mais recentes primeiro. `filtro = null` significa todos. */
    fun observarEstante(filtro: StatusLeitura?): Flow<List<Livro>>

    fun observarLivro(id: String): Flow<Livro?>

    fun observarIdsNaEstante(): Flow<Set<String>>

    /** Adiciona o resultado à estante. Retorna `false` se o livro já estava lá. */
    suspend fun adicionar(resultado: ResultadoBusca): Boolean

    suspend fun atualizarStatus(id: String, status: StatusLeitura)

    suspend fun atualizarProgresso(id: String, pagina: Int)

    suspend fun remover(id: String)

    suspend fun livrosSendoLidos(): List<Livro>
}

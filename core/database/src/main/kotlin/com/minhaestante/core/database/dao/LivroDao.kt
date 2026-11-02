package com.minhaestante.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.minhaestante.core.database.entity.LivroEntity
import com.minhaestante.core.model.StatusLeitura
import kotlinx.coroutines.flow.Flow

/**
 * Acesso à tabela `livros`. O Room gera a implementação em tempo de compilação (via KSP).
 *
 * - Funções que retornam [Flow] são "observáveis": emitem de novo sempre que a tabela muda.
 *   É isso que torna a estante reativa e offline-first.
 * - Funções `suspend` executam uma vez, fora da thread principal.
 */
@Dao
interface LivroDao {

    @Query("SELECT * FROM livros ORDER BY adicionadoEm DESC")
    fun observarTodos(): Flow<List<LivroEntity>>

    @Query("SELECT * FROM livros WHERE status = :status ORDER BY adicionadoEm DESC")
    fun observarPorStatus(status: StatusLeitura): Flow<List<LivroEntity>>

    @Query("SELECT * FROM livros WHERE id = :id")
    fun observarPorId(id: String): Flow<LivroEntity?>

    @Query("SELECT id FROM livros")
    fun observarIds(): Flow<List<String>>

    @Query("SELECT * FROM livros WHERE status = 'LENDO'")
    suspend fun buscarLendo(): List<LivroEntity>

    /** Retorna o rowid inserido, ou -1 se já existia um livro com o mesmo id. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserir(livro: LivroEntity): Long

    @Update
    suspend fun atualizar(livro: LivroEntity)

    @Query("DELETE FROM livros WHERE id = :id")
    suspend fun remover(id: String)
}

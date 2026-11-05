package com.minhaestante.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.minhaestante.core.database.BibliotecaDatabase
import com.minhaestante.core.database.entity.LivroEntity
import com.minhaestante.core.model.StatusLeitura
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Teste **instrumentado**: roda num dispositivo/emulador, com o SQLite de verdade.
 *
 * Usamos um banco **em memória**: ele é criado do zero antes de cada teste e some no `close()`,
 * então um teste nunca enxerga os dados de outro e nada é gravado no disco do aparelho.
 * Isso testa as queries SQL e os TypeConverters reais, coisa que um mock do DAO não consegue.
 */
@RunWith(AndroidJUnit4::class)
class LivroDaoTest {

    private lateinit var database: BibliotecaDatabase
    private lateinit var dao: LivroDao

    @Before
    fun criarBanco() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BibliotecaDatabase::class.java).build()
        dao = database.livroDao()
    }

    @After
    fun fecharBanco() {
        database.close()
    }

    @Test
    fun inserirLivroNovoRetornaRowIdESalvaTodosOsCampos() = runTest {
        val livro = livroEntity(id = "/works/OL1W", autores = listOf("Neil Gaiman", "Terry Pratchett"))

        val resultado = dao.inserir(livro)

        assertNotEquals(ID_JA_EXISTENTE, resultado)
        assertEquals(livro, dao.observarPorId("/works/OL1W").first())
    }

    @Test
    fun inserirLivroDuplicadoRetornaMenosUmEMantemOOriginal() = runTest {
        val original = livroEntity(id = "/works/OL1W", titulo = "Original")
        dao.inserir(original)

        val resultado = dao.inserir(original.copy(titulo = "Duplicado"))

        assertEquals(ID_JA_EXISTENTE, resultado)
        assertEquals(listOf(original), dao.observarTodos().first())
    }

    @Test
    fun observarTodosOrdenaDoMaisRecenteParaOMaisAntigo() = runTest {
        dao.inserir(livroEntity(id = "antigo", adicionadoEm = 1_000L))
        dao.inserir(livroEntity(id = "recente", adicionadoEm = 3_000L))
        dao.inserir(livroEntity(id = "meio", adicionadoEm = 2_000L))

        val ids = dao.observarTodos().first().map { it.id }

        assertEquals(listOf("recente", "meio", "antigo"), ids)
    }

    @Test
    fun observarPorStatusRetornaApenasOsLivrosDaqueleStatus() = runTest {
        dao.inserir(livroEntity(id = "a", status = StatusLeitura.LENDO))
        dao.inserir(livroEntity(id = "b", status = StatusLeitura.QUERO_LER))
        dao.inserir(livroEntity(id = "c", status = StatusLeitura.LENDO))

        val lendo = dao.observarPorStatus(StatusLeitura.LENDO).first()

        assertEquals(setOf("a", "c"), lendo.map { it.id }.toSet())
        assertTrue(lendo.all { it.status == StatusLeitura.LENDO })
    }

    @Test
    fun buscarLendoRetornaApenasLivrosComStatusLendo() = runTest {
        dao.inserir(livroEntity(id = "a", status = StatusLeitura.LENDO))
        dao.inserir(livroEntity(id = "b", status = StatusLeitura.LIDO))

        val lendo = dao.buscarLendo()

        assertEquals(listOf("a"), lendo.map { it.id })
    }

    @Test
    fun atualizarGravaAsNovasInformacoesDoLivro() = runTest {
        val livro = livroEntity(id = "/works/OL1W", status = StatusLeitura.QUERO_LER, paginaAtual = 0)
        dao.inserir(livro)

        val atualizado = livro.copy(status = StatusLeitura.LENDO, paginaAtual = 42)
        dao.atualizar(atualizado)

        assertEquals(atualizado, dao.observarPorId("/works/OL1W").first())
    }

    @Test
    fun removerApagaOLivroEObservarPorIdPassaAEmitirNulo() = runTest {
        dao.inserir(livroEntity(id = "/works/OL1W"))

        dao.remover("/works/OL1W")

        assertNull(dao.observarPorId("/works/OL1W").first())
        assertTrue(dao.observarTodos().first().isEmpty())
    }

    @Test
    fun observarIdsEmiteDeNovoQuandoATabelaMuda() = runTest {
        // Turbine: coleta o Flow e deixa conferir cada emissão em ordem.
        // Isso prova que o Flow do Room é reativo: cada escrita na tabela gera uma nova lista.
        dao.observarIds().test {
            assertEquals(emptyList<String>(), awaitItem())

            dao.inserir(livroEntity(id = "a"))
            assertEquals(listOf("a"), awaitItem())

            dao.inserir(livroEntity(id = "b"))
            assertEquals(setOf("a", "b"), awaitItem().toSet())

            dao.remover("a")
            assertEquals(listOf("b"), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun livroEntity(
        id: String,
        titulo: String = "Livro $id",
        autores: List<String> = listOf("Autor"),
        status: StatusLeitura = StatusLeitura.QUERO_LER,
        paginaAtual: Int = 0,
        adicionadoEm: Long = 0L,
    ) = LivroEntity(
        id = id,
        titulo = titulo,
        autores = autores,
        anoPublicacao = 2000,
        capaId = null,
        totalPaginas = 300,
        status = status,
        paginaAtual = paginaAtual,
        adicionadoEm = adicionadoEm,
    )

    private companion object {
        /** Valor que o Room devolve num `@Insert(IGNORE)` quando a linha já existia. */
        const val ID_JA_EXISTENTE = -1L
    }
}

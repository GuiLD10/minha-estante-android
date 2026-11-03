package com.minhaestante.feature.estante

import app.cash.turbine.test
import com.minhaestante.core.model.StatusLeitura
import com.minhaestante.core.testing.FakeEstanteRepository
import com.minhaestante.core.testing.MainDispatcherRule
import com.minhaestante.core.testing.livroTeste
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DetalheViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** livroTeste tem 100 páginas no total. */
    private val livro = livroTeste("/works/1", StatusLeitura.QUERO_LER)

    /**
     * Usa o construtor `internal`, que recebe o id direto. O construtor do Hilt (com
     * SavedStateHandle + toRoute) depende de `android.os.Bundle`, indisponível na JVM.
     */
    private fun criarViewModel(
        repository: FakeEstanteRepository,
        livroId: String = livro.id,
    ) = DetalheViewModel(livroId, repository)

    @Test
    fun `quando o repositorio ainda nao emitiu, entao o estado e Loading`() = runTest {
        val viewModel = criarViewModel(FakeEstanteRepository())

        viewModel.uiState.test {
            assertEquals(DetalheUiState.Loading, awaitItem())
        }
    }

    @Test
    fun `quando o livro existe, entao o estado e Success com o livro da rota`() = runTest {
        val outro = livroTeste("/works/2")
        val viewModel = criarViewModel(FakeEstanteRepository(listOf(livro, outro)))

        viewModel.uiState.test {
            assertEquals(DetalheUiState.Success(livro), awaitItem())
        }
    }

    @Test
    fun `quando o livro nao existe, entao o estado e NaoEncontrado`() = runTest {
        val viewModel = criarViewModel(FakeEstanteRepository(listOf(livro)), livroId = "/works/x")

        viewModel.uiState.test {
            assertEquals(DetalheUiState.NaoEncontrado, awaitItem())
        }
    }

    @Test
    fun `quando altera o status para LIDO, entao a pagina vai para o total`() = runTest {
        val viewModel = criarViewModel(FakeEstanteRepository(listOf(livro)))

        viewModel.uiState.test {
            awaitItem() // estado inicial

            viewModel.alterarStatus(StatusLeitura.LIDO)

            val atualizado = (awaitItem() as DetalheUiState.Success).livro
            assertEquals(StatusLeitura.LIDO, atualizado.status)
            assertEquals(100, atualizado.paginaAtual)
        }
    }

    @Test
    fun `quando salva uma pagina em um livro QUERO_LER, entao ele passa para LENDO`() = runTest {
        val viewModel = criarViewModel(FakeEstanteRepository(listOf(livro)))

        viewModel.uiState.test {
            awaitItem() // estado inicial

            viewModel.salvarPagina(42)

            val atualizado = (awaitItem() as DetalheUiState.Success).livro
            assertEquals(42, atualizado.paginaAtual)
            assertEquals(StatusLeitura.LENDO, atualizado.status)
        }
    }

    @Test
    fun `quando salva a ultima pagina, entao o livro passa para LIDO`() = runTest {
        val viewModel = criarViewModel(FakeEstanteRepository(listOf(livro)))

        viewModel.uiState.test {
            awaitItem() // estado inicial

            viewModel.salvarPagina(100)

            assertEquals(StatusLeitura.LIDO, (awaitItem() as DetalheUiState.Success).livro.status)
        }
    }

    @Test
    fun `quando remove, entao emite o evento de voltar`() = runTest {
        val viewModel = criarViewModel(FakeEstanteRepository(listOf(livro)))

        viewModel.eventos.test {
            viewModel.remover()

            assertEquals(DetalheEvento.LivroRemovido, awaitItem())
        }
    }

    @Test
    fun `quando remove, entao o livro sai da estante e o estado vira NaoEncontrado`() = runTest {
        val repository = FakeEstanteRepository(listOf(livro))
        val viewModel = criarViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // estado inicial

            viewModel.remover()

            assertEquals(DetalheUiState.NaoEncontrado, awaitItem())
        }
    }
}

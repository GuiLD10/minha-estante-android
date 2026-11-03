package com.minhaestante.feature.busca

import app.cash.turbine.test
import com.minhaestante.core.model.ResultadoBusca
import com.minhaestante.core.testing.FakeEstanteRepository
import com.minhaestante.core.testing.FakePreferenciasRepository
import com.minhaestante.core.testing.MainDispatcherRule
import com.minhaestante.feature.busca.BuscaViewModel.Companion.DEBOUNCE_MS
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class BuscaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val buscaRepository = FakeBuscaRepository()
    private val estanteRepository = FakeEstanteRepository(livrosIniciais = emptyList())
    private val preferenciasRepository = FakePreferenciasRepository()

    private val livroA = resultadoTeste("/works/A")
    private val livroB = resultadoTeste("/works/B")

    private fun criarViewModel() =
        BuscaViewModel(buscaRepository, estanteRepository, preferenciasRepository)

    /**
     * `uiState` usa `WhileSubscribed`: sem ninguém coletando, o pipeline nem roda.
     * Aqui simulamos a tela coletando em segundo plano durante o teste todo.
     */
    private fun TestScope.observar(viewModel: BuscaViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
    }

    /** Digita o termo e avança o relógio virtual além do debounce. */
    private fun TestScope.digitarEEsperar(viewModel: BuscaViewModel, termo: String) {
        viewModel.alterarTermo(termo)
        advanceUntilIdle()
    }

    @Test
    fun `quando o termo tem menos de 3 caracteres, entao o estado e Ocioso e nada e buscado`() = runTest {
        val viewModel = criarViewModel()
        observar(viewModel)

        digitarEEsperar(viewModel, "ab")

        assertEquals(BuscaUiState.Ocioso, viewModel.uiState.value)
        assertTrue(buscaRepository.termosBuscados.isEmpty())
    }

    @Test
    fun `quando digita, entao so busca depois do tempo de debounce`() = runTest {
        val viewModel = criarViewModel()
        observar(viewModel)

        viewModel.alterarTermo("tolkien")
        advanceTimeBy(DEBOUNCE_MS - 1)
        assertTrue(buscaRepository.termosBuscados.isEmpty())

        advanceUntilIdle()
        assertEquals(listOf("tolkien"), buscaRepository.termosBuscados)
    }

    @Test
    fun `quando digita rapido, entao busca apenas o ultimo termo`() = runTest {
        val viewModel = criarViewModel()
        observar(viewModel)

        listOf("tol", "tolk", "tolki", "tolkien").forEach { termo ->
            viewModel.alterarTermo(termo)
            advanceTimeBy(DEBOUNCE_MS / 2)
        }
        advanceUntilIdle()

        assertEquals(listOf("tolkien"), buscaRepository.termosBuscados)
    }

    @Test
    fun `quando o termo tem espacos nas pontas, entao busca o termo aparado`() = runTest {
        val viewModel = criarViewModel()
        observar(viewModel)

        digitarEEsperar(viewModel, "  tolkien  ")

        assertEquals(listOf("tolkien"), buscaRepository.termosBuscados)
    }

    @Test
    fun `quando a busca esta em andamento, entao o estado e Carregando`() = runTest {
        val respostaPendente = CompletableDeferred<List<ResultadoBusca>>()
        buscaRepository.resposta = { respostaPendente.await() }
        val viewModel = criarViewModel()

        viewModel.uiState.test {
            assertEquals(BuscaUiState.Ocioso, awaitItem())
            viewModel.alterarTermo("tolkien")

            assertEquals(BuscaUiState.Carregando, awaitItem())

            respostaPendente.complete(listOf(livroA))
            assertEquals(BuscaUiState.Resultados(listOf(livroA)), awaitItem())
        }
    }

    @Test
    fun `quando a api retorna livros, entao o estado e Resultados`() = runTest {
        buscaRepository.resposta = { listOf(livroA, livroB) }
        val viewModel = criarViewModel()
        observar(viewModel)

        digitarEEsperar(viewModel, "tolkien")

        assertEquals(BuscaUiState.Resultados(listOf(livroA, livroB)), viewModel.uiState.value)
    }

    @Test
    fun `quando a api nao retorna nada, entao o estado e SemResultados`() = runTest {
        buscaRepository.resposta = { emptyList() }
        val viewModel = criarViewModel()
        observar(viewModel)

        digitarEEsperar(viewModel, "xyzxyz")

        assertEquals(BuscaUiState.SemResultados, viewModel.uiState.value)
    }

    @Test
    fun `quando nao ha conexao, entao o estado e Erro sem conexao`() = runTest {
        buscaRepository.resposta = { throw IOException("sem rede") }
        val viewModel = criarViewModel()
        observar(viewModel)

        digitarEEsperar(viewModel, "tolkien")

        assertEquals(BuscaUiState.Erro(ErroBusca.SemConexao), viewModel.uiState.value)
    }

    @Test
    fun `quando o servidor responde com erro, entao o estado e Erro com o codigo`() = runTest {
        buscaRepository.resposta = { throw HttpException(Response.error<Any>(503, "".toResponseBody())) }
        val viewModel = criarViewModel()
        observar(viewModel)

        digitarEEsperar(viewModel, "tolkien")

        assertEquals(BuscaUiState.Erro(ErroBusca.Servidor(503)), viewModel.uiState.value)
    }

    @Test
    fun `quando tenta novamente apos um erro, entao busca de novo o mesmo termo`() = runTest {
        buscaRepository.resposta = { throw IOException("sem rede") }
        val viewModel = criarViewModel()
        observar(viewModel)
        digitarEEsperar(viewModel, "tolkien")

        buscaRepository.resposta = { listOf(livroA) }
        viewModel.tentarNovamente()
        advanceUntilIdle()

        assertEquals(listOf("tolkien", "tolkien"), buscaRepository.termosBuscados)
        assertEquals(BuscaUiState.Resultados(listOf(livroA)), viewModel.uiState.value)
    }

    @Test
    fun `quando um resultado ja esta na estante, entao ele e marcado como naEstante`() = runTest {
        estanteRepository.adicionar(livroA)
        buscaRepository.resposta = { listOf(livroA, livroB) }
        val viewModel = criarViewModel()
        observar(viewModel)

        digitarEEsperar(viewModel, "tolkien")

        assertEquals(
            BuscaUiState.Resultados(listOf(livroA.copy(naEstante = true), livroB)),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `quando adiciona um livro, entao emite o evento e o item passa a estar na estante`() = runTest {
        buscaRepository.resposta = { listOf(livroA) }
        val viewModel = criarViewModel()
        observar(viewModel)
        digitarEEsperar(viewModel, "tolkien")

        viewModel.eventos.test {
            viewModel.adicionar(livroA)

            assertEquals(BuscaEvento.AdicionadoNaEstante, awaitItem())
        }
        assertEquals(
            BuscaUiState.Resultados(listOf(livroA.copy(naEstante = true))),
            viewModel.uiState.value,
        )
        // A marcação veio do banco: a rede não foi consultada de novo.
        assertEquals(1, buscaRepository.termosBuscados.size)
    }

    @Test
    fun `quando adiciona um livro que ja estava na estante, entao nao emite evento`() = runTest {
        estanteRepository.adicionar(livroA)
        val viewModel = criarViewModel()

        viewModel.eventos.test {
            viewModel.adicionar(livroA)
            advanceUntilIdle()

            expectNoEvents()
        }
    }

    @Test
    fun `quando busca um termo valido, entao ele e salvo como ultima busca`() = runTest {
        val viewModel = criarViewModel()
        observar(viewModel)

        digitarEEsperar(viewModel, "tolkien")

        assertEquals("tolkien", preferenciasRepository.ultimaBuscaSalva)
    }

    @Test
    fun `quando o termo e curto demais, entao a ultima busca salva nao muda`() = runTest {
        val preferencias = FakePreferenciasRepository(ultimaBuscaInicial = "tolkien")
        val viewModel = BuscaViewModel(buscaRepository, estanteRepository, preferencias)
        observar(viewModel)
        advanceUntilIdle()

        digitarEEsperar(viewModel, "ab")

        assertEquals("tolkien", preferencias.ultimaBuscaSalva)
    }

    @Test
    fun `quando abre a tela, entao restaura e executa a ultima busca salva`() = runTest {
        val preferencias = FakePreferenciasRepository(ultimaBuscaInicial = "machado")
        buscaRepository.resposta = { listOf(livroA) }

        val viewModel = BuscaViewModel(buscaRepository, estanteRepository, preferencias)
        observar(viewModel)
        advanceUntilIdle()

        assertEquals("machado", viewModel.termo.value)
        assertEquals(listOf("machado"), buscaRepository.termosBuscados)
        assertEquals(BuscaUiState.Resultados(listOf(livroA)), viewModel.uiState.value)
    }
}

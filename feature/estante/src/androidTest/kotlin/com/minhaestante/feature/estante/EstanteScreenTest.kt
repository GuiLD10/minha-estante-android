package com.minhaestante.feature.estante

import androidx.annotation.StringRes
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.minhaestante.core.designsystem.theme.MinhaEstanteTheme
import com.minhaestante.core.model.Livro
import com.minhaestante.core.model.StatusLeitura
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Teste de UI da [EstanteScreen].
 *
 * Como a Screen é **stateless** (padrão Route + Screen), basta entregar um `EstanteUiState`
 * pronto e callbacks que anotam o que foi chamado. Não há ViewModel, Hilt nem banco aqui.
 *
 * Os textos vêm do `strings.xml` (via [texto]) para o teste não quebrar se a frase mudar.
 */
@RunWith(AndroidJUnit4::class)
class EstanteScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val livroLendo = livro(id = "/works/OL1W", titulo = "O Hobbit", status = StatusLeitura.LENDO)
    private val livroQueroLer = livro(id = "/works/OL2W", titulo = "Dom Casmurro", status = StatusLeitura.QUERO_LER)

    @Test
    fun carregandoMostraIndicadorDeProgresso() {
        abrirTela(EstanteUiState.Loading)

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun estanteVaziaMostraMensagemEBotaoBuscarLivros() {
        var buscarClicado = false
        abrirTela(EstanteUiState.Vazia(filtro = null), onBuscarClick = { buscarClicado = true })

        composeTestRule.onNodeWithText(texto(R.string.estante_vazia)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.estante_buscar_livros)).performClick()

        assertTrue(buscarClicado)
    }

    @Test
    fun estanteVaziaComFiltroMostraMensagemDoStatus() {
        abrirTela(EstanteUiState.Vazia(filtro = StatusLeitura.LIDO))

        val mensagem = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.estante_vazia_filtro, texto(R.string.estante_status_lido))
        composeTestRule.onNodeWithText(mensagem).assertIsDisplayed()
    }

    @Test
    fun comLivrosMostraTituloDeCadaLivro() {
        abrirTela(EstanteUiState.Success(livros = listOf(livroLendo, livroQueroLer), filtro = null))

        composeTestRule.onNodeWithText("O Hobbit").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dom Casmurro").assertIsDisplayed()
    }

    @Test
    fun cliqueNoLivroRepassaOIdDele() {
        var idClicado: String? = null
        abrirTela(
            EstanteUiState.Success(livros = listOf(livroLendo, livroQueroLer), filtro = null),
            onLivroClick = { idClicado = it },
        )

        composeTestRule.onNodeWithText("Dom Casmurro").performClick()

        assertEquals("/works/OL2W", idClicado)
    }

    @Test
    fun cliqueNoChipDeFiltroRepassaOStatus() {
        var filtroEscolhido: StatusLeitura? = null
        abrirTela(
            EstanteUiState.Success(livros = listOf(livroLendo), filtro = null),
            onFiltroSelecionado = { filtroEscolhido = it },
        )

        composeTestRule.onNodeWithText(texto(R.string.estante_status_lido)).performClick()

        assertEquals(StatusLeitura.LIDO, filtroEscolhido)
    }

    @Test
    fun chipDoFiltroAtualApareceSelecionado() {
        abrirTela(EstanteUiState.Vazia(filtro = StatusLeitura.LIDO))

        composeTestRule.onNodeWithText(texto(R.string.estante_status_lido)).assertIsSelected()
    }

    @Test
    fun cliqueNasAcoesDaBarraAbreConfiguracoesEBusca() {
        var configuracoesClicado = false
        var buscarClicado = false
        abrirTela(
            EstanteUiState.Success(livros = listOf(livroLendo), filtro = null),
            onConfiguracoesClick = { configuracoesClicado = true },
            onBuscarClick = { buscarClicado = true },
        )

        composeTestRule.onNodeWithContentDescription(texto(R.string.estante_abrir_configuracoes)).performClick()
        composeTestRule.onNodeWithContentDescription(texto(R.string.estante_buscar_livros)).performClick()

        assertTrue(configuracoesClicado)
        assertTrue(buscarClicado)
    }

    private fun abrirTela(
        uiState: EstanteUiState,
        onFiltroSelecionado: (StatusLeitura?) -> Unit = {},
        onLivroClick: (String) -> Unit = {},
        onBuscarClick: () -> Unit = {},
        onConfiguracoesClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            MinhaEstanteTheme {
                EstanteScreen(
                    uiState = uiState,
                    onFiltroSelecionado = onFiltroSelecionado,
                    onLivroClick = onLivroClick,
                    onBuscarClick = onBuscarClick,
                    onConfiguracoesClick = onConfiguracoesClick,
                )
            }
        }
    }

    private fun texto(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private fun livro(id: String, titulo: String, status: StatusLeitura) = Livro(
        id = id,
        titulo = titulo,
        autores = listOf("Autor"),
        anoPublicacao = 2000,
        capaId = null,
        totalPaginas = 300,
        status = status,
        paginaAtual = if (status == StatusLeitura.LENDO) 100 else 0,
        adicionadoEm = 0L,
    )
}

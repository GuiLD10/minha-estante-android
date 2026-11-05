package com.minhaestante.feature.busca

import androidx.annotation.StringRes
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.minhaestante.core.designsystem.theme.MinhaEstanteTheme
import com.minhaestante.core.model.ResultadoBusca
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Teste de UI da [BuscaScreen]: cada estado do [BuscaUiState] e as ações do usuário.
 *
 * A Screen não conhece o ViewModel; aqui só verificamos que ela **desenha** o estado
 * recebido e **repassa** as ações pelos callbacks. A lógica (debounce, erros, naEstante)
 * já é coberta pelo `BuscaViewModelTest`.
 */
@RunWith(AndroidJUnit4::class)
class BuscaScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val foraDaEstante = resultado(id = "/works/OL1W", titulo = "O Hobbit", naEstante = false)
    private val naEstante = resultado(id = "/works/OL2W", titulo = "Good Omens", naEstante = true)

    @Test
    fun ociosoMostraOrientacaoParaDigitar() {
        abrirTela(BuscaUiState.Ocioso)

        val mensagem = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.busca_ocioso, TAMANHO_MINIMO_TERMO)
        composeTestRule.onNodeWithText(mensagem).assertIsDisplayed()
    }

    @Test
    fun digitarNoCampoRepassaOTermo() {
        var termoDigitado: String? = null
        abrirTela(BuscaUiState.Ocioso, onTermoChange = { termoDigitado = it })

        composeTestRule.onNode(hasSetTextAction()).performTextInput("tolkien")

        assertEquals("tolkien", termoDigitado)
    }

    @Test
    fun carregandoMostraIndicadorDeProgresso() {
        abrirTela(BuscaUiState.Carregando, termo = "tolkien")

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun semResultadosMostraMensagem() {
        abrirTela(BuscaUiState.SemResultados, termo = "xyzxyz")

        composeTestRule.onNodeWithText(texto(R.string.busca_sem_resultados)).assertIsDisplayed()
    }

    @Test
    fun erroSemConexaoMostraMensagemETentarNovamente() {
        var tentouNovamente = false
        abrirTela(
            BuscaUiState.Erro(ErroBusca.SemConexao),
            termo = "tolkien",
            onTentarNovamente = { tentouNovamente = true },
        )

        composeTestRule.onNodeWithText(texto(R.string.busca_erro_sem_conexao)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.busca_tentar_novamente)).performClick()

        assertTrue(tentouNovamente)
    }

    @Test
    fun erroNoServidorMostraOCodigo() {
        abrirTela(BuscaUiState.Erro(ErroBusca.Servidor(codigo = CODIGO_ERRO)), termo = "tolkien")

        val mensagem = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.busca_erro_servidor, CODIGO_ERRO)
        composeTestRule.onNodeWithText(mensagem).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.busca_tentar_novamente)).assertIsDisplayed()
    }

    @Test
    fun resultadosMostramOTituloDeCadaLivro() {
        abrirTela(BuscaUiState.Resultados(listOf(foraDaEstante, naEstante)), termo = "livro")

        composeTestRule.onNodeWithText("O Hobbit").assertIsDisplayed()
        composeTestRule.onNodeWithText("Good Omens").assertIsDisplayed()
    }

    @Test
    fun cliqueEmAdicionarRepassaOResultado() {
        var adicionado: ResultadoBusca? = null
        abrirTela(
            BuscaUiState.Resultados(listOf(foraDaEstante, naEstante)),
            termo = "livro",
            onAdicionar = { adicionado = it },
        )

        // Só o livro fora da estante tem o botão "Adicionar"; o outro mostra "Na estante".
        composeTestRule.onNodeWithText(texto(R.string.busca_adicionar)).performClick()

        assertEquals(foraDaEstante, adicionado)
    }

    @Test
    fun livroJaNaEstanteMostraBotaoDesabilitado() {
        abrirTela(BuscaUiState.Resultados(listOf(naEstante)), termo = "good omens")

        composeTestRule.onNodeWithText(texto(R.string.busca_na_estante)).assertIsNotEnabled()
    }

    @Test
    fun cliqueEmVoltarChamaOCallback() {
        var voltou = false
        abrirTela(BuscaUiState.Ocioso, onVoltar = { voltou = true })

        composeTestRule.onNodeWithContentDescription(texto(R.string.busca_voltar)).performClick()

        assertTrue(voltou)
    }

    private fun abrirTela(
        uiState: BuscaUiState,
        termo: String = "",
        onTermoChange: (String) -> Unit = {},
        onAdicionar: (ResultadoBusca) -> Unit = {},
        onTentarNovamente: () -> Unit = {},
        onVoltar: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            MinhaEstanteTheme {
                BuscaScreen(
                    termo = termo,
                    uiState = uiState,
                    onTermoChange = onTermoChange,
                    onAdicionar = onAdicionar,
                    onTentarNovamente = onTentarNovamente,
                    onVoltar = onVoltar,
                )
            }
        }
    }

    private fun texto(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private fun resultado(id: String, titulo: String, naEstante: Boolean) = ResultadoBusca(
        id = id,
        titulo = titulo,
        autores = listOf("Autor"),
        anoPublicacao = 2000,
        capaId = null,
        totalPaginas = 300,
        naEstante = naEstante,
    )

    private companion object {
        const val CODIGO_ERRO = 503
    }
}

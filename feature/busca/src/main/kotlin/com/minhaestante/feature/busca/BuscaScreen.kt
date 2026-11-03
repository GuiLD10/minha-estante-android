package com.minhaestante.feature.busca

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minhaestante.core.designsystem.theme.MinhaEstanteTheme
import com.minhaestante.core.model.ResultadoBusca

/**
 * **Route**: obtém o ViewModel, coleta estado e eventos e repassa as ações para a [BuscaScreen].
 */
@Composable
fun BuscaRoute(
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BuscaViewModel = hiltViewModel(),
) {
    val termo by viewModel.termo.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // stringResource só pode ser chamado na composição, então resolvemos o texto antes da coroutine.
    val mensagemAdicionado = stringResource(R.string.busca_adicionado)

    // LaunchedEffect roda uma coroutine ligada à composição: começa quando a tela aparece e é
    // cancelada quando ela sai. Cada evento do Channel é consumido aqui uma única vez.
    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                BuscaEvento.AdicionadoNaEstante -> snackbarHostState.showSnackbar(mensagemAdicionado)
            }
        }
    }

    BuscaScreen(
        termo = termo,
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onTermoChange = viewModel::alterarTermo,
        onAdicionar = viewModel::adicionar,
        onTentarNovamente = viewModel::tentarNovamente,
        onVoltar = onVoltar,
        modifier = modifier,
    )
}

/** **Screen**: stateless. Recebe estado e callbacks, por isso funciona em @Preview e testes de UI. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscaScreen(
    termo: String,
    uiState: BuscaUiState,
    onTermoChange: (String) -> Unit,
    onAdicionar: (ResultadoBusca) -> Unit,
    onTentarNovamente: () -> Unit,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.busca_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            painter = painterResource(R.drawable.ic_voltar),
                            contentDescription = stringResource(R.string.busca_voltar),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            CampoBusca(
                termo = termo,
                onTermoChange = onTermoChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            when (uiState) {
                BuscaUiState.Ocioso -> Mensagem(
                    texto = stringResource(R.string.busca_ocioso, TAMANHO_MINIMO_TERMO),
                )
                BuscaUiState.Carregando -> Carregando()
                is BuscaUiState.Resultados -> ListaResultados(
                    resultados = uiState.itens,
                    onAdicionar = onAdicionar,
                )
                BuscaUiState.SemResultados -> Mensagem(
                    texto = stringResource(R.string.busca_sem_resultados),
                )
                is BuscaUiState.Erro -> ErroComTentarNovamente(
                    erro = uiState.erro,
                    onTentarNovamente = onTentarNovamente,
                )
            }
        }
    }
}

@Composable
private fun CampoBusca(
    termo: String,
    onTermoChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val teclado = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = termo,
        onValueChange = onTermoChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.busca_campo_rotulo)) },
        singleLine = true,
        trailingIcon = {
            if (termo.isNotEmpty()) {
                IconButton(onClick = { onTermoChange("") }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_limpar),
                        contentDescription = stringResource(R.string.busca_limpar),
                    )
                }
            }
        },
        // A busca já acontece sozinha (debounce); o botão "buscar" do teclado só o fecha.
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { teclado?.hide() }),
    )
}

@Composable
private fun ListaResultados(
    resultados: List<ResultadoBusca>,
    onAdicionar: (ResultadoBusca) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = resultados, key = { it.id }) { resultado ->
            ResultadoItem(resultado = resultado, onAdicionar = { onAdicionar(resultado) })
        }
    }
}

@Composable
private fun ErroComTentarNovamente(
    erro: ErroBusca,
    onTentarNovamente: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val texto = when (erro) {
        ErroBusca.SemConexao -> stringResource(R.string.busca_erro_sem_conexao)
        is ErroBusca.Servidor -> stringResource(R.string.busca_erro_servidor, erro.codigo)
    }
    Mensagem(texto = texto, modifier = modifier) {
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onTentarNovamente) {
            Text(stringResource(R.string.busca_tentar_novamente))
        }
    }
}

/** Texto centralizado, com espaço opcional para uma ação abaixo dele. */
@Composable
private fun Mensagem(
    texto: String,
    modifier: Modifier = Modifier,
    acao: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        acao()
    }
}

@Composable
private fun Carregando(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

// ---------- Previews: um por estado relevante ----------

@Composable
private fun BuscaScreenPreview(uiState: BuscaUiState, termo: String = "tolkien") {
    MinhaEstanteTheme {
        BuscaScreen(
            termo = termo,
            uiState = uiState,
            onTermoChange = {},
            onAdicionar = {},
            onTentarNovamente = {},
            onVoltar = {},
        )
    }
}

@Preview(showBackground = true, name = "Ocioso")
@Composable
private fun BuscaOciosoPreview() {
    BuscaScreenPreview(BuscaUiState.Ocioso, termo = "")
}

@Preview(showBackground = true, name = "Carregando")
@Composable
private fun BuscaCarregandoPreview() {
    BuscaScreenPreview(BuscaUiState.Carregando)
}

@Preview(showBackground = true, name = "Resultados")
@Composable
private fun BuscaResultadosPreview() {
    BuscaScreenPreview(BuscaUiState.Resultados(resultadosPreview))
}

@Preview(showBackground = true, name = "Sem resultados")
@Composable
private fun BuscaSemResultadosPreview() {
    BuscaScreenPreview(BuscaUiState.SemResultados, termo = "xyzxyz")
}

@Preview(showBackground = true, name = "Erro sem conexão")
@Composable
private fun BuscaErroSemConexaoPreview() {
    BuscaScreenPreview(BuscaUiState.Erro(ErroBusca.SemConexao))
}

@Preview(showBackground = true, name = "Erro no servidor")
@Composable
private fun BuscaErroServidorPreview() {
    BuscaScreenPreview(BuscaUiState.Erro(ErroBusca.Servidor(codigo = 503)))
}

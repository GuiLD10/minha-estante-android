package com.minhaestante.feature.estante

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minhaestante.core.designsystem.theme.MinhaEstanteTheme
import com.minhaestante.core.model.StatusLeitura

/**
 * **Route**: conecta a tela ao ViewModel. Obtém o ViewModel, coleta o estado
 * e repassa ações. Não desenha nada sozinha.
 *
 * `viewModelFactory` é temporário (etapa 3); na etapa 4 vira `hiltViewModel()`.
 */
@Composable
fun EstanteRoute(
    viewModelFactory: ViewModelProvider.Factory,
    onLivroClick: (String) -> Unit,
    onBuscarClick: () -> Unit,
    onConfiguracoesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // viewModel() devolve sempre a MESMA instância enquanto a tela existir,
    // inclusive depois de uma rotação. A factory só é usada na primeira vez.
    val viewModel: EstanteViewModel = viewModel(factory = viewModelFactory)

    // Coleta o StateFlow apenas enquanto a tela está visível (pausa em segundo plano).
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EstanteScreen(
        uiState = uiState,
        onFiltroSelecionado = viewModel::selecionarFiltro,
        onLivroClick = onLivroClick,
        onBuscarClick = onBuscarClick,
        onConfiguracoesClick = onConfiguracoesClick,
        modifier = modifier,
    )
}

/**
 * **Screen**: stateless. Recebe o estado pronto e callbacks; por isso pode ser
 * usada em @Preview e em testes de UI sem ViewModel nem banco.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstanteScreen(
    uiState: EstanteUiState,
    onFiltroSelecionado: (StatusLeitura?) -> Unit,
    onLivroClick: (String) -> Unit,
    onBuscarClick: () -> Unit,
    onConfiguracoesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.estante_titulo)) },
                actions = {
                    IconButton(onClick = onConfiguracoesClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_configuracoes),
                            contentDescription = stringResource(R.string.estante_abrir_configuracoes),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onBuscarClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_adicionar),
                    contentDescription = stringResource(R.string.estante_buscar_livros),
                )
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            val filtroAtual = when (uiState) {
                EstanteUiState.Loading -> null
                is EstanteUiState.Success -> uiState.filtro
                is EstanteUiState.Vazia -> uiState.filtro
            }
            FiltrosStatus(
                filtroSelecionado = filtroAtual,
                onFiltroSelecionado = onFiltroSelecionado,
            )
            when (uiState) {
                EstanteUiState.Loading -> Carregando()
                is EstanteUiState.Success -> ListaLivros(uiState = uiState, onLivroClick = onLivroClick)
                is EstanteUiState.Vazia -> EstanteVazia(filtro = uiState.filtro, onBuscarClick = onBuscarClick)
            }
        }
    }
}

/** Chips "Todos / Quero ler / Lendo / Lido". */
@Composable
private fun FiltrosStatus(
    filtroSelecionado: StatusLeitura?,
    onFiltroSelecionado: (StatusLeitura?) -> Unit,
    modifier: Modifier = Modifier,
) {
    // `null` representa o chip "Todos"; os demais vêm direto do enum.
    val opcoes: List<StatusLeitura?> = listOf(null) + StatusLeitura.entries
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        opcoes.forEach { opcao ->
            FilterChip(
                selected = opcao == filtroSelecionado,
                onClick = { onFiltroSelecionado(opcao) },
                label = { Text(stringResource(opcao?.rotulo ?: R.string.estante_filtro_todos)) },
            )
        }
    }
}

@Composable
private fun ListaLivros(
    uiState: EstanteUiState.Success,
    onLivroClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        // Espaço extra embaixo para o último item não ficar escondido atrás do FAB.
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // `key` ajuda o Compose a saber qual item é qual quando a lista muda.
        items(items = uiState.livros, key = { it.id }) { livro ->
            LivroItem(livro = livro, onClick = { onLivroClick(livro.id) })
        }
    }
}

@Composable
private fun EstanteVazia(
    filtro: StatusLeitura?,
    onBuscarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mensagem = if (filtro == null) {
        stringResource(R.string.estante_vazia)
    } else {
        stringResource(R.string.estante_vazia_filtro, stringResource(filtro.rotulo))
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBuscarClick) {
            Text(stringResource(R.string.estante_buscar_livros))
        }
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
private fun EstanteScreenPreview(uiState: EstanteUiState) {
    MinhaEstanteTheme {
        EstanteScreen(
            uiState = uiState,
            onFiltroSelecionado = {},
            onLivroClick = {},
            onBuscarClick = {},
            onConfiguracoesClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Carregando")
@Composable
private fun EstanteLoadingPreview() {
    EstanteScreenPreview(EstanteUiState.Loading)
}

@Preview(showBackground = true, name = "Com livros")
@Composable
private fun EstanteSuccessPreview() {
    EstanteScreenPreview(EstanteUiState.Success(livros = livrosPreview, filtro = null))
}

@Preview(showBackground = true, name = "Vazia")
@Composable
private fun EstanteVaziaPreview() {
    EstanteScreenPreview(EstanteUiState.Vazia(filtro = null))
}

@Preview(showBackground = true, name = "Vazia com filtro")
@Composable
private fun EstanteVaziaComFiltroPreview() {
    EstanteScreenPreview(EstanteUiState.Vazia(filtro = StatusLeitura.LIDO))
}

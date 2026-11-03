package com.minhaestante.feature.estante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minhaestante.core.designsystem.R as DesignSystemR
import com.minhaestante.core.designsystem.theme.MinhaEstanteTheme
import com.minhaestante.core.model.Livro
import com.minhaestante.core.model.StatusLeitura
import com.minhaestante.core.model.autoresFormatados
import com.minhaestante.core.model.progressoPercentual

/** Limite de dígitos do campo de página: evita estourar o `Int` e não restringe livros reais. */
private const val MAXIMO_DIGITOS_PAGINA = 5

/**
 * **Route**: obtém o ViewModel (o Hilt já entrega o `SavedStateHandle` com o `livroId` da rota),
 * coleta estado e eventos e repassa as ações.
 */
@Composable
fun DetalheRoute(
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetalheViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // rememberUpdatedState: o LaunchedEffect abaixo roda uma vez só, mas sempre chama
    // a versão mais recente do callback (se ele mudar numa recomposição).
    val voltar by rememberUpdatedState(onVoltar)
    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                DetalheEvento.LivroRemovido -> voltar()
            }
        }
    }

    DetalheScreen(
        uiState = uiState,
        onStatusSelecionado = viewModel::alterarStatus,
        onSalvarPagina = viewModel::salvarPagina,
        onRemover = viewModel::remover,
        onVoltar = onVoltar,
        modifier = modifier,
    )
}

/** **Screen**: stateless em relação aos dados; só guarda estado puramente visual (diálogo, campo). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalheScreen(
    uiState: DetalheUiState,
    onStatusSelecionado: (StatusLeitura) -> Unit,
    onSalvarPagina: (Int) -> Unit,
    onRemover: () -> Unit,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            painter = painterResource(DesignSystemR.drawable.ic_voltar),
                            contentDescription = stringResource(R.string.detalhe_voltar),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val conteudoModifier = Modifier.padding(innerPadding)
        when (uiState) {
            DetalheUiState.Loading -> Carregando(conteudoModifier)
            DetalheUiState.NaoEncontrado -> NaoEncontrado(conteudoModifier)
            is DetalheUiState.Success -> DetalheLivro(
                livro = uiState.livro,
                onStatusSelecionado = onStatusSelecionado,
                onSalvarPagina = onSalvarPagina,
                onRemover = onRemover,
                modifier = conteudoModifier,
            )
        }
    }
}

@Composable
private fun DetalheLivro(
    livro: Livro,
    onStatusSelecionado: (StatusLeitura) -> Unit,
    onSalvarPagina: (Int) -> Unit,
    onRemover: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // rememberSaveable: o diálogo continua aberto se a tela girar.
    var mostrarConfirmacao by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Cabecalho(livro = livro)
        HorizontalDivider()
        SecaoStatus(statusAtual = livro.status, onStatusSelecionado = onStatusSelecionado)
        SecaoProgresso(livro = livro, onSalvarPagina = onSalvarPagina)
        HorizontalDivider()
        OutlinedButton(
            onClick = { mostrarConfirmacao = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            Text(stringResource(R.string.detalhe_remover))
        }
    }

    if (mostrarConfirmacao) {
        ConfirmarRemocao(
            titulo = livro.titulo,
            onConfirmar = {
                mostrarConfirmacao = false
                onRemover()
            },
            onCancelar = { mostrarConfirmacao = false },
        )
    }
}

@Composable
private fun Cabecalho(livro: Livro, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = livro.titulo, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = livro.autoresFormatados(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = livro.anoPublicacao
                ?.let { stringResource(R.string.detalhe_ano_publicacao, it) }
                ?: stringResource(R.string.detalhe_ano_desconhecido),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = livro.totalPaginas
                ?.let { stringResource(R.string.detalhe_total_paginas, it) }
                ?: stringResource(R.string.detalhe_total_paginas_desconhecido),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/** Segmented button: escolha única entre os três status. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecaoStatus(
    statusAtual: StatusLeitura,
    onStatusSelecionado: (StatusLeitura) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.detalhe_status), style = MaterialTheme.typography.titleSmall)
        val opcoes = StatusLeitura.entries
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            opcoes.forEachIndexed { indice, status ->
                SegmentedButton(
                    selected = status == statusAtual,
                    onClick = { onStatusSelecionado(status) },
                    shape = SegmentedButtonDefaults.itemShape(index = indice, count = opcoes.size),
                ) {
                    Text(stringResource(status.rotulo))
                }
            }
        }
    }
}

@Composable
private fun SecaoProgresso(
    livro: Livro,
    onSalvarPagina: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.detalhe_progresso), style = MaterialTheme.typography.titleSmall)

        val total = livro.totalPaginas
        val percentual = livro.progressoPercentual()
        // Sem total conhecido não há percentual: mostramos só a página atual, sem barra.
        if (total != null && percentual != null) {
            Text(stringResource(R.string.detalhe_pagina_de_total, livro.paginaAtual, total))
            BarraProgresso(percentual = percentual)
        } else {
            Text(stringResource(R.string.detalhe_pagina_atual, livro.paginaAtual))
        }

        CampoPagina(paginaAtual = livro.paginaAtual, onSalvar = onSalvarPagina)
    }
}

/**
 * Campo numérico + "Salvar". O texto digitado é estado **da UI** (rascunho): só vira
 * dado de verdade quando o usuário salva.
 *
 * `rememberSaveable(paginaAtual)`: a chave faz o rascunho ser recriado quando a página
 * salva muda (ex.: ao marcar como LIDO, a página vai para o total e o campo acompanha).
 */
@Composable
private fun CampoPagina(
    paginaAtual: Int,
    onSalvar: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var texto by rememberSaveable(paginaAtual) { mutableStateOf(paginaAtual.toString()) }
    val pagina = texto.toIntOrNull()
    val teclado = LocalSoftwareKeyboardController.current
    val salvar = {
        if (pagina != null) {
            onSalvar(pagina)
            teclado?.hide()
        }
    }

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = texto,
            // Aceita só dígitos: não há como digitar número negativo ou texto.
            onValueChange = { novo ->
                if (novo.length <= MAXIMO_DIGITOS_PAGINA && novo.all(Char::isDigit)) texto = novo
            },
            label = { Text(stringResource(R.string.detalhe_campo_pagina)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { salvar() }),
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Button(onClick = salvar, enabled = pagina != null) {
            Text(stringResource(R.string.detalhe_salvar))
        }
    }
}

@Composable
private fun ConfirmarRemocao(
    titulo: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
) {
    AlertDialog(
        // Tocar fora do diálogo ou no "voltar" do sistema equivale a cancelar.
        onDismissRequest = onCancelar,
        title = { Text(stringResource(R.string.detalhe_remover_titulo)) },
        text = { Text(stringResource(R.string.detalhe_remover_mensagem, titulo)) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(
                    text = stringResource(R.string.detalhe_remover_confirmar),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(stringResource(R.string.detalhe_remover_cancelar))
            }
        },
    )
}

@Composable
private fun NaoEncontrado(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.detalhe_nao_encontrado),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
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
private fun DetalheScreenPreview(uiState: DetalheUiState) {
    MinhaEstanteTheme {
        DetalheScreen(
            uiState = uiState,
            onStatusSelecionado = {},
            onSalvarPagina = {},
            onRemover = {},
            onVoltar = {},
        )
    }
}

@Preview(showBackground = true, name = "Carregando")
@Composable
private fun DetalheLoadingPreview() {
    DetalheScreenPreview(DetalheUiState.Loading)
}

@Preview(showBackground = true, name = "Lendo, total conhecido")
@Composable
private fun DetalheLendoPreview() {
    DetalheScreenPreview(DetalheUiState.Success(livrosPreview[0]))
}

@Preview(showBackground = true, name = "Total de páginas desconhecido")
@Composable
private fun DetalheTotalDesconhecidoPreview() {
    DetalheScreenPreview(DetalheUiState.Success(livrosPreview[2]))
}

@Preview(showBackground = true, name = "Não encontrado")
@Composable
private fun DetalheNaoEncontradoPreview() {
    DetalheScreenPreview(DetalheUiState.NaoEncontrado)
}

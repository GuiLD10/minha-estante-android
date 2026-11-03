package com.minhaestante.feature.estante

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.minhaestante.core.designsystem.theme.MinhaEstanteTheme
import com.minhaestante.core.model.Livro
import com.minhaestante.core.model.StatusLeitura
import com.minhaestante.core.model.autoresFormatados
import com.minhaestante.core.model.progressoPercentual

private const val PERCENTUAL_MAXIMO = 100f

/** Card de um livro na lista da estante: título, autores, status e progresso. */
@Composable
internal fun LivroItem(
    livro: Livro,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = livro.titulo,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = livro.autoresFormatados(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(livro.status.rotulo),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            livro.progressoPercentual()?.let { percentual ->
                Spacer(modifier = Modifier.height(8.dp))
                BarraProgresso(percentual = percentual)
            }
        }
    }
}

@Composable
private fun BarraProgresso(percentual: Int, modifier: Modifier = Modifier) {
    val descricao = stringResource(R.string.estante_progresso_descricao, percentual)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = descricao },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LinearProgressIndicator(
            progress = { percentual / PERCENTUAL_MAXIMO },
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.estante_progresso, percentual),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LivroItemPreview() {
    MinhaEstanteTheme {
        LivroItem(livro = livrosPreview[0], onClick = {})
    }
}

/** Dados fictícios usados apenas nos @Preview deste módulo. */
internal val livrosPreview = listOf(
    Livro(
        id = "/works/OL27448W",
        titulo = "The Lord of the Rings",
        autores = listOf("J. R. R. Tolkien"),
        anoPublicacao = 1954,
        capaId = null,
        totalPaginas = 1193,
        status = StatusLeitura.LENDO,
        paginaAtual = 420,
        adicionadoEm = 0L,
    ),
    Livro(
        id = "/works/OL1168083W",
        titulo = "Dom Casmurro",
        autores = listOf("Machado de Assis"),
        anoPublicacao = 1899,
        capaId = null,
        totalPaginas = 256,
        status = StatusLeitura.QUERO_LER,
        paginaAtual = 0,
        adicionadoEm = 0L,
    ),
    Livro(
        id = "/works/OL453936W",
        titulo = "Good Omens",
        autores = listOf("Neil Gaiman", "Terry Pratchett"),
        anoPublicacao = 1990,
        capaId = null,
        totalPaginas = null,
        status = StatusLeitura.LENDO,
        paginaAtual = 75,
        adicionadoEm = 0L,
    ),
)

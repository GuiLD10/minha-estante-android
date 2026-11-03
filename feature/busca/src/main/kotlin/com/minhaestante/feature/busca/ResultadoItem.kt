package com.minhaestante.feature.busca

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.minhaestante.core.designsystem.theme.MinhaEstanteTheme
import com.minhaestante.core.model.ResultadoBusca
import com.minhaestante.core.model.autoresFormatados

/** Card de um resultado: título, autores, ano e o botão "Adicionar" / "Na estante". */
@Composable
internal fun ResultadoItem(
    resultado: ResultadoBusca,
    onAdicionar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resultado.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = resultado.autoresFormatados(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                resultado.anoPublicacao?.let { ano ->
                    Text(
                        text = stringResource(R.string.busca_ano_publicacao, ano),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            BotaoAdicionar(naEstante = resultado.naEstante, onAdicionar = onAdicionar)
        }
    }
}

@Composable
private fun BotaoAdicionar(
    naEstante: Boolean,
    onAdicionar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (naEstante) {
        OutlinedButton(onClick = {}, enabled = false, modifier = modifier) {
            Text(stringResource(R.string.busca_na_estante))
        }
    } else {
        FilledTonalButton(onClick = onAdicionar, modifier = modifier) {
            Text(stringResource(R.string.busca_adicionar))
        }
    }
}

@Preview(showBackground = true, name = "Fora da estante")
@Composable
private fun ResultadoItemPreview() {
    MinhaEstanteTheme {
        ResultadoItem(resultado = resultadosPreview[0], onAdicionar = {})
    }
}

@Preview(showBackground = true, name = "Na estante")
@Composable
private fun ResultadoItemNaEstantePreview() {
    MinhaEstanteTheme {
        ResultadoItem(resultado = resultadosPreview[1], onAdicionar = {})
    }
}

/** Dados fictícios usados apenas nos @Preview deste módulo. */
internal val resultadosPreview = listOf(
    ResultadoBusca(
        id = "/works/OL27448W",
        titulo = "The Lord of the Rings",
        autores = listOf("J. R. R. Tolkien"),
        anoPublicacao = 1954,
        capaId = null,
        totalPaginas = 1193,
        naEstante = false,
    ),
    ResultadoBusca(
        id = "/works/OL453936W",
        titulo = "Good Omens",
        autores = listOf("Neil Gaiman", "Terry Pratchett"),
        anoPublicacao = 1990,
        capaId = null,
        totalPaginas = null,
        naEstante = true,
    ),
    ResultadoBusca(
        id = "/works/OL1W",
        titulo = "Livro sem autor nem ano",
        autores = emptyList(),
        anoPublicacao = null,
        capaId = null,
        totalPaginas = null,
        naEstante = false,
    ),
)

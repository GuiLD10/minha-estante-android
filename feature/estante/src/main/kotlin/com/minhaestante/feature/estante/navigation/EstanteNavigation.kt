package com.minhaestante.feature.estante.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.minhaestante.feature.estante.DetalheRoute
import com.minhaestante.feature.estante.EstanteRoute
import kotlinx.serialization.Serializable

/*
 * Rotas tipadas: cada destino é uma classe @Serializable. Em vez de montar strings como
 * "detalhe/{id}" à mão, navegamos com `navigate(DetalheDestino(livroId))` e o compilador
 * garante que o argumento existe e tem o tipo certo.
 */

/** Tela inicial: a estante. Sem argumentos, por isso um `object`. */
@Serializable
data object EstanteDestino

/** Detalhe de um livro. O `livroId` vira argumento da rota (e chega ao ViewModel via SavedStateHandle). */
@Serializable
data class DetalheDestino(val livroId: String)

fun NavController.navegarParaDetalhe(livroId: String) {
    navigate(DetalheDestino(livroId))
}

/**
 * Registra a tela Estante no grafo. A feature expõe só callbacks: quem decide para onde
 * cada ação leva é o NavHost do `:app` (as features não se conhecem).
 */
fun NavGraphBuilder.telaEstante(
    onLivroClick: (String) -> Unit,
    onBuscarClick: () -> Unit,
    onConfiguracoesClick: () -> Unit,
) {
    composable<EstanteDestino> {
        EstanteRoute(
            onLivroClick = onLivroClick,
            onBuscarClick = onBuscarClick,
            onConfiguracoesClick = onConfiguracoesClick,
        )
    }
}

fun NavGraphBuilder.telaDetalhe(onVoltar: () -> Unit) {
    composable<DetalheDestino> {
        // `dropUnlessResumed` ignora o clique se a tela não estiver RESUMED (ex.: já saindo
        // numa animação). Evita que dois toques rápidos em "voltar" desempilhem duas telas.
        DetalheRoute(onVoltar = dropUnlessResumed(block = onVoltar))
    }
}

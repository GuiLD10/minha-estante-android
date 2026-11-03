package com.minhaestante.feature.busca.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.minhaestante.feature.busca.BuscaRoute
import kotlinx.serialization.Serializable

/** Rota tipada da Busca. */
@Serializable
data object BuscaDestino

fun NavController.navegarParaBusca() {
    navigate(BuscaDestino)
}

fun NavGraphBuilder.telaBusca(onVoltar: () -> Unit) {
    composable<BuscaDestino> {
        // Ignora toques em "voltar" enquanto a tela não estiver RESUMED (evita voltar duas vezes).
        BuscaRoute(onVoltar = dropUnlessResumed(block = onVoltar))
    }
}

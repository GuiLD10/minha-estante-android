package com.minhaestante.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.minhaestante.feature.busca.navigation.navegarParaBusca
import com.minhaestante.feature.busca.navigation.telaBusca
import com.minhaestante.feature.estante.navigation.EstanteDestino
import com.minhaestante.feature.estante.navigation.navegarParaDetalhe
import com.minhaestante.feature.estante.navigation.telaDetalhe
import com.minhaestante.feature.estante.navigation.telaEstante

/**
 * Grafo de navegação do app.
 *
 * Fica no `:app` porque é o único módulo que conhece todas as features. Cada feature registra
 * as próprias telas (`telaEstante`, `telaBusca`...) e recebe só callbacks; o `NavController`
 * nunca entra nas telas, o que as mantém testáveis e independentes umas das outras.
 *
 * Cada destino ganha seu próprio `NavBackStackEntry`, que é dono dos ViewModels daquela tela:
 * ao sair da Busca, o `BuscaViewModel` é destruído (antes, na etapa 5, ele vivia junto com a Activity).
 */
@Composable
fun MinhaEstanteNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = EstanteDestino,
        modifier = modifier,
    ) {
        telaEstante(
            onLivroClick = navController::navegarParaDetalhe,
            onBuscarClick = navController::navegarParaBusca,
            onConfiguracoesClick = {}, // Configurações (Activity com XML) → etapa 7
        )
        telaBusca(onVoltar = navController::popBackStack)
        telaDetalhe(onVoltar = navController::popBackStack)
    }
}

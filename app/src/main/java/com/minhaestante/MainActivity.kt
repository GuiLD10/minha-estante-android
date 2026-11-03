package com.minhaestante

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.minhaestante.core.designsystem.theme.MinhaEstanteTheme
import com.minhaestante.feature.busca.BuscaRoute
import com.minhaestante.feature.estante.EstanteRoute
import dagger.hilt.android.AndroidEntryPoint

/**
 * `@AndroidEntryPoint`: marca a Activity como um ponto de entrada do Hilt. Sem isso,
 * `hiltViewModel()` não funciona dentro dos Composables desta Activity.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MinhaEstanteTheme {
                NavegacaoTemporaria()
            }
        }
    }
}

/** Telas acessíveis enquanto o NavHost não existe. */
private enum class Tela { ESTANTE, BUSCA }

/**
 * TEMPORÁRIO (etapa 5): alterna entre Estante e Busca com um simples estado.
 * Será substituído pelo NavHost com rotas tipadas na etapa 6.
 *
 * `rememberSaveable` guarda a tela atual no Bundle da Activity, então ela sobrevive à rotação.
 * Limitação conhecida: sem NavHost, os ViewModels ficam presos ao escopo da Activity
 * (o da Busca não é destruído ao voltar para a Estante).
 */
@Composable
private fun NavegacaoTemporaria() {
    var telaAtual by rememberSaveable { mutableStateOf(Tela.ESTANTE) }

    when (telaAtual) {
        Tela.ESTANTE -> EstanteRoute(
            onLivroClick = {},         // Detalhe → etapa 6
            onBuscarClick = { telaAtual = Tela.BUSCA },
            onConfiguracoesClick = {}, // Configurações → etapa 7
        )

        Tela.BUSCA -> {
            // Intercepta o botão/gesto "voltar" do sistema enquanto a Busca está aberta.
            BackHandler { telaAtual = Tela.ESTANTE }
            BuscaRoute(onVoltar = { telaAtual = Tela.ESTANTE })
        }
    }
}

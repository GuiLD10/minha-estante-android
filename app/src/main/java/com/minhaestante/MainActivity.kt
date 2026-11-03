package com.minhaestante

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.minhaestante.core.designsystem.theme.MinhaEstanteTheme
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
                EstanteRoute(
                    // As ações abaixo serão ligadas nas próximas etapas:
                    onLivroClick = {},        // Detalhe → etapa 6
                    onBuscarClick = {},       // Busca → etapa 5
                    onConfiguracoesClick = {}, // Configurações → etapa 7
                )
            }
        }
    }
}

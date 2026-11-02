package com.minhaestante

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.minhaestante.core.designsystem.theme.MinhaEstanteTheme
import com.minhaestante.feature.estante.EstanteRoute
import com.minhaestante.feature.estante.EstanteViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // DI manual (etapa 3): pega o repository do container da Application
        // e entrega à factory do ViewModel. Na etapa 4 isso some com o Hilt.
        val dataContainer = (application as MinhaEstanteApp).dataContainer
        val estanteViewModelFactory = EstanteViewModel.Factory(dataContainer.estanteRepository)

        setContent {
            MinhaEstanteTheme {
                EstanteRoute(
                    viewModelFactory = estanteViewModelFactory,
                    // As ações abaixo serão ligadas nas próximas etapas:
                    onLivroClick = {},        // Detalhe → etapa 6
                    onBuscarClick = {},       // Busca → etapa 5
                    onConfiguracoesClick = {}, // Configurações → etapa 7
                )
            }
        }
    }
}

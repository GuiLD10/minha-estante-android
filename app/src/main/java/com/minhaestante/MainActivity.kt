package com.minhaestante

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.minhaestante.core.designsystem.theme.MinhaEstanteTheme
import com.minhaestante.navigation.MinhaEstanteNavHost
import dagger.hilt.android.AndroidEntryPoint

/**
 * `@AndroidEntryPoint`: marca a Activity como um ponto de entrada do Hilt. Sem isso,
 * `hiltViewModel()` não funciona dentro dos Composables desta Activity.
 *
 * Única Activity do app em Compose (single-activity): as telas são destinos do NavHost.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MinhaEstanteTheme {
                MinhaEstanteNavHost()
            }
        }
    }
}

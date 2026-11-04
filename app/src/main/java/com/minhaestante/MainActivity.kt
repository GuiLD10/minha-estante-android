package com.minhaestante

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minhaestante.core.designsystem.theme.MinhaEstanteTheme
import com.minhaestante.navigation.MinhaEstanteNavHost
import dagger.hilt.android.AndroidEntryPoint

/**
 * `@AndroidEntryPoint`: marca a Activity como um ponto de entrada do Hilt. Sem isso,
 * `hiltViewModel()` não funciona dentro dos Composables desta Activity.
 *
 * Activity principal em Compose: Estante, Busca e Detalhe são destinos do NavHost.
 * (Configurações é a exceção: uma Activity em XML, aberta por Intent.)
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val tema by viewModel.tema.collectAsStateWithLifecycle()
            val darkTheme = tema.usaTemaEscuro(sistemaEscuro = isSystemInDarkTheme())

            // O `enableEdgeToEdge()` padrão escolhe a cor dos ícones da status bar pelo tema do
            // *sistema*. Com o tema forçado (ex.: Escuro com o sistema claro) os ícones ficariam
            // escuros sobre fundo escuro, então refazemos a escolha sempre que `darkTheme` muda.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(SCRIM_CLARO, SCRIM_ESCURO) { darkTheme },
                )
                onDispose {}
            }

            MinhaEstanteTheme(darkTheme = darkTheme) {
                MinhaEstanteNavHost()
            }
        }
    }

    private companion object {
        // Mesmos valores que o `enableEdgeToEdge()` usa por padrão na barra de navegação de 3 botões.
        val SCRIM_CLARO = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val SCRIM_ESCURO = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}

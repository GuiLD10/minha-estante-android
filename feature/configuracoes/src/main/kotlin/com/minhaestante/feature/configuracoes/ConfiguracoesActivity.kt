package com.minhaestante.feature.configuracoes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.color.DynamicColors
import com.minhaestante.feature.configuracoes.databinding.ActivityConfiguracoesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Tela de Configurações, feita no jeito "clássico": Activity + layout XML + ViewBinding + Fragment.
 *
 * É uma Activity separada da `MainActivity` (Compose) de propósito, para praticar o ciclo de vida
 * de Activity/Fragment. É aberta a partir da Estante com um `Intent` ([criarIntent]).
 *
 * `AppCompatActivity` (e não `ComponentActivity`) porque precisamos do `AppCompatDelegate`,
 * que permite forçar claro/escuro só nesta Activity via `delegate.localNightMode`.
 */
@AndroidEntryPoint
class ConfiguracoesActivity : AppCompatActivity() {

    /** Mesma instância que o Fragment recebe com `activityViewModels()`. */
    private val viewModel: ConfiguracoesViewModel by viewModels()

    /**
     * Na Activity, o binding vive tanto quanto ela, então `lateinit` basta. No Fragment é
     * diferente: a View pode morrer antes do Fragment (ver `ConfiguracoesFragment`).
     */
    private lateinit var binding: ActivityConfiguracoesBinding

    init {
        // Registrado no construtor para não perder nenhum evento, nem o primeiro ON_CREATE.
        if (BuildConfig.DEBUG) lifecycle.addObserver(LogCicloDeVida(NOME_LOG))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Cores dinâmicas (Android 12+), como no tema Compose do resto do app.
        DynamicColors.applyToActivityIfAvailable(this)
        // A partir do targetSdk 35 o conteúdo sempre desenha atrás das barras do sistema;
        // tratamos os "insets" em `aplicarInsets()` para nada ficar escondido.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        aplicarInsets()
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        observarTema()
    }

    /**
     * Empurra a toolbar para baixo da status bar e o conteúdo para longe da barra de navegação.
     * A toolbar recebe o padding (em vez da raiz) para que seu fundo continue por trás da status bar.
     */
    private fun aplicarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val barras = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            binding.toolbar.updatePadding(left = barras.left, top = barras.top, right = barras.right)
            binding.containerFragment.updatePadding(
                left = barras.left,
                right = barras.right,
                bottom = barras.bottom,
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * Aplica o tema escolhido **nesta** Activity. Quando `localNightMode` muda, o AppCompat
     * recria a Activity com a nova configuração (como numa rotação). Como o ViewModel sobrevive
     * à recriação, a nova Activity já recebe o tema atual e não há recriação em loop
     * (`distinctUntilChanged` + o AppCompat ignora quando o modo não mudou).
     *
     * `repeatOnLifecycle(STARTED)`: coleta só enquanto a tela está visível e cancela no `onStop`,
     * recomeçando no próximo `onStart`. Evita trabalho com a tela em segundo plano.
     */
    private fun observarTema() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .filterIsInstance<ConfiguracoesUiState.Pronto>()
                    .map { it.tema.paraModoNoturno() }
                    .distinctUntilChanged()
                    .collect { modo -> delegate.localNightMode = modo }
            }
        }
    }

    companion object {
        private const val NOME_LOG = "ConfiguracoesActivity"

        /** Quem abre a tela não precisa saber o nome da classe nem quais extras ela espera. */
        fun criarIntent(context: Context): Intent = Intent(context, ConfiguracoesActivity::class.java)
    }
}

package com.minhaestante.feature.configuracoes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.minhaestante.core.model.TemaApp
import com.minhaestante.feature.configuracoes.databinding.FragmentConfiguracoesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Controles das Configurações: escolha de tema e lembrete diário.
 *
 * Segue o mesmo fluxo unidirecional das telas Compose: a View **só** desenha o
 * [ConfiguracoesUiState] e repassa as ações do usuário ao ViewModel.
 */
@AndroidEntryPoint
class ConfiguracoesFragment : Fragment() {

    /*
     * Padrão de ViewBinding em Fragment: a **View** do Fragment pode ser destruída enquanto o
     * Fragment continua vivo (ex.: na back stack). Guardar o binding depois de `onDestroyView`
     * vazaria a árvore de Views inteira. Por isso ele é anulável e zerado em `onDestroyView`,
     * e o `!!` em `binding` só é seguro entre `onCreateView` e `onDestroyView`.
     */
    private var _binding: FragmentConfiguracoesBinding? = null
    private val binding get() = _binding!!

    /** `activityViewModels()`: compartilha o ViewModel com a [ConfiguracoesActivity]. */
    private val viewModel: ConfiguracoesViewModel by activityViewModels()

    /**
     * Precisa ser registrado **antes** do Fragment ficar STARTED (aqui, na inicialização do campo).
     * Se a tela girar com o diálogo aberto, o resultado é entregue ao novo Fragment.
     */
    private val pedirPermissaoNotificacao =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedida ->
            if (concedida) {
                viewModel.definirLembreteAtivo(true)
            } else {
                // O estado continua "desligado" (nada mudou no DataStore, logo nada é emitido),
                // então precisamos desfazer manualmente o toque que ligou o switch.
                binding.switchLembrete.isChecked = false
                Snackbar.make(binding.root, R.string.configuracoes_permissao_negada, Snackbar.LENGTH_LONG)
                    .show()
            }
        }

    init {
        if (BuildConfig.DEBUG) lifecycle.addObserver(LogCicloDeVida(NOME_LOG))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentConfiguracoesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // O ciclo de vida da View é diferente do ciclo do Fragment: compare os dois no Logcat.
        if (BuildConfig.DEBUG) viewLifecycleOwner.lifecycle.addObserver(LogCicloDeVida("$NOME_LOG (view)"))

        configurarAcoes()
        observarEstado()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun configurarAcoes() {
        // Também dispara quando o código chama `check()` em `renderizar`. Não há problema: gravar
        // o mesmo tema não muda nada (o DataStore nem escreve se o valor for igual).
        binding.grupoTema.setOnCheckedChangeListener { _, idMarcado ->
            temaDoBotao(idMarcado)?.let(viewModel::definirTema)
        }
        // `setOnClickListener`, e não `setOnCheckedChangeListener`: este último também dispara
        // quando o *código* muda `isChecked`, o que pediria permissão sem o usuário ter tocado.
        binding.switchLembrete.setOnClickListener {
            aoAlternarLembrete(binding.switchLembrete.isChecked)
        }

        // Botão de teste: existe no layout, mas só aparece em builds debug.
        binding.botaoDispararLembrete.isVisible = BuildConfig.DEBUG
        binding.botaoDispararLembrete.setOnClickListener {
            viewModel.dispararLembreteAgora()
            Snackbar.make(binding.root, R.string.configuracoes_lembrete_disparado, Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Coleta com o `viewLifecycleOwner` (e não com o próprio Fragment): a coleta para quando a
     * View é destruída, então nunca tentamos mexer num binding nulo.
     */
    private fun observarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderizar)
            }
        }
    }

    private fun renderizar(estado: ConfiguracoesUiState) {
        when (estado) {
            ConfiguracoesUiState.Carregando -> habilitarControles(false)
            is ConfiguracoesUiState.Pronto -> {
                habilitarControles(true)
                val idBotao = estado.tema.idDoBotao()
                if (binding.grupoTema.checkedRadioButtonId != idBotao) binding.grupoTema.check(idBotao)
                binding.switchLembrete.isChecked = estado.lembreteAtivo
            }
        }
    }

    private fun habilitarControles(habilitados: Boolean) {
        listOf(binding.radioSistema, binding.radioClaro, binding.radioEscuro, binding.switchLembrete)
            .forEach { it.isEnabled = habilitados }
    }

    private fun aoAlternarLembrete(ativo: Boolean) {
        when {
            !ativo -> viewModel.definirLembreteAtivo(false)
            precisaPedirPermissao() -> pedirPermissaoNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS)
            else -> viewModel.definirLembreteAtivo(true)
        }
    }

    /**
     * `POST_NOTIFICATIONS` só existe a partir do Android 13 (API 33). Antes disso, notificações
     * são permitidas por padrão e não há o que pedir.
     */
    private fun precisaPedirPermissao(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED

    @IdRes
    private fun TemaApp.idDoBotao(): Int = when (this) {
        TemaApp.SISTEMA -> R.id.radio_sistema
        TemaApp.CLARO -> R.id.radio_claro
        TemaApp.ESCURO -> R.id.radio_escuro
    }

    private fun temaDoBotao(@IdRes id: Int): TemaApp? = when (id) {
        R.id.radio_sistema -> TemaApp.SISTEMA
        R.id.radio_claro -> TemaApp.CLARO
        R.id.radio_escuro -> TemaApp.ESCURO
        else -> null // -1 quando nada está marcado
    }

    private companion object {
        const val NOME_LOG = "ConfiguracoesFragment"
    }
}

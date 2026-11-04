package com.minhaestante.feature.configuracoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhaestante.core.data.repository.PreferenciasRepository
import com.minhaestante.core.model.TemaApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado da tela de Configurações. */
sealed interface ConfiguracoesUiState {
    /**
     * Enquanto o DataStore não respondeu. Sem este estado, a tela mostraria os valores padrão
     * (tema Sistema, lembrete desligado) por um instante e depois "pularia" para os salvos.
     */
    data object Carregando : ConfiguracoesUiState

    data class Pronto(val tema: TemaApp, val lembreteAtivo: Boolean) : ConfiguracoesUiState
}

/**
 * ViewModel compartilhado entre a [ConfiguracoesActivity] (que aplica o tema) e o
 * [ConfiguracoesFragment] (que mostra os controles). Os dois obtêm a **mesma instância**,
 * porque o Fragment usa `activityViewModels()`, que escopa o ViewModel à Activity.
 */
@HiltViewModel
class ConfiguracoesViewModel @Inject constructor(
    private val preferencias: PreferenciasRepository,
) : ViewModel() {

    /** `combine` junta as duas preferências: emite sempre que qualquer uma delas muda. */
    val uiState: StateFlow<ConfiguracoesUiState> = combine(
        preferencias.tema,
        preferencias.lembreteAtivo,
    ) { tema, lembreteAtivo ->
        ConfiguracoesUiState.Pronto(tema, lembreteAtivo)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TEMPO_PARA_PARAR_MS),
        initialValue = ConfiguracoesUiState.Carregando,
    )

    /*
     * As gravações rodam no `viewModelScope`, e não no `lifecycleScope` da tela, de propósito:
     * trocar o tema **recria a Activity**. Uma coroutine presa à Activity antiga seria cancelada
     * no meio da gravação; o ViewModel sobrevive à recriação, então a gravação termina.
     */

    fun definirTema(tema: TemaApp) {
        viewModelScope.launch { preferencias.definirTema(tema) }
    }

    /** O agendamento do lembrete com WorkManager entra na etapa 8; por ora só salvamos a preferência. */
    fun definirLembreteAtivo(ativo: Boolean) {
        viewModelScope.launch { preferencias.definirLembreteAtivo(ativo) }
    }

    private companion object {
        const val TEMPO_PARA_PARAR_MS = 5_000L
    }
}

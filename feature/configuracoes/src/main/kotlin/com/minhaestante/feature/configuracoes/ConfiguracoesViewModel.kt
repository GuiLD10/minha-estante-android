package com.minhaestante.feature.configuracoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhaestante.core.data.repository.PreferenciasRepository
import com.minhaestante.core.lembrete.AgendadorLembrete
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
    private val agendador: AgendadorLembrete,
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

    /**
     * Salva a preferência **e** agenda/cancela o trabalho no WorkManager. A preferência continua
     * necessária: o Worker a confere antes de notificar (proteção extra caso um agendamento
     * antigo ainda dispare depois de o usuário desligar o lembrete).
     */
    fun definirLembreteAtivo(ativo: Boolean) {
        viewModelScope.launch {
            preferencias.definirLembreteAtivo(ativo)
            if (ativo) agendador.agendar() else agendador.cancelar()
        }
    }

    /** Só para builds debug: roda o lembrete agora, sem esperar as 20h. */
    fun dispararLembreteAgora() {
        agendador.dispararAgora()
    }

    private companion object {
        const val TEMPO_PARA_PARAR_MS = 5_000L
    }
}

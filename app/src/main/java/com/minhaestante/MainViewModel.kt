package com.minhaestante

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhaestante.core.data.repository.PreferenciasRepository
import com.minhaestante.core.model.TemaApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel da [MainActivity]: expõe o tema escolhido para o tema Compose do app inteiro.
 *
 * Como observa o DataStore, ao voltar das Configurações a Activity já recebe o tema novo,
 * sem precisar de nenhum "aviso" entre as duas telas.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    preferencias: PreferenciasRepository,
) : ViewModel() {

    val tema: StateFlow<TemaApp> = preferencias.tema.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TEMPO_PARA_PARAR_MS),
        initialValue = TemaApp.SISTEMA,
    )

    private companion object {
        const val TEMPO_PARA_PARAR_MS = 5_000L
    }
}

/** Decide se o app deve ficar escuro, dado o tema escolhido e o modo atual do sistema. */
fun TemaApp.usaTemaEscuro(sistemaEscuro: Boolean): Boolean = when (this) {
    TemaApp.SISTEMA -> sistemaEscuro
    TemaApp.CLARO -> false
    TemaApp.ESCURO -> true
}

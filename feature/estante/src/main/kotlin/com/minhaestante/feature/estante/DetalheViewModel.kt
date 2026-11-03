package com.minhaestante.feature.estante

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.minhaestante.core.data.repository.EstanteRepository
import com.minhaestante.core.model.Livro
import com.minhaestante.core.model.StatusLeitura
import com.minhaestante.feature.estante.navigation.DetalheDestino
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado da tela de Detalhe. */
sealed interface DetalheUiState {
    data object Loading : DetalheUiState
    data class Success(val livro: Livro) : DetalheUiState

    /** O livro não existe (ex.: acabou de ser removido). */
    data object NaoEncontrado : DetalheUiState
}

/** Eventos únicos da tela de Detalhe. */
sealed interface DetalheEvento {
    /** O livro foi removido: a tela deve voltar para a anterior. */
    data object LivroRemovido : DetalheEvento
}

/**
 * O construtor principal recebe o [livroId] já pronto; é ele que os testes usam.
 */
@HiltViewModel
class DetalheViewModel internal constructor(
    private val livroId: String,
    private val repository: EstanteRepository,
) : ViewModel() {

    /**
     * Construtor usado pelo Hilt (o `@Inject` marca qual construtor ele deve chamar).
     *
     * `SavedStateHandle`: um mapa de chave → valor que sobrevive à rotação **e** à morte do processo.
     * O Navigation coloca nele os argumentos da rota, e o Hilt o injeta no ViewModel automaticamente.
     * `toRoute<DetalheDestino>()` lê esses argumentos de volta como um objeto tipado.
     *
     * Por que dois construtores? O `toRoute()` usa `android.os.Bundle` por dentro, que não existe
     * nos testes unitários (JVM pura). Separando a leitura da rota, o resto do ViewModel é testável
     * sem emulador nem Robolectric.
     */
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        repository: EstanteRepository,
    ) : this(savedStateHandle.toRoute<DetalheDestino>().livroId, repository)

    private val _eventos = Channel<DetalheEvento>(Channel.BUFFERED)
    val eventos: Flow<DetalheEvento> = _eventos.receiveAsFlow()

    /**
     * Observa o livro no Room: quando o status ou a página mudam, o banco emite de novo
     * e a tela se atualiza sozinha. O ViewModel nunca guarda uma cópia "local" do livro.
     */
    val uiState: StateFlow<DetalheUiState> = repository.observarLivro(livroId)
        .map { livro -> livro?.let(DetalheUiState::Success) ?: DetalheUiState.NaoEncontrado }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TEMPO_PARA_PARAR_MS),
            initialValue = DetalheUiState.Loading,
        )

    /** As regras (página zerada em QUERO_LER, página = total em LIDO) ficam no Repository/domínio. */
    fun alterarStatus(status: StatusLeitura) {
        viewModelScope.launch { repository.atualizarStatus(livroId, status) }
    }

    fun salvarPagina(pagina: Int) {
        viewModelScope.launch { repository.atualizarProgresso(livroId, pagina) }
    }

    fun remover() {
        viewModelScope.launch {
            repository.remover(livroId)
            _eventos.send(DetalheEvento.LivroRemovido)
        }
    }

    private companion object {
        const val TEMPO_PARA_PARAR_MS = 5_000L
    }
}

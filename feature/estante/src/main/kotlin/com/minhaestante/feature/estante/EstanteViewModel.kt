package com.minhaestante.feature.estante

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.minhaestante.core.data.repository.EstanteRepository
import com.minhaestante.core.model.Livro
import com.minhaestante.core.model.StatusLeitura
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Estado da tela Estante. A UI só desenha o que estiver aqui. */
sealed interface EstanteUiState {
    data object Loading : EstanteUiState
    data class Success(val livros: List<Livro>, val filtro: StatusLeitura?) : EstanteUiState
    data class Vazia(val filtro: StatusLeitura?) : EstanteUiState
}

class EstanteViewModel(
    private val repository: EstanteRepository,
) : ViewModel() {

    /** Filtro selecionado nos chips. `null` = "Todos". */
    private val filtro = MutableStateFlow<StatusLeitura?>(null)

    /**
     * Toda vez que o filtro muda, `flatMapLatest` cancela a observação anterior do banco
     * e começa a observar a consulta do novo filtro.
     *
     * `stateIn` transforma o Flow em StateFlow (sempre tem um valor atual, começando em Loading).
     * `WhileSubscribed(5s)` mantém a consulta viva por 5s sem observadores: numa rotação de tela
     * a UI some e volta em menos tempo que isso, então o banco não é consultado de novo.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<EstanteUiState> = filtro
        .flatMapLatest { filtroAtual ->
            repository.observarEstante(filtroAtual).map { livros ->
                if (livros.isEmpty()) {
                    EstanteUiState.Vazia(filtroAtual)
                } else {
                    EstanteUiState.Success(livros, filtroAtual)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TEMPO_PARA_PARAR_MS),
            initialValue = EstanteUiState.Loading,
        )

    fun selecionarFiltro(novoFiltro: StatusLeitura?) {
        filtro.value = novoFiltro
    }

    /**
     * Factory **manual** (etapa 3, antes do Hilt).
     *
     * O Android é quem cria ViewModels (para mantê-los vivos na rotação), e por padrão
     * ele só sabe chamar construtores sem parâmetros. A Factory ensina como criar
     * um [EstanteViewModel] que recebe um [EstanteRepository].
     */
    class Factory(
        private val repository: EstanteRepository,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            require(modelClass.isAssignableFrom(EstanteViewModel::class.java)) {
                "Factory não sabe criar ${modelClass.name}"
            }
            return EstanteViewModel(repository) as T
        }
    }

    private companion object {
        const val TEMPO_PARA_PARAR_MS = 5_000L
    }
}

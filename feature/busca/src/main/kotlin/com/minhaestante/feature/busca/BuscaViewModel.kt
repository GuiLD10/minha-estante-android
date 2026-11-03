package com.minhaestante.feature.busca

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhaestante.core.data.repository.BuscaRepository
import com.minhaestante.core.data.repository.EstanteRepository
import com.minhaestante.core.data.repository.PreferenciasRepository
import com.minhaestante.core.model.ResultadoBusca
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/** Termos menores que isso não disparam busca (a tela fica em [BuscaUiState.Ocioso]). */
internal const val TAMANHO_MINIMO_TERMO = 3

/** Estado da tela de Busca. */
sealed interface BuscaUiState {
    /** Termo vazio ou curto demais. */
    data object Ocioso : BuscaUiState
    data object Carregando : BuscaUiState
    data class Resultados(val itens: List<ResultadoBusca>) : BuscaUiState
    data object SemResultados : BuscaUiState
    data class Erro(val erro: ErroBusca) : BuscaUiState
}

/**
 * Tipo do erro, em vez de um texto pronto: o ViewModel não tem acesso a `strings.xml`
 * (e nem deveria, para continuar testável na JVM). A tela traduz cada tipo em texto.
 */
sealed interface ErroBusca {
    data object SemConexao : ErroBusca
    data class Servidor(val codigo: Int) : ErroBusca
}

/** Eventos únicos: acontecem uma vez e não fazem parte do estado (ex.: Snackbar). */
sealed interface BuscaEvento {
    data object AdicionadoNaEstante : BuscaEvento
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class BuscaViewModel @Inject constructor(
    private val buscaRepository: BuscaRepository,
    private val estanteRepository: EstanteRepository,
    private val preferenciasRepository: PreferenciasRepository,
) : ViewModel() {

    /** O que está digitado no campo, atualizado a cada tecla (sem debounce). */
    private val _termo = MutableStateFlow("")
    val termo: StateFlow<String> = _termo.asStateFlow()

    /** Contador incrementado em "Tentar novamente" para forçar a mesma busca de novo. */
    private val tentativas = MutableStateFlow(0)

    /**
     * Channel = fila de eventos: cada evento é entregue **uma única vez** a quem coletar.
     * Diferente de um StateFlow, não é reemitido ao girar a tela (o Snackbar não reaparece).
     */
    private val _eventos = Channel<BuscaEvento>(Channel.BUFFERED)
    val eventos: Flow<BuscaEvento> = _eventos.receiveAsFlow()

    /**
     * Pipeline do termo:
     * - `debounce`: espera o usuário parar de digitar por [DEBOUNCE_MS] antes de seguir;
     *   evita uma requisição por tecla.
     * - `map { trim() }` + `distinctUntilChanged`: "tolkien" e "tolkien " não buscam duas vezes.
     */
    private val termoProcessado: Flow<String> = _termo
        .debounce(DEBOUNCE_MS)
        .map { it.trim() }
        .distinctUntilChanged()

    /**
     * `combine` com [tentativas] faz o mesmo termo passar de novo quando o usuário pede.
     * `flatMapLatest`: se chegar um termo novo enquanto a busca anterior ainda roda,
     * a anterior é **cancelada** (a resposta antiga nunca sobrescreve a nova).
     */
    private val estadoDaBusca: Flow<BuscaUiState> = combine(termoProcessado, tentativas) { termo, _ -> termo }
        .flatMapLatest { termo -> buscar(termo) }

    /**
     * Combina o estado da busca com os ids salvos no Room. Quando o usuário adiciona um livro,
     * só os ids mudam: o item vira "Na estante" **sem** repetir a requisição na rede.
     */
    val uiState: StateFlow<BuscaUiState> = combine(
        estadoDaBusca,
        estanteRepository.observarIdsNaEstante(),
    ) { estado, idsNaEstante -> estado.marcandoNaEstante(idsNaEstante) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TEMPO_PARA_PARAR_MS),
            initialValue = BuscaUiState.Ocioso,
        )

    init {
        viewModelScope.launch { restaurarUltimaBusca() }
    }

    fun alterarTermo(novoTermo: String) {
        _termo.value = novoTermo
    }

    fun tentarNovamente() {
        tentativas.update { it + 1 }
    }

    fun adicionar(resultado: ResultadoBusca) {
        viewModelScope.launch {
            val adicionou = estanteRepository.adicionar(resultado)
            if (adicionou) _eventos.send(BuscaEvento.AdicionadoNaEstante)
        }
    }

    /** Lê o último termo do DataStore. Se o usuário já começou a digitar, não sobrescreve. */
    private suspend fun restaurarUltimaBusca() {
        val ultimaBusca = preferenciasRepository.ultimaBusca.first()
        if (_termo.value.isEmpty()) _termo.value = ultimaBusca
    }

    /**
     * Um Flow por termo: emite Carregando e depois o resultado.
     * `catch` converte só os erros de rede esperados; qualquer outro erro (inclusive
     * `CancellationException`, usada pelo `flatMapLatest` para cancelar) é relançado.
     */
    private fun buscar(termo: String): Flow<BuscaUiState> = flow {
        if (termo.length < TAMANHO_MINIMO_TERMO) {
            emit(BuscaUiState.Ocioso)
            return@flow
        }
        emit(BuscaUiState.Carregando)
        preferenciasRepository.salvarUltimaBusca(termo)
        val resultados = buscaRepository.buscar(termo)
        emit(if (resultados.isEmpty()) BuscaUiState.SemResultados else BuscaUiState.Resultados(resultados))
    }.catch { erro ->
        when (erro) {
            is IOException -> emit(BuscaUiState.Erro(ErroBusca.SemConexao))
            is HttpException -> emit(BuscaUiState.Erro(ErroBusca.Servidor(erro.code())))
            else -> throw erro
        }
    }

    private fun BuscaUiState.marcandoNaEstante(ids: Set<String>): BuscaUiState = when (this) {
        is BuscaUiState.Resultados -> copy(itens = itens.map { it.copy(naEstante = it.id in ids) })
        else -> this
    }

    internal companion object {
        const val DEBOUNCE_MS = 400L
        private const val TEMPO_PARA_PARAR_MS = 5_000L
    }
}

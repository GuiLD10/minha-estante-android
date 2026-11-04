package com.minhaestante.core.testing

import com.minhaestante.core.data.repository.PreferenciasRepository
import com.minhaestante.core.model.TemaApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Preferências em memória para testes de ViewModel. */
class FakePreferenciasRepository(
    ultimaBuscaInicial: String = "",
    temaInicial: TemaApp = TemaApp.SISTEMA,
    lembreteAtivoInicial: Boolean = false,
) : PreferenciasRepository {

    private val _ultimaBusca = MutableStateFlow(ultimaBuscaInicial)
    override val ultimaBusca: Flow<String> = _ultimaBusca

    private val _tema = MutableStateFlow(temaInicial)
    override val tema: Flow<TemaApp> = _tema

    private val _lembreteAtivo = MutableStateFlow(lembreteAtivoInicial)
    override val lembreteAtivo: Flow<Boolean> = _lembreteAtivo

    /** Valores gravados mais recentemente, para o teste conferir. */
    val ultimaBuscaSalva: String get() = _ultimaBusca.value
    val temaSalvo: TemaApp get() = _tema.value
    val lembreteAtivoSalvo: Boolean get() = _lembreteAtivo.value

    override suspend fun salvarUltimaBusca(termo: String) {
        _ultimaBusca.value = termo
    }

    override suspend fun definirTema(tema: TemaApp) {
        _tema.value = tema
    }

    override suspend fun definirLembreteAtivo(ativo: Boolean) {
        _lembreteAtivo.value = ativo
    }
}

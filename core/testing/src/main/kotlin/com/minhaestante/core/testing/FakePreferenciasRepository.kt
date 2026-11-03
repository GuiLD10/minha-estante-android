package com.minhaestante.core.testing

import com.minhaestante.core.data.repository.PreferenciasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Preferências em memória para testes de ViewModel. */
class FakePreferenciasRepository(ultimaBuscaInicial: String = "") : PreferenciasRepository {

    private val _ultimaBusca = MutableStateFlow(ultimaBuscaInicial)
    override val ultimaBusca: Flow<String> = _ultimaBusca

    /** Valor gravado mais recentemente, para o teste conferir. */
    val ultimaBuscaSalva: String get() = _ultimaBusca.value

    override suspend fun salvarUltimaBusca(termo: String) {
        _ultimaBusca.value = termo
    }
}

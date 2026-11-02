package com.minhaestante.feature.estante

import com.minhaestante.core.data.repository.EstanteRepository
import com.minhaestante.core.model.Livro
import com.minhaestante.core.model.ResultadoBusca
import com.minhaestante.core.model.StatusLeitura
import com.minhaestante.core.model.comProgresso
import com.minhaestante.core.model.comStatus
import com.minhaestante.core.model.paraLivro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Repository em memória para testes de ViewModel.
 *
 * Usa um `MutableSharedFlow` sem valor inicial: nada é emitido até o teste chamar
 * [emitir]. Isso permite verificar o estado `Loading` antes de os dados "chegarem".
 */
class FakeEstanteRepository : EstanteRepository {

    private val livros = MutableSharedFlow<List<Livro>>(replay = 1)

    suspend fun emitir(lista: List<Livro>) = livros.emit(lista)

    override fun observarEstante(filtro: StatusLeitura?): Flow<List<Livro>> =
        livros.map { lista -> lista.filter { filtro == null || it.status == filtro } }

    override fun observarLivro(id: String): Flow<Livro?> =
        livros.map { lista -> lista.find { it.id == id } }

    override fun observarIdsNaEstante(): Flow<Set<String>> =
        livros.map { lista -> lista.map { it.id }.toSet() }

    override suspend fun adicionar(resultado: ResultadoBusca): Boolean {
        val atuais = atuais()
        if (atuais.any { it.id == resultado.id }) return false
        livros.emit(listOf(resultado.paraLivro(agora = 0L)) + atuais)
        return true
    }

    override suspend fun atualizarStatus(id: String, status: StatusLeitura) =
        atualizar(id) { it.comStatus(status) }

    override suspend fun atualizarProgresso(id: String, pagina: Int) =
        atualizar(id) { it.comProgresso(pagina) }

    override suspend fun remover(id: String) {
        livros.emit(atuais().filterNot { it.id == id })
    }

    override suspend fun livrosSendoLidos(): List<Livro> =
        atuais().filter { it.status == StatusLeitura.LENDO }

    private fun atuais(): List<Livro> = livros.replayCache.firstOrNull().orEmpty()

    private suspend fun atualizar(id: String, transformacao: (Livro) -> Livro) {
        livros.emit(atuais().map { if (it.id == id) transformacao(it) else it })
    }
}

/** Cria um livro de teste com valores padrão. */
fun livroTeste(
    id: String,
    status: StatusLeitura = StatusLeitura.QUERO_LER,
) = Livro(
    id = id,
    titulo = "Livro $id",
    autores = listOf("Autor"),
    anoPublicacao = 2000,
    capaId = null,
    totalPaginas = 100,
    status = status,
    paginaAtual = 0,
    adicionadoEm = 0L,
)

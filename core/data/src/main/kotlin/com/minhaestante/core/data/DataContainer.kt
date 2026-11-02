package com.minhaestante.core.data

import android.content.Context
import com.minhaestante.core.data.repository.EstanteRepository
import com.minhaestante.core.data.repository.OfflineFirstEstanteRepository
import com.minhaestante.core.database.BibliotecaDatabase

/**
 * Injeção de dependência **manual** (temporária, apenas na etapa 3).
 *
 * Monta o grafo à mão: Database → DAO → Repository. Deve existir uma única
 * instância, guardada na Application. Na etapa 4 esta classe é substituída pelo Hilt.
 */
class DataContainer(context: Context) {

    private val database: BibliotecaDatabase = BibliotecaDatabase.construir(context)

    val estanteRepository: EstanteRepository = OfflineFirstEstanteRepository(database.livroDao())
}

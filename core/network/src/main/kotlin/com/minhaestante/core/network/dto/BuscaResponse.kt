package com.minhaestante.core.network.dto

import kotlinx.serialization.Serializable

/** Corpo da resposta de `search.json`. Só nos interessa a lista `docs`. */
@Serializable
internal data class BuscaResponse(val docs: List<LivroDto>)

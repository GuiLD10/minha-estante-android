package com.minhaestante.core.network

import kotlinx.serialization.json.Json

/**
 * Configuração do kotlinx.serialization para a Open Library.
 *
 * `ignoreUnknownKeys`: campos do JSON que o DTO não declara são ignorados em vez de
 * causar erro. Deixa o app resistente a mudanças na API.
 */
internal val openLibraryJson = Json { ignoreUnknownKeys = true }

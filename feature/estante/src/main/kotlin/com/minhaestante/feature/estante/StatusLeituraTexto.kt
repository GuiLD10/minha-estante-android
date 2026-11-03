package com.minhaestante.feature.estante

import androidx.annotation.StringRes
import com.minhaestante.core.model.StatusLeitura

/** Texto exibido para cada status. Fica na UI porque depende de recursos do Android. */
@get:StringRes
internal val StatusLeitura.rotulo: Int
    get() = when (this) {
        StatusLeitura.QUERO_LER -> R.string.estante_status_quero_ler
        StatusLeitura.LENDO -> R.string.estante_status_lendo
        StatusLeitura.LIDO -> R.string.estante_status_lido
    }

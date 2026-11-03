package com.minhaestante

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * `@HiltAndroidApp`: dispara a geração de código do Hilt e cria o container de
 * dependências raiz (`SingletonComponent`), que vive tanto quanto a Application.
 */
@HiltAndroidApp
class MinhaEstanteApp : Application()

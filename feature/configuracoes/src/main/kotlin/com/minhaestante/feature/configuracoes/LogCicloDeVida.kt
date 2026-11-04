package com.minhaestante.feature.configuracoes

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Observador que só escreve no Logcat cada callback de ciclo de vida. Serve para estudo:
 * filtre o Logcat por `CicloDeVida` e gire a tela ou navegue para ver a ordem dos eventos.
 *
 * `DefaultLifecycleObserver` é a forma recomendada de reagir ao ciclo de vida **de fora**
 * da Activity/Fragment: em vez de sobrescrever `onStart()` etc., registramos um observador
 * com `lifecycle.addObserver(...)`. Muitas bibliotecas (ex.: câmera, player) usam esse padrão.
 */
internal class LogCicloDeVida(private val nome: String) : DefaultLifecycleObserver {

    override fun onCreate(owner: LifecycleOwner) = registrar("onCreate")
    override fun onStart(owner: LifecycleOwner) = registrar("onStart")
    override fun onResume(owner: LifecycleOwner) = registrar("onResume")
    override fun onPause(owner: LifecycleOwner) = registrar("onPause")
    override fun onStop(owner: LifecycleOwner) = registrar("onStop")
    override fun onDestroy(owner: LifecycleOwner) = registrar("onDestroy")

    private fun registrar(evento: String) {
        Log.d(TAG, "$nome → $evento")
    }

    private companion object {
        const val TAG = "CicloDeVida"
    }
}

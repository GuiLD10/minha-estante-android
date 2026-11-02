// Build raiz: apenas declara os plugins usados pelos módulos, com `apply false`.
// Isso coloca todos no mesmo classpath (evita conflitos de versão entre módulos)
// e deixa cada módulo aplicar só o que precisa.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
}

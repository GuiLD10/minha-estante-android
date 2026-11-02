import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Módulo Kotlin/JVM puro: sem nenhuma dependência do Android.
// Por isso os testes rodam direto na JVM, muito rápidos.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation(libs.junit)
}

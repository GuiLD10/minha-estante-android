plugins {
    alias(libs.plugins.android.library)
    // KSP roda o processador de anotações do Room, que gera a implementação do DAO e do Database.
    alias(libs.plugins.ksp)
    // Plugin do Room: configura onde exportar o esquema do banco (útil para Migrations).
    alias(libs.plugins.room)
    // Hilt: este módulo fornece o DatabaseModule (como criar o banco e o DAO).
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.minhaestante.core.database"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

room {
    // Cada versão do banco gera um JSON com o esquema; ele vai para o Git e serve
    // de referência (e de teste) quando escrevermos Migrations.
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(projects.core.model)

    // `api` (e não `implementation`) porque BibliotecaDatabase, que é pública, estende
    // RoomDatabase: quem usa este módulo precisa enxergar essa classe para compilar.
    api(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Usado no TypeConverter para salvar a lista de autores como JSON.
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)

    // Testes instrumentados (src/androidTest): rodam no emulador com um Room em memória.
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.turbine)
}

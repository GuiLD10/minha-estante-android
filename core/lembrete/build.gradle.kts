plugins {
    alias(libs.plugins.android.library)
    // Hilt: o LembreteWorker é um @HiltWorker e o LembreteModule faz o binding do agendador.
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.minhaestante.core.lembrete"
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

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.data)

    // NotificationCompat / NotificationManagerCompat / ContextCompat
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)

    // `api`: o :app precisa enxergar WorkManager/Configuration para montar a HiltWorkerFactory.
    api(libs.androidx.work.runtime.ktx)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // @HiltWorker e o compilador que gera a fábrica do worker com @AssistedInject.
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(projects.core.testing)
}

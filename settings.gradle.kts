pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Permite escrever `projects.core.model` em vez de `project(":core:model")`,
// com autocompletar e verificação em tempo de compilação.
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "MinhaEstante"

include(":app")

include(":feature:estante")
include(":feature:busca")
include(":feature:configuracoes")

include(":core:model")
include(":core:designsystem")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:data")
include(":core:lembrete")

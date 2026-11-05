# Minha Estante

[![CI](https://github.com/GuiLD10/minha-estante-android/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/GuiLD10/minha-estante-android/actions/workflows/ci.yml)

Personal reading shelf for Android: search books on [Open Library](https://openlibrary.org/), add them to your shelf and track what you want to read, are reading and have read.

Built with Kotlin, Jetpack Compose, MVVM, Coroutines/Flow, Room, DataStore, Hilt and WorkManager.

## Running the tests

```bash
./gradlew testDebugUnitTest :core:model:test   # unit tests
./gradlew connectedDebugAndroidTest            # instrumented tests (requires an emulator or device)
./gradlew lintDebug                            # static analysis
```

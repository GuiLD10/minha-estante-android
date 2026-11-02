# CLAUDE.md — Minha Estante

App Android de estante de leitura pessoal. O usuário busca livros na Open Library, adiciona à sua estante e acompanha o que quer ler, está lendo e já leu.

## Contexto e objetivo do projeto

Este é um **projeto de estudo**. O objetivo principal não é o app em si, mas praticar todos os conceitos exigidos para uma vaga de Analista de Desenvolvimento Android Júnior (Kotlin): Kotlin e POO, ciclo de vida de Activity/Fragment, layouts XML, MVVM, Coroutines e Flow, Jetpack Compose, Retrofit, Room, DataStore, Hilt, WorkManager, testes automatizados, Gradle modular, Git e noções de Kotlin Multiplatform.

O projeto também servirá como portfólio na candidatura.

### Como o Claude deve trabalhar neste projeto

- O desenvolvedor está **reaprendendo** Kotlin e Android. Ao implementar algo, explique brevemente os conceitos envolvidos e o porquê das decisões, em português.
- Siga a **ordem das etapas** descrita abaixo. Não implemente etapas futuras antecipadamente sem pedido explícito.
- Prefira implementar **uma etapa (ou parte dela) por vez**, deixando o app compilando e funcionando ao final de cada uma.
- Não adicione bibliotecas, telas ou funcionalidades fora do escopo definido neste arquivo sem perguntar antes.
- Toda lógica nova em ViewModel, Repository, mapper ou regra de domínio deve vir acompanhada de testes unitários.
- Ao concluir uma etapa, atualize a seção **Status** no final deste arquivo.

---

## Escopo

### Dentro do escopo

**Telas**

1. **Busca** (Compose) — busca livros na Open Library com debounce e permite adicioná-los à estante.
2. **Minha Estante** (Compose) — lista os livros salvos, com filtro por status. Funciona offline. É a tela inicial.
3. **Detalhe** (Compose) — mostra os dados do livro, permite mudar o status, atualizar a página atual e remover da estante.
4. **Configurações** (XML) — Activity com layout XML, ViewBinding e um Fragment. Permite escolher o tema e ativar/desativar o lembrete diário.

**Funcionalidade em segundo plano**

- **Lembrete diário de leitura** com WorkManager: se houver livros com status "Lendo", envia uma notificação por dia sugerindo continuar a leitura.

**Engenharia**

- Arquitetura MVVM em camadas, com fluxo unidirecional de dados.
- Projeto modularizado com version catalog.
- Injeção de dependência com Hilt.
- Testes unitários e instrumentados.
- CI com GitHub Actions rodando os testes a cada Pull Request.
- README em inglês.

**Bônus (opcional, após o restante)**

- Módulo `:shared` com Kotlin Multiplatform (Ktor + kotlinx.serialization) e uma versão desktop da busca com Compose Multiplatform.
- Capas dos livros com Coil.

### Fora do escopo

- Login, contas de usuário ou backend próprio.
- Sincronização entre dispositivos.
- Paginação ou cache offline dos **resultados de busca** (apenas a estante é offline).
- Escolha de horário do lembrete pelo usuário (horário fixo: 20h).
- Avaliações, resenhas, metas de leitura, estatísticas.
- Suporte a tablets/layouts adaptativos.

---

## Stack técnica

| Área | Tecnologia |
|---|---|
| Linguagem | Kotlin (versão estável mais recente) |
| UI principal | Jetpack Compose + Material 3 |
| UI legada | XML + ViewBinding (somente Configurações) |
| Navegação | Navigation Compose com rotas tipadas (`@Serializable`) |
| Arquitetura | MVVM, Repository, UiState com sealed interface |
| Assíncrono | Kotlin Coroutines, Flow, StateFlow, Channel |
| Rede | Retrofit + OkHttp (logging interceptor) |
| Serialização | kotlinx.serialization |
| Banco local | Room (com KSP) |
| Preferências | DataStore (Preferences) |
| DI | Hilt (com KSP), incluindo `hilt-work` e `hilt-navigation-compose` |
| Background | WorkManager (`CoroutineWorker`) |
| Imagens (bônus) | Coil |
| Testes unitários | JUnit 4, MockK, kotlinx-coroutines-test, Turbine |
| Testes instrumentados | AndroidX Test, Room in-memory, Compose UI Test |
| Build | Gradle Kotlin DSL, version catalog (`gradle/libs.versions.toml`) |
| CI | GitHub Actions |

**Configuração Android**

- `minSdk = 26`
- `compileSdk` e `targetSdk` = versão estável mais recente
- Java/JVM target 17
- Pacote base: `com.minhaestante`

Use sempre as versões estáveis mais recentes das bibliotecas, declaradas no version catalog. Nunca declare versões diretamente nos `build.gradle.kts` dos módulos.

---

## Arquitetura

### Camadas

```
UI (Composables / Fragment)    → só renderiza estado e repassa ações
        ↓ ações        ↑ StateFlow<UiState> + eventos (Channel)
ViewModel                      → estado da tela, chama Repositories
        ↓              ↑ Flow / suspend
Repository (interface)         → fonte da verdade
        ↓              ↑
Fontes de dados                → Retrofit (rede), Room (estante), DataStore (preferências)
```

**Regras**

- A UI nunca acessa Repository, DAO ou API diretamente.
- ViewModels dependem de **interfaces** de Repository, nunca das implementações.
- DTOs (rede) e Entities (banco) **nunca chegam à UI**. São mapeados para os modelos de `:core:model`.
- A estante é **offline-first**: a UI observa sempre o Room; nenhuma tela da estante depende da rede.
- Estado da tela em `StateFlow<XUiState>`; eventos únicos (Snackbar, navegação após ação) em `Channel` exposto como `Flow`.
- Composables de tela seguem o padrão **Route + Screen**: o `XRoute` obtém o ViewModel e coleta o estado; o `XScreen` é stateless e recebe estado + callbacks (para permitir `@Preview` e testes de UI).
- O `NavController` não é passado para dentro das telas; elas recebem callbacks como `onLivroClick` e `onVoltar`.

### Módulos

```
:app                     → MainActivity, Application, NavHost, canal de notificação
:feature:estante         → telas Estante e Detalhe + ViewModels
:feature:busca           → tela de Busca + ViewModel
:feature:configuracoes   → ConfiguracoesActivity + Fragment (XML) + ViewModel
:core:model              → modelos de domínio e regras puras (Kotlin puro, sem Android)
:core:designsystem       → tema Compose (cores, tipografia), componentes reutilizáveis
:core:network            → Retrofit, DTOs, OpenLibraryApi
:core:database           → Room: Database, Entities, DAOs, TypeConverters
:core:datastore          → PreferenciasDataSource (DataStore)
:core:data               → interfaces e implementações dos Repositories, mappers
:core:lembrete           → LembreteWorker, AgendadorLembrete, criação da notificação
```

**Grafo de dependências**

```
:app → :feature:*, :core:designsystem, :core:lembrete, :core:data
:feature:* → :core:data, :core:model, :core:designsystem
:feature:configuracoes → também :core:lembrete
:core:lembrete → :core:data, :core:model
:core:data → :core:network, :core:database, :core:datastore, :core:model
:core:network, :core:database, :core:datastore → :core:model
```

- Features **não dependem umas das outras**.
- Módulos `core` **nunca dependem** de módulos `feature`.
- Prefira `implementation` a `api`.
- Classes que não precisam ser vistas fora do módulo (DTOs, Entities, implementações) devem ser `internal`.

---

## Modelo de domínio (`:core:model`)

```kotlin
enum class StatusLeitura { QUERO_LER, LENDO, LIDO }

data class Livro(
    val id: String,                 // "key" da Open Library, ex.: "/works/OL45804W"
    val titulo: String,
    val autores: List<String>,
    val anoPublicacao: Int?,
    val capaId: Long?,              // "cover_i" da Open Library
    val totalPaginas: Int?,         // "number_of_pages_median"; pode ser desconhecido
    val status: StatusLeitura,
    val paginaAtual: Int,
    val adicionadoEm: Long          // epoch millis
)

data class ResultadoBusca(
    val id: String,
    val titulo: String,
    val autores: List<String>,
    val anoPublicacao: Int?,
    val capaId: Long?,
    val totalPaginas: Int?,
    val naEstante: Boolean          // calculado cruzando com os ids salvos
)

enum class TemaApp { SISTEMA, CLARO, ESCURO }
```

### Regras de negócio (funções puras, testadas em `:core:model`)

- `Livro.progressoPercentual(): Int?` — `null` se `totalPaginas` for nulo ou zero; senão `paginaAtual * 100 / totalPaginas`, limitado a 0..100.
- `Livro.comProgresso(pagina: Int): Livro`
  - Limita `pagina` a `0..totalPaginas` (ou apenas `>= 0` se o total for desconhecido).
  - Se o livro estava em `QUERO_LER` e a página for > 0, o status passa para `LENDO`.
  - Se a página atingir `totalPaginas`, o status passa para `LIDO`.
- `Livro.comStatus(novo: StatusLeitura): Livro`
  - `LIDO` → `paginaAtual = totalPaginas` (se conhecido).
  - `QUERO_LER` → `paginaAtual = 0`.
  - `LENDO` → mantém `paginaAtual`.
- `ResultadoBusca.paraLivro(agora: Long): Livro` — cria o livro com status `QUERO_LER` e `paginaAtual = 0`.
- `Livro.autoresFormatados(): String` — junta os autores com ", " ou retorna "Autor desconhecido".

---

## Fontes de dados

### API — Open Library (`:core:network`)

- Base URL: `https://openlibrary.org/` (via `BuildConfig`)
- Endpoint de busca: `GET search.json?q={termo}&limit=20&fields=key,title,author_name,first_publish_year,cover_i,number_of_pages_median`
- Capas (bônus com Coil): `https://covers.openlibrary.org/b/id/{capaId}-M.jpg`
- Não exige chave de API.

```kotlin
@Serializable
internal data class BuscaResponse(val docs: List<LivroDto>)

@Serializable
internal data class LivroDto(
    val key: String,
    val title: String,
    @SerialName("author_name") val autores: List<String> = emptyList(),
    @SerialName("first_publish_year") val ano: Int? = null,
    @SerialName("cover_i") val capaId: Long? = null,
    @SerialName("number_of_pages_median") val paginas: Int? = null
)
```

- `Json { ignoreUnknownKeys = true }`.
- `HttpLoggingInterceptor` com nível `BODY` apenas em debug.

### Banco — Room (`:core:database`)

**Tabela `livros`** (`LivroEntity`): mesmos campos de `Livro`, com:
- `@PrimaryKey id: String`
- `autores` salvo como String via `@TypeConverter` (lista ↔ texto separado por um delimitador ou JSON)
- `status` salvo como String via `@TypeConverter` (enum ↔ `name`)

**`LivroDao`**

```kotlin
@Query("SELECT * FROM livros ORDER BY adicionadoEm DESC")
fun observarTodos(): Flow<List<LivroEntity>>

@Query("SELECT * FROM livros WHERE status = :status ORDER BY adicionadoEm DESC")
fun observarPorStatus(status: StatusLeitura): Flow<List<LivroEntity>>

@Query("SELECT * FROM livros WHERE id = :id")
fun observarPorId(id: String): Flow<LivroEntity?>

@Query("SELECT id FROM livros")
fun observarIds(): Flow<List<String>>

@Query("SELECT * FROM livros WHERE status = 'LENDO'")
suspend fun buscarLendo(): List<LivroEntity>

@Insert(onConflict = OnConflictStrategy.IGNORE)
suspend fun inserir(livro: LivroEntity): Long   // -1 se já existir

@Update
suspend fun atualizar(livro: LivroEntity)

@Query("DELETE FROM livros WHERE id = :id")
suspend fun remover(id: String)
```

- `BibliotecaDatabase` versão 1. Qualquer mudança de esquema exige incrementar a versão e escrever uma `Migration`.

### Preferências — DataStore (`:core:datastore`)

| Chave | Tipo | Padrão |
|---|---|---|
| `tema` | String (`TemaApp.name`) | `SISTEMA` |
| `lembrete_ativo` | Boolean | `false` |
| `ultima_busca` | String | vazio |

---

## Repositories (`:core:data`)

```kotlin
interface EstanteRepository {
    fun observarEstante(filtro: StatusLeitura?): Flow<List<Livro>>   // null = todos
    fun observarLivro(id: String): Flow<Livro?>
    fun observarIdsNaEstante(): Flow<Set<String>>
    suspend fun adicionar(resultado: ResultadoBusca): Boolean          // false se já existia
    suspend fun atualizarStatus(id: String, status: StatusLeitura)
    suspend fun atualizarProgresso(id: String, pagina: Int)
    suspend fun remover(id: String)
    suspend fun livrosSendoLidos(): List<Livro>
}

interface BuscaRepository {
    suspend fun buscar(termo: String): List<ResultadoBusca>           // naEstante = false; o ViewModel combina com os ids
}

interface PreferenciasRepository {
    val tema: Flow<TemaApp>
    val lembreteAtivo: Flow<Boolean>
    val ultimaBusca: Flow<String>
    suspend fun definirTema(tema: TemaApp)
    suspend fun definirLembreteAtivo(ativo: Boolean)
    suspend fun salvarUltimaBusca(termo: String)
}
```

- As atualizações de status e progresso usam as regras puras de `:core:model` (`comStatus`, `comProgresso`); o Repository só lê, aplica a regra e grava.
- Erros de rede **não são capturados no Repository**: `IOException` e `HttpException` sobem para o ViewModel, que os converte em estado/evento.
- Bindings via `@Binds` em um módulo Hilt `@InstallIn(SingletonComponent::class)`.

---

## Telas

### Estante (`:feature:estante`) — tela inicial

**UiState**

```kotlin
sealed interface EstanteUiState {
    data object Loading : EstanteUiState
    data class Success(val livros: List<Livro>, val filtro: StatusLeitura?) : EstanteUiState
    data class Vazia(val filtro: StatusLeitura?) : EstanteUiState
}
```

- Filtro com chips: Todos / Quero ler / Lendo / Lido. O filtro selecionado fica em um `MutableStateFlow` no ViewModel e é combinado com o Repository via `flatMapLatest`.
- Cada item mostra título, autores, status e uma barra de progresso (quando `progressoPercentual()` não for nulo).
- Estado vazio com mensagem e botão "Buscar livros".
- `TopAppBar` com ação para abrir Configurações.
- `FloatingActionButton` para abrir a Busca.

### Busca (`:feature:busca`)

**UiState**

```kotlin
sealed interface BuscaUiState {
    data object Ocioso : BuscaUiState                // termo vazio
    data object Carregando : BuscaUiState
    data class Resultados(val itens: List<ResultadoBusca>) : BuscaUiState
    data object SemResultados : BuscaUiState
    data class Erro(val mensagem: String) : BuscaUiState
}
```

- O termo digitado fica em `MutableStateFlow<String>` no ViewModel.
- Pipeline: `termo.debounce(400).map { it.trim() }.distinctUntilChanged().flatMapLatest { ... }`; termos com menos de 3 caracteres resultam em `Ocioso`.
- Os resultados são combinados (`combine`) com `observarIdsNaEstante()` para marcar `naEstante`. O botão vira "Na estante" (desabilitado) quando já adicionado.
- `IOException` → `Erro("Sem conexão com a internet")`; `HttpException` → `Erro("Erro no servidor (código)")`, com botão "Tentar novamente".
- Ao adicionar: evento de Snackbar "Adicionado à estante".
- O último termo é salvo no DataStore e restaurado ao abrir a tela.

### Detalhe (`:feature:estante`)

**UiState**

```kotlin
sealed interface DetalheUiState {
    data object Loading : DetalheUiState
    data class Success(val livro: Livro) : DetalheUiState
    data object NaoEncontrado : DetalheUiState          // ex.: removido
}
```

- Recebe o `id` pela rota tipada `DetalheDestino(livroId: String)`; o ViewModel lê o argumento via `SavedStateHandle.toRoute()`.
- Mostra título, autores, ano, total de páginas (ou "desconhecido") e progresso.
- Seletor de status (segmented button ou chips).
- Campo numérico para página atual com botão "Salvar". Funciona mesmo com total de páginas desconhecido (nesse caso não há barra de progresso nem mudança automática para `LIDO`).
- Botão "Remover da estante" com diálogo de confirmação; após remover, emite evento para voltar à tela anterior.

### Configurações (`:feature:configuracoes`) — XML

- `ConfiguracoesActivity` (`AppCompatActivity`, `@AndroidEntryPoint`) com `activity_configuracoes.xml` contendo uma `MaterialToolbar` e um `FragmentContainerView`.
- `ConfiguracoesFragment` com `fragment_configuracoes.xml` (ConstraintLayout):
  - `RadioGroup` com Sistema / Claro / Escuro.
  - `MaterialSwitch` "Lembrete diário de leitura".
- ViewBinding no padrão `_binding` / `onDestroyView` → `_binding = null`.
- `ConfiguracoesViewModel` (`@HiltViewModel`) expõe `StateFlow` com tema e lembrete; coleta com `repeatOnLifecycle(STARTED)` usando `viewLifecycleOwner`.
- O tema escolhido é aplicado nesta Activity via `delegate.localNightMode`.
- Ao ativar o lembrete no Android 13+: pedir `POST_NOTIFICATIONS` com `registerForActivityResult`. Se negado, o switch volta para desligado e é mostrada uma mensagem.
- Ativar/desativar chama `AgendadorLembrete.agendar()` / `cancelar()` e salva no DataStore.
- Aberta a partir da Estante (Compose) com um `Intent`.
- Adicionar logs de ciclo de vida (`DefaultLifecycleObserver`) na Activity e no Fragment em builds debug, para fins de estudo.

### App (`:app`)

- `MinhaEstanteApp`: `@HiltAndroidApp`, implementa `Configuration.Provider` com `HiltWorkerFactory` e cria o canal de notificação.
- Remover o inicializador padrão do WorkManager no `AndroidManifest.xml` (`tools:node="remove"`).
- `MainActivity` (`ComponentActivity`, `@AndroidEntryPoint`): `enableEdgeToEdge()` e `setContent`, com o tema Compose recebendo `darkTheme` calculado a partir de `PreferenciasRepository.tema` (via um `MainViewModel`).
- `NavHost` com rotas tipadas: `EstanteDestino`, `BuscaDestino`, `DetalheDestino(livroId)`.

---

## Lembrete diário (`:core:lembrete`)

- `LembreteWorker`: `@HiltWorker`, `CoroutineWorker`, recebe `EstanteRepository` e `PreferenciasRepository` via `@AssistedInject`.
  - Se `lembreteAtivo` for falso ou não houver livros `LENDO` → `Result.success()` sem notificar.
  - Senão, notifica: "Você está lendo {título}. Que tal 20 minutos hoje?" (se houver mais de um, "Você tem {n} livros em andamento").
  - Ao tocar na notificação, abre a `MainActivity`.
  - Verificar permissão de notificação antes de notificar (Android 13+).
- `AgendadorLembrete` (interface + implementação):
  - `agendar()`: `PeriodicWorkRequest` de 24h, com `initialDelay` calculado até as próximas 20h, `enqueueUniquePeriodicWork("lembrete_leitura", ExistingPeriodicWorkPolicy.UPDATE, ...)`.
  - `cancelar()`: `cancelUniqueWork("lembrete_leitura")`.
- Em builds debug, oferecer um modo de disparar o worker imediatamente (`OneTimeWorkRequest`) para teste, por exemplo um botão visível apenas em debug nas Configurações.

---

## Convenções de código

- Identificadores do domínio em **português** (`Livro`, `StatusLeitura`, `adicionar`); termos técnicos de framework em inglês como sufixo (`EstanteRepository`, `BuscaViewModel`, `LivroDao`, `LivroEntity`, `LivroDto`, `BuscaUiState`).
- Mappers como extension functions: `LivroDto.toResultadoBusca()`, `LivroEntity.toDomain()`, `Livro.toEntity()`.
- Um arquivo por classe pública principal; UiState no mesmo arquivo do ViewModel ou em `XUiState.kt`.
- Composables reutilizáveis aceitam `modifier: Modifier = Modifier` como primeiro parâmetro opcional.
- Todo `XScreen` tem `@Preview` para cada estado relevante.
- Strings visíveis ao usuário em `strings.xml` (português).
- Sem `!!` em código de produção, exceto no padrão de ViewBinding em Fragment.
- Capturar exceções específicas (`IOException`, `HttpException`); nunca capturar `Exception` genérica dentro de coroutines sem relançar `CancellationException`.
- Funções curtas, nomes que revelam intenção, sem números mágicos (usar constantes).

---

## Testes

**Unitários (`src/test`)**

| Alvo | O que testar | Ferramentas |
|---|---|---|
| `:core:model` | `progressoPercentual`, `comProgresso`, `comStatus`, `paraLivro`, `autoresFormatados` | JUnit |
| `:core:network` | mapeamento `LivroDto` → `ResultadoBusca` (campos ausentes) | JUnit |
| `:core:data` | Repositories com API e DAO mockados; regras aplicadas na gravação; `adicionar` retornando `false` para duplicado | MockK, coroutines-test |
| `EstanteViewModel` | Loading → Success/Vazia; troca de filtro | Fake repository, Turbine |
| `BuscaViewModel` | debounce (termo curto → Ocioso), resultados, sem resultados, erro de rede e de servidor, marcação `naEstante`, evento ao adicionar | Fake repository, Turbine, `advanceTimeBy` |
| `DetalheViewModel` | carregar, atualizar status/progresso, remover → evento de voltar | Fake repository, Turbine |
| `ConfiguracoesViewModel` | alterar tema e lembrete chama DataStore e agendador | MockK |
| `LembreteWorker` | sem livros lendo → não notifica; lembrete desativado → não notifica | `TestListenableWorkerBuilder` (instrumentado) ou lógica extraída para classe testável |

- Usar `MainDispatcherRule` para substituir `Dispatchers.Main`.
- Fakes dos Repositories ficam em um source set/módulo de teste compartilhado (ex.: `:core:testing`) se forem reutilizados entre módulos.
- Nomes de testes unitários com crases e no formato "quando X, então Y".

**Instrumentados (`src/androidTest`)**

- `LivroDaoTest` com `Room.inMemoryDatabaseBuilder`: inserir, ignorar duplicado, filtrar por status, atualizar, remover, `observarIds`.
- `EstanteScreenTest` e `BuscaScreenTest` com `createComposeRule`: estados vazios, erro, clique em item e em "Adicionar".
- Nomes em camelCase (sem crases).

---

## Etapas de implementação

Cada etapa é desenvolvida em uma branch própria (`etapa/NN-nome`) e integrada à `main` por Pull Request, com descrição do que foi feito e do que foi aprendido. Commits no padrão Conventional Commits (`feat:`, `fix:`, `test:`, `refactor:`, `chore:`, `docs:`).

### Etapa 1 — Setup (`etapa/01-setup`)

**Conceitos:** Gradle, version catalog, modularização, Git.

- Criar o projeto no Android Studio (Empty Activity com Compose).
- Criar o repositório no GitHub com `.gitignore` de Android.
- Configurar `gradle/libs.versions.toml` com todas as dependências e plugins da stack.
- Criar todos os módulos listados (ainda vazios), com plugins corretos (`com.android.library` ou `org.jetbrains.kotlin.jvm` para `:core:model`) e as dependências entre eles.
- Configurar `BuildConfig` com `BASE_URL`.

**Pronto quando:** `./gradlew assembleDebug` passa e o app abre uma tela "Hello".

### Etapa 2 — Domínio (`etapa/02-dominio`)

**Conceitos:** Kotlin, POO, data classes, enums, extension functions, testes unitários.

- Implementar `Livro`, `ResultadoBusca`, `StatusLeitura`, `TemaApp` em `:core:model`.
- Implementar todas as regras de negócio puras.
- Escrever os testes unitários das regras.

**Pronto quando:** todas as regras têm testes e `./gradlew :core:model:test` passa.

### Etapa 3 — Estante sem rede (`etapa/03-estante-local`)

**Conceitos:** Room, Repository, MVVM, StateFlow, Compose, state hoisting.

- Implementar `:core:database` (Entity, TypeConverters, DAO, Database).
- Implementar `EstanteRepository` e sua implementação em `:core:data`.
- Implementar `EstanteViewModel` e as telas `EstanteRoute`/`EstanteScreen` com filtro por status.
- Criar o ViewModel com uma `ViewModelProvider.Factory` **manual** (sem Hilt ainda), montando Database → DAO → Repository à mão.
- Inserir alguns livros de exemplo na primeira execução (código temporário, apenas debug).

**Pronto quando:** a estante lista os livros de exemplo, o filtro funciona e o estado sobrevive à rotação.

### Etapa 4 — Hilt (`etapa/04-hilt`)

**Conceitos:** injeção de dependência, `@Provides` vs `@Binds`, escopos.

- Adicionar `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`.
- Criar `DatabaseModule` e `DataModule` (bindings dos Repositories).
- Remover a factory manual; usar `hiltViewModel()`.

**Pronto quando:** o app funciona igual à etapa 3, sem nenhuma construção manual de dependências.

### Etapa 5 — Busca (`etapa/05-busca`)

**Conceitos:** Retrofit, kotlinx.serialization, DTO e mapeamento, Flow (`debounce`, `flatMapLatest`, `combine`), tratamento de erros, DataStore.

- Implementar `:core:network` (API, DTOs, `NetworkModule`).
- Implementar `BuscaRepository`.
- Implementar `:core:datastore` e `PreferenciasRepository` (inicialmente só `ultimaBusca`).
- Implementar `BuscaViewModel` e as telas de Busca, com adição à estante.
- Remover os livros de exemplo da etapa 3.

**Pronto quando:** é possível buscar, adicionar livros e vê-los na estante; sem internet aparece o estado de erro com "Tentar novamente"; a estante continua funcionando offline.

### Etapa 6 — Detalhe e navegação (`etapa/06-detalhe`)

**Conceitos:** Navigation Compose com rotas tipadas, `SavedStateHandle`, eventos únicos, diálogos.

- Configurar o `NavHost` com as três rotas.
- Implementar `DetalheViewModel` e as telas de Detalhe (status, progresso, remoção).

**Pronto quando:** é possível navegar Estante → Detalhe → voltar, alterar status/progresso refletindo na estante, e remover um livro.

### Etapa 7 — Configurações em XML (`etapa/07-configuracoes`)

**Conceitos:** Activity e Fragment, ciclos de vida, layouts XML, ViewBinding, `repeatOnLifecycle`, DataStore, permissões em tempo de execução.

- Implementar `ConfiguracoesActivity`, `ConfiguracoesFragment` e layouts.
- Implementar `ConfiguracoesViewModel`.
- Completar `PreferenciasRepository` (tema e lembrete).
- Aplicar o tema escolhido no app Compose (`MainViewModel`) e na própria Activity.
- Switch de lembrete apenas salva a preferência e pede permissão (o agendamento entra na etapa 8).
- Adicionar logs de ciclo de vida para estudo.

**Pronto quando:** trocar o tema muda o app inteiro; as preferências persistem após fechar o app; os logs mostram a ordem dos callbacks ao girar a tela e ao navegar.

### Etapa 8 — Lembrete (`etapa/08-lembrete`)

**Conceitos:** WorkManager, `CoroutineWorker`, Hilt Worker, constraints, unique work, notificações.

- Configurar `HiltWorkerFactory` na Application e remover o inicializador padrão.
- Criar o canal de notificação.
- Implementar `LembreteWorker` e `AgendadorLembrete`.
- Ligar o switch das Configurações ao agendador.
- Adicionar o disparo imediato em debug.

**Pronto quando:** com o lembrete ativo e um livro "Lendo", o disparo de debug gera a notificação; tocar nela abre o app; desativar cancela o trabalho agendado.

### Etapa 9 — Testes e CI (`etapa/09-testes-ci`)

**Conceitos:** MockK, Turbine, coroutines-test, testes instrumentados, GitHub Actions.

- Completar todos os testes listados na seção **Testes**.
- Criar `.github/workflows/ci.yml` que, em todo push e Pull Request para `main`, faz checkout, configura JDK 17, configura o Gradle e roda `./gradlew testDebugUnitTest lintDebug`.
- Adicionar o badge de CI no README.

**Pronto quando:** o pipeline passa no GitHub e um PR com teste quebrado falha o check.

### Etapa 10 — README e polimento (`etapa/10-readme`)

- README em inglês: descrição, prints/GIF das telas, stack, diagrama de camadas e módulos, seção "Technical decisions" (offline-first, Room vs DataStore, Route/Screen, por que XML em Configurações), como rodar e como testar.
- Revisar strings, ícone do app, estados vazios e acessibilidade básica (`contentDescription`).

### Bônus A — Capas com Coil (`bonus/capas`)

- `AsyncImage` com a URL de capa nos itens da Estante, Busca e Detalhe, com placeholder quando `capaId` for nulo.

### Bônus B — Kotlin Multiplatform (`bonus/kmp`)

**Conceitos:** source sets, `expect`/`actual`, Ktor, Koin, Compose Multiplatform.

- Criar módulo `:shared` (alvos Android e desktop; iOS apenas se houver Mac).
- Mover os modelos de busca e DTOs para `commonMain`; implementar `OpenLibraryClient` com Ktor.
- Implementar `expect fun nomePlataforma(): String` com `actual` por plataforma.
- Criar um app desktop simples (Compose Multiplatform) com a tela de busca.
- O app Android continua usando Retrofit/Hilt; a integração do `:shared` no app Android é opcional.

---

## Comandos úteis

```bash
./gradlew assembleDebug            # gera o APK de debug
./gradlew testDebugUnitTest        # testes unitários de todos os módulos
./gradlew :core:model:test         # testes de um módulo JVM
./gradlew connectedDebugAndroidTest # testes instrumentados (emulador ligado)
./gradlew lintDebug                # análise estática
```

---

## Status

- [x] Etapa 1 — Setup
- [x] Etapa 2 — Domínio
- [ ] Etapa 3 — Estante sem rede
- [ ] Etapa 4 — Hilt
- [ ] Etapa 5 — Busca
- [ ] Etapa 6 — Detalhe e navegação
- [ ] Etapa 7 — Configurações em XML
- [ ] Etapa 8 — Lembrete
- [ ] Etapa 9 — Testes e CI
- [ ] Etapa 10 — README e polimento
- [ ] Bônus A — Capas com Coil
- [ ] Bônus B — Kotlin Multiplatform

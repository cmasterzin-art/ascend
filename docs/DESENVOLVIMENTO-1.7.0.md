# ASCEND 1.7.0 — Os Nove Caminhos (Diário de Desenvolvimento)

- **Branch base:** `feat/ascend-1.6.9.19`
- **Branch de trabalho:** `feat/ascend-1.7.0`
- **Versão alvo:** `versionName = "1.7.0"`, `versionCode = 20`, `applicationId = "br.com.ascend.aetheris"`
- **Direção:** Personal Projects & Advanced Productivity Update — offline, Kotlin/Compose/Room/DataStore, reutilizando integralmente a Biblioteca da Forja (1.6.9.19) e preservando todos os dados, recompensas e os 135 testes existentes.

---

## Plano por Fases

- [x] **Fase 1 — Auditoria técnica e ambiente**
  - Criada branch `feat/ascend-1.7.0` a partir de `feat/ascend-1.6.9.19`.
  - Auditados `Domain.kt`, `Database.kt`, `Repository.kt`, `Productivity.kt`, `ProductivityScreens.kt`, `JourneyHome.kt`, `JourneyScreens.kt`, `ChronicleData.kt`, `ChronicleScreens.kt`, `DesignSystem2.kt`, `Design.kt`, `Forms.kt`, `AetherisNavigation.kt`, `MainActivity.kt`, esquemas Room (`6.json`), testes unitários e testes visuais Robolectric.
  - Configurado Android SDK 35 + Gradle 8.9 em espaço de usuário para validação local completa (`testDebugUnitTest`, `lintDebug`, `assembleDebug` e capturas Robolectric).
- [x] **Fase 2 — Modelo de dados, Room v7 (`MIGRATION_6_7`), Backup v9 e Repositório**
  - Entidades Room: `PersonalPath`, `PathStage`, `PathTask`, `PathSubtask`, `PathHabitLink`, `TimeBlock`, `AgendaEvent`, `InboxItem`.
  - Extensão compatível de `Focus` e `FocusSession` para vínculo opcional a projeto (`pathId`) e/ou tarefa (`taskId`), preservando sessões de hábitos e compatibilidade com `6.json`.
  - Migração explícita `MIGRATION_6_7` + esquema `7.json` gerado e conferido linha a linha.
  - Backup v9 retrocompatível com versões v1..v8, validação estrita e importação transacional atômica com rollback.
  - Lógica pura de `Caminhos`, `Agenda 2.0` (Dia/Semana/Mês, sobreposição de blocos, planejado vs efetivo), `Central de Pendências`, `Rumos do Dia` e vínculo com `Salão das Memórias` / `Crônica`.
  - Validado `./gradlew testDebugUnitTest`: **135 testes aprovados, 0 falhas**.
- [x] **Fase 3 — Interface de Caminhos (Lista, Editor, Detalhes, Etapas, Tarefas, Subtarefas, Duplicação)**
  - Implementado `CaminhosScreens.kt` (`PathsScreen`, `PathEditorDialog`, `PathDetailScreen`, `TaskEditorDialog`) com filtros por estado, busca, etapas com progresso real, tarefas, subtarefas reordenáveis, mover/duplicar e confirmações explícitas.
- [x] **Fase 4 — Agenda Profissional 2.0 (Dia/Semana/Mês, Blocos de Tempo, Eventos, Caixa de Entrada, Pendências)**
  - Implementado `AgendaScreens.kt` (`PlannerScreen` com modos `Dia`/`Semana`/`Mês`, blocos de tempo com detecção de sobreposição e comparação planejado vs foco real, compromissos sem XP, `InboxScreen` com triagem atômica e `PendingCenterScreen` com 6 recortes).
- [x] **Fase 5 — Integração na Jornada ("Rumos do Dia"), Vínculo de Hábitos, Foco em Projeto/Tarefa e Salão das Memórias**
  - Adicionados `ShortcutStrip` (`Caminhos`, `Entrada`, `Pendências`) e `RumosDoDiaPanel` em `JourneyHome.kt`, foco em projeto/tarefa sem XP inventado em `MainActivity.kt` / `JourneyScreens.kt` / `ProductivityScreens.kt`, e navegação completa em `AetherisNavigation.kt` e `CarvedArt.kt`.
- [x] **Fase 6 — Acabamento visual, acessibilidade, tema claro/escuro, `320dp` / fonte `130%`, modo minimalista e redução de movimento**
  - Inspecionadas visualmente todas as 11 capturas novas (`411dp` e `320dp / 130%`), corrigindo empilhamento em telas estreitas (`PlannerScreen`), `FlowRow` no progresso (`PathsScreen`), alvos táteis (`44dp`/`48dp`) e alinhamento do cabeçalho (`PendingCenterScreen`).
- [x] **Fase 7 — Bateria abrangente de testes (135 preservados + novos testes 1.7.0) e capturas reais**
  - Criados [`PathsAndAgendaTest.kt`](../app/src/test/java/br/com/ascend/aetheris/PathsAndAgendaTest.kt) (5 testes de domínio, migração Room `v6 → v7`, foco sem XP extra e backup `v9` com rollback atômico) e [`PathsAndAgendaUiTest.kt`](../app/src/test/java/br/com/ascend/aetheris/PathsAndAgendaUiTest.kt) (3 testes de UI e capturas em `411dp` e `320dp / 130%`).
  - Executada toda a suíte: **143 testes aprovados, 0 falhas, 0 erros, 0 ignorados**.
- [x] **Fase 8 — Build final, Lint 0 erros, documentação e entrega CI assinada**
  - Atualizado `versionCode = 20` / `versionName = "1.7.0"`, workflow `.github/workflows/android.yml`, `README.md`, `CHANGELOG.md`, `PROGRESSO.md`, `VALIDACAO.md`, `INSTALACAO.md`, `ASSINATURA.md`, `CAMINHOS-1.7.0.md`, `AGENDA-1.7.0.md` e `VISUAL-1.7.0.md`.
  - Validado `lintDebug` (**0 erros, 22 avisos**) e `assembleDebug` (`aapt2`: `versionCode 20`, `versionName 1.7.0`, sem permissão `INTERNET`).

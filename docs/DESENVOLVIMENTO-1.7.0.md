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
- [ ] **Fase 3 — Interface de Caminhos (Lista, Editor, Detalhes, Etapas, Tarefas, Subtarefas, Duplicação)**
- [ ] **Fase 4 — Agenda Profissional 2.0 (Dia/Semana/Mês, Blocos de Tempo, Eventos, Caixa de Entrada, Pendências)**
- [ ] **Fase 5 — Integração na Jornada ("Rumos do Dia"), Vínculo de Hábitos, Foco em Projeto/Tarefa e Salão das Memórias**
- [ ] **Fase 6 — Acabamento visual, acessibilidade, tema claro/escuro, `320dp` / fonte `130%`, modo minimalista e redução de movimento**
- [ ] **Fase 7 — Bateria abrangente de testes (135 preservados + novos testes 1.7.0) e capturas reais**
- [ ] **Fase 8 — Build final, Lint 0 erros, documentação (`CAMINHOS-1.7.0.md`, `AGENDA-1.7.0.md`, `VISUAL-1.7.0.md`, `README.md`, `CHANGELOG.md`, `PROGRESSO.md`, `VALIDACAO.md`, `INSTALACAO.md`) e entrega CI assinada**

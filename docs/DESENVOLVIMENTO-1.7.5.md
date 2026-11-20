# Registro de Desenvolvimento — ASCEND 1.7.5 (A Mesa de Odin)

- **Branch de origem autorizada:** `feat/ascend-1.7.3.1` (`13922af`, Room v10, Backup v12, 202 testes).
  - Nota de rastreabilidade: a referência inicial à branch `feat/ascend-1.7.4` (Oráculo de Aetheris) não existia no repositório; mediante confirmação explícita do usuário, adotou-se `feat/ascend-1.7.3.1` como base direta e omitiu-se o módulo Oráculo.
- **Nova branch:** `feat/ascend-1.7.5`
- **Versionamento:** `versionName = "1.7.5"`, `versionCode = 27`
- **Banco e Backup:** Room `v10 → v11` (`MIGRATION_10_11` puramente aditiva, 9 novas tabelas, zero migração destrutiva) e Backup `v12 → v13` (compatível `v1`–`v13` com validação de ciclos/intervalos e rollback atômico).

## Checkpoints Executados

1. **Checkpoint 1 (`5622f56`):**
   - Etapa 0 (6.1–6.4): favoritos do Codex Rúnico persistidos em DataStore (`codex_favorite_runes`), confirmação de zero chamadas UI legadas a `equip()`, política documentada `expeditionFocusRulePolicy` e correção das 12 runas originais em `docs/CODEX-RUNICO.md`.
   - 10 gerações pictóricas originais (`10/12`, `2` de reserva intactas), otimizadas para `664.174 bytes` (`0,63 MB`) no APK com limpeza RGBA inspecionada.
2. **Checkpoint 2 (Núcleo Estratégico + UI + Testes):**
   - `StrategicData.kt`, `OdinPlanningEngine.kt`, `Database.kt` (Room v11 + `MIGRATION_10_11` + `schemas/11.json`), `Repository.kt` (transação `applyOdinProposal`, `undoAppliedPlan` inteligente, Pomodoro/monotônico, Backup v13), `AmbientAudio.kt`, `Reminders.kt`.
   - `NordicCouncilDesign.kt` (DS 3.2), `OdinCouncilScreens.kt`, `Paths30Screens.kt`, `TimeForgeScreen.kt`, `CouncilOfNineScreen.kt`, integrações em `CaminhosScreens.kt`, `AgendaScreens.kt`, `JourneyHome.kt`, `AetherisNavigation.kt`, `MainActivity.kt`.
   - `OdinCouncilTest.kt` (incluindo benchmarks 50/200/1.000 tarefas e migração SQLite v10→v11) + `OdinCouncilUiTest.kt` (17 capturas reais em `docs/screenshots/1.7.5/`, com 2 ajustes visuais aplicados após inspeção real em Gantt e Forja do Tempo).
3. **Checkpoint 3 (Validação Completa, Lint, Docs e CI Oficial):**
   - Suíte completa `testDebugUnitTest`, `assembleDebug lintDebug`, documentação completa e publicação via GitHub Actions com assinatura oficial `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`.

# ASCEND 1.7.5.1 — Handoff Técnico para Gemini Neon High
## Transição da Etapa I (Engineering & Reliability Pass) para a Etapa II (Nordic Visual Refinement & Official Distribution)

Data: 20/11/2026
Repositório: `https://github.com/cmasterzin-art/ascend`
Branch de origem preservada: `feat/ascend-1.7.5` (`0b07076`)
Branch ativa para a Etapa II: `feat/ascend-1.7.5.1`
Versionamento já configurado em `app/build.gradle.kts`: `versionName = "1.7.5.1"`, `versionCode = 28`

---

## 1. Commits da Etapa I (`feat/ascend-1.7.5.1`)

1. `225f5da` — `test(1.7.5.1): independent engineering audit report and deterministic reproduction test suite [skip ci]`
2. `6886916` — `fix(1.7.5.1): P0 automatic Pomodoro phase transitions and ON_RESUME lifecycle reconciliation [skip ci]`
3. `00c996d` — `fix(1.7.5.1): P1 operational recurrence projections in agendaDay, monthMarker, and Agenda 3.0 [skip ci]`
4. `2349960` — `fix(1.7.5.1): P1 full-schedule dependency gating, PlanningClock past-hour filtering, and no-9am replan fallback [skip ci]`
5. `9d66ef8` — `fix(1.7.5.1): P1 centralized schedule slot validation, partial-apply dependency checks, and conditional task snapshot undo [skip ci]`
6. Commit final de documentação e handoff desta Etapa I (`docs(1.7.5.1): record technical stabilization and Neon High handoff [skip ci]`).

---

## 2. Correções Realizadas (NÃO REVERTER NA ETAPA II)

1. **P0 — Avanço Automático do Pomodoro e Reconciliação de Ciclo de Vida:**
   - Adicionado `Focus.isPhaseCompleted(now, nowRealtimeMs): Boolean` em `Domain.kt`.
   - Implementado `GameRepository.reconcileFocusTimer(now, mono): Boolean` em `Repository.kt`, protegido pelo `Mutex` (`lock`) e `db.withTransaction`.
   - Transiciona automaticamente entre trabalho e pausa curta/longa quando `autoAdvance == true`, encerra o último ciclo (`currentCycle >= totalCycles`), impede contagem de pausas como foco, evita sessões ou transições duplicadas em concorrência e nunca avança sozinho quando `autoAdvance == false`.
   - `MainActivity.kt` chama `vm.repo.reconcileFocusTimer()` em `Lifecycle.Event.ON_RESUME` e `TimeForgeScreen.kt` reconcilia no loop de `tick`.
2. **P1 — Recorrências Operacionais na Agenda 3.0 e Mesa de Odin:**
   - Implementadas `OdinPlanningEngine.effectiveBlocksForDate(s, date)` e `OdinPlanningEngine.effectiveEventsForDate(s, date)` para projeção virtual sob demanda sem duplicar o template original.
   - Integradas em `busyIntervalsForDate()`, `freeIntervalsForDate()`, `generateOdinProposal()`, `simulateCapacity()`, `generateReplanProposal()`, `agendaDay()` e `monthMarker()`.
   - Tratamento seguro em `Repository.kt` (`deleteTimeBlock`, `toggleEvent`, `deleteEvent`, `togglePinTimeBlock`, `startTimeForgeFocus`) e `AgendaScreens.kt` para ocorrências virtuais (`@rec:YYYY-MM-DD`).
3. **P1 — Replanejamento Transacional e Validação Centralizada:**
   - Removido o fallback arbitrário `"09:00"` de `ReplanActionKind.POSTPONE` em `OdinPlanningEngine.generateReplanProposal()`.
   - Implementada `GameRepository.validateScheduledSlot(...)` centralizando a revalidação de disponibilidade autorizada, compromissos fixos, recorrências, blocos fixados/existentes, dias de descanso, intervalo mínimo (`bufferMinutes`) e limite diário (`dailyLimitMinutes`) em `applyOdinProposal` e `applyReplanChoice`.
4. **P1 — Dependências Parcialmente Agendadas e Seleção Parcial:**
   - Introduzido `TaskScheduleState` (`UNSCHEDULED`, `PARTIALLY_SCHEDULED`, `FULLY_SCHEDULED`, `COMPLETED`) e `OdinProposal.taskScheduleStates`.
   - `scheduledCompletion[taskId]` só é registrado quando a tarefa pré-requisito está **integralmente agendada** (`remaining == 0`).
   - `applyOdinProposal()` revalida dependências `finish-to-start` no subconjunto `selectedIds`, rejeitando seleções parciais que deixem pré-requisitos incompletos.
5. **P1 — Desfazimento Condicional Seguro (`undoAppliedPlan`):**
   - `taskSnapshotsJson` passa a gravar `[prevDate, prevTime, appliedDate, appliedTime]` (mantendo retrocompatibilidade com snapshots legados de 2 itens).
   - `undoAppliedPlan()` só restaura `scheduledDate`/`scheduledTime` da `PathTask` se o estado atual da tarefa ainda corresponder ao estado aplicado pelo plano, preservando edições manuais posteriores na tarefa, tarefas concluídas, blocos fixados/editados e planos subsequentes.
6. **P1 — Horários Passados e Referência Temporal Injetável (`PlanningClock`):**
   - Introduzida `PlanningClock(referenceDate, referenceMinuteOfDay)` com `PlanningClock.fromClock(clock)` e `PlanningClock.at(date, time)`.
   - Quando `d == clock.referenceDate`, `generateOdinProposal` e `generateReplanProposal` filtram intervalos já transcorridos antes de `referenceMinuteOfDay`, sem afetar datas futuras nem consultas históricas explícitas.

---

## 3. Arquivos Modificados / Criados na Etapa I

- `app/build.gradle.kts` (`versionCode = 28`, `versionName = "1.7.5.1"`)
- `app/src/main/java/br/com/ascend/aetheris/Domain.kt`
- `app/src/main/java/br/com/ascend/aetheris/OdinPlanningEngine.kt`
- `app/src/main/java/br/com/ascend/aetheris/Repository.kt`
- `app/src/main/java/br/com/ascend/aetheris/ProjectsAndAgenda.kt`
- `app/src/main/java/br/com/ascend/aetheris/TimeForgeScreen.kt`
- `app/src/main/java/br/com/ascend/aetheris/AgendaScreens.kt`
- `app/src/main/java/br/com/ascend/aetheris/OdinCouncilScreens.kt`
- `app/src/main/java/br/com/ascend/aetheris/MainActivity.kt`
- `app/src/test/java/br/com/ascend/aetheris/Stability1751AuditTest.kt` (8 novos métodos de teste cobrindo todos os cenários P0, P1 e Seção 12)
- `docs/AUDITORIA-1.7.5.1.md`
- `docs/ESTABILIDADE-1.7.5.1.md`
- `docs/HANDOFF-NEON-1.7.5.1.md`

---

## 4. Mudanças em Contratos Públicos e Novos Componentes

- `PlanningClock` (`OdinPlanningEngine.kt`): `data class PlanningClock(val referenceDate: String = today(), val referenceMinuteOfDay: Int = 0)` com `fromClock(Clock)` e `at(date, time)`.
- `TaskScheduleState` (`OdinPlanningEngine.kt`): `enum class TaskScheduleState { UNSCHEDULED, PARTIALLY_SCHEDULED, FULLY_SCHEDULED, COMPLETED }`.
- `UnscheduledItem.scheduleState` e `OdinProposal.taskScheduleStates`: campos com valores padrão retrocompatíveis.
- `OdinPlanningEngine.effectiveBlocksForDate(s, date)` e `OdinPlanningEngine.effectiveEventsForDate(s, date)`: projeções virtuais sob demanda de recorrências.
- `OdinPlanningEngine.busyIntervalsForDate(...)` e `freeIntervalsForDate(...)`: parâmetros opcionais `ignoreBlockId: String = ""` e `minStartMinute: Int = 0` com defaults retrocompatíveis.
- `OdinPlanningEngine.generateOdinProposal(...)` e `generateReplanProposal(...)`: parâmetro opcional `clock: PlanningClock` com default retrocompatível.
- `Focus.isPhaseCompleted(now, nowRealtimeMs): Boolean` (`Domain.kt`).
- `GameRepository.reconcileFocusTimer(now, mono): Boolean` (`Repository.kt`).

---

## 5. Resultados de Validação da Etapa I

- **`./gradlew testDebugUnitTest`:** **226 testes executados, 226 aprovados, 0 falhas, 0 erros, 0 ignorados** (`218` testes anteriores integralmente preservados + `8` novos testes em `Stability1751AuditTest`).
- **`./gradlew lintDebug`:** **0 erros, 34 avisos** (`app/build/reports/lint-results-debug.txt`).
- **`./gradlew assembleDebug`:** **BUILD SUCCESSFUL**.
- **Esquema e Backup:** **Room v11** e **Backup v13** preservados sem nenhuma alteração de esquema ou migração destrutiva.

---

## 6. Limitações, Testes Físicos Pendentes e Riscos Remanescentes

- **Restrições de Background do Android para Pomodoro:** sem um `ForegroundService` (não incluído para evitar mudanças de permissão/manifesto fora de escopo), a transição automática ocorre em tempo real enquanto a tela está visível (`LaunchedEffect` de `tick`) e é reconciliada imediatamente em `Lifecycle.Event.ON_RESUME` ao retornar do segundo plano. Caso o processo permaneça em background por múltiplos ciclos sem ser aberto, `reconcileFocusTimer` avança de forma conservadora apenas a fase concluída, sem inventar múltiplos ciclos de foco não supervisionados.
- **Testes em Dispositivo Físico Pendentes:** toda a validação automatizada ocorreu via Robolectric + JVM. Testes em hardware real (precisão de `AlarmManager` em Doze mode, áudio procedural `AudioTrack` em alto-falante físico, TalkBack e teclado IME físico) permanecem declarados como limites de ambiente.

---

## 7. Escopo Obrigatório para o Próximo Agente (Gemini Neon High — Etapa II)

O agente **Gemini Neon High** deverá partir do HEAD desta branch `feat/ascend-1.7.5.1` (**sem reverter silenciosamente nenhuma das correções técnicas acima**) e executar:

1. **Refinamento visual nórdico e viking** de toda a experiência estratégica.
2. **Melhoria dos botões** e hierarquia tátil/pictórica.
3. **Ampliação contextual das runas** nas superfícies estratégicas.
4. **Refinamento visual da Mesa de Odin** (Salão, Proposta, Capacidade, Replanejamento, Eisenhower e Disponibilidade).
5. **Melhorias visuais de Gantt e Kanban** em Caminhos 3.0.
6. **Refinamento visual da Forja do Tempo** (painel de cronômetro, fases e modo imersivo).
7. **Refinamento visual da Agenda 3.0** (blocos fixados, recorrências virtuais e indicadores).
8. **Melhorias visuais do Conselho dos Nove** (áreas da vida e objetivos estratégicos).
9. **Geração pictórica usando Nano Banana** (dentro do orçamento artístico autorizado).
10. **Inspeção visual de capturas reais** renderizadas via testes automatizados.
11. **Compilação, assinatura oficial no CI (`e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`) e distribuição do APK oficial `ASCEND-1.7.5.1.apk`**.

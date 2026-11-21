# ASCEND 1.7.5.1 — Estabilização Técnica (Etapa I)
## Engineering & Reliability Summary

Data: 20/11/2026
Branch: `feat/ascend-1.7.5.1` (derivada de `feat/ascend-1.7.5`)
Versão: `versionName = "1.7.5.1"` · `versionCode = 28`
Persistência: **Room v11** e **Backup v13** integralmente preservados (zero migrações destrutivas, zero alteração de esquema).

---

## 1. Resumo das Correções de Engenharia

### 1.1 P0 — Pomodoro Automático e Reconciliação de Ciclo de Vida (`Domain.kt`, `Repository.kt`, `TimeForgeScreen.kt`, `MainActivity.kt`)
- **Domínio (`Focus`):** adicionado `Focus.isPhaseCompleted(now, nowRealtimeMs): Boolean` sobre a medição monotônica (`seconds(now, nowRealtimeMs) >= activePhaseMinutes() * 60L`).
- **Repositório (`GameRepository.reconcileFocusTimer`):** lógica centralizada, transacional e protegida pelo `Mutex` (`lock`) para avançar fases automaticamente somente quando `autoAdvance == true`, `startedAt > 0L` e `isPhaseCompleted == true`.
  - Diferencia estritamente fases de trabalho (`"work"`) e pausas (`"short_break"`, `"long_break"`): pausas jamais criam `FocusSession` nem somam minutos ao hábito ou progresso de RPG.
  - Respeita `totalCycles` e `currentCycle`, encerrando e limpando o `Focus` ao término do último ciclo.
  - Garante idempotência sob chamadas concorrentes (duas chamadas simultâneas a `reconcileFocusTimer` resultam em exatamente uma transição e uma única `FocusSession` por `sessionId`).
  - Quando `autoAdvance == false`, o término do cronômetro nunca avança silenciosamente a fase.
- **Ciclo de Vida e UI:** `MainActivity` invoca `vm.repo.reconcileFocusTimer()` em `Lifecycle.Event.ON_RESUME` (reconciliando o estado ao retornar do segundo plano ou após recriação da `Activity`) e `TimeForgeScreen` invoca `reconcileFocusTimer` no loop de `tick` disparando as notificações locais configuradas.

### 1.2 P1 — Recorrências Operacionais na Agenda 3.0 e Mesa de Odin (`OdinPlanningEngine.kt`, `ProjectsAndAgenda.kt`, `Repository.kt`, `AgendaScreens.kt`)
- **Projeção Virtual Sob Demanda:** implementadas `OdinPlanningEngine.effectiveBlocksForDate(s, date)` e `OdinPlanningEngine.effectiveEventsForDate(s, date)`:
  - Projetam ocorrências recorrentes para a data consultada sem materializar centenas de registros no banco.
  - Evitam duplicação na data original do template ou em datas já materializadas.
  - Respeitam `active`, `startDate`, `endDate`, `excludedDates`, `daysOfWeek`, meses curtos (fevereiro 28 dias, meses de 30 dias) e anos bissextos (`29/02`).
- **Integração Completa:**
  - `busyIntervalsForDate()`, `freeIntervalsForDate()`, `generateOdinProposal()`, `simulateCapacity()` e `generateReplanProposal()` agora consideram todas as ocorrências recorrentes de blocos e compromissos.
  - `agendaDay()` e `monthMarker()` exibem e contabilizam ocorrências recorrentes no modo Dia, Semana e Mês.
  - Ações de exclusão sobre uma ocorrência virtual (`deleteTimeBlock` / `deleteEvent` com ID `@rec:YYYY-MM-DD`) registram a data em `excludedDates` da regra sem apagar o template histórico.

### 1.3 P1 — Replanejamento Transacional e Validação Centralizada (`Repository.kt`, `OdinPlanningEngine.kt`)
- **Remoção de Fallback Arbitrário às 09:00:** `OdinPlanningEngine.generateReplanProposal()` não cria mais opções `POSTPONE` com horário artificial `"09:00"` quando não há janela livre na próxima semana.
- **Validação Centralizada (`validateScheduledSlot`):** reutilizada por `applyOdinProposal` e `applyReplanChoice` (`TRANSFER`, `POSTPONE`, `SPLIT`), revalidando dentro da transação:
  - Janelas de disponibilidade autorizadas (`authorizedWindowsForDate`, dias de descanso e exceções bloqueadas);
  - Intervalos ocupados (`busyIntervalsForDate`, incluindo compromissos fixos, blocos existentes/fixados e recorrências);
  - Intervalo mínimo (`bufferMinutes`) e limite diário (`dailyLimitMinutes`);
  - Estado atual da tarefa/caminho e pré-requisitos `finish-to-start`.

### 1.4 P1 — Dependências e Seleção Parcial (`OdinPlanningEngine.kt`, `Repository.kt`)
- **Quatro Estados Explícitos (`TaskScheduleState`):** `UNSCHEDULED`, `PARTIALLY_SCHEDULED`, `FULLY_SCHEDULED` e `COMPLETED`.
- **Bloqueio Estrito de Agendamento Parcial:** `scheduledCompletion[taskId]` só recebe registro quando `remaining == 0` (`FULLY_SCHEDULED`). Se um pré-requisito couber apenas parcialmente no horizonte, as tarefas dependentes permanecem bloqueadas em `unscheduled` com motivo explícito.
- **Validação de Seleção Parcial em `applyOdinProposal`:** se o usuário desmarcar sugestões de uma tarefa pré-requisito deixando-a incompleta enquanto mantém selecionada uma tarefa dependente, a transação é rejeitada com mensagem explicativa e rollback total.

### 1.5 P1 — Desfazimento Condicional Seguro (`Repository.kt`)
- **Snapshot Estendido Compatível:** `taskSnapshotsJson` em `AppliedPlanRecord` armazena `[prevDate, prevTime, appliedDate, appliedTime]` (mantendo compatibilidade com snapshots legados de 2 elementos).
- **Restauração Condicional em `undoAppliedPlan`:** `scheduledDate` e `scheduledTime` de uma `PathTask` só são restaurados para `[prevDate, prevTime]` se a tarefa não estiver concluída/arquivada, não tiver bloco preservado (`modifiedManually || pinned`) e seu agendamento atual ainda corresponder ao estado aplicado pelo plano (`appliedDate`, `appliedTime`). Edições manuais posteriores na tarefa e planos subsequentes são integralmente preservados.

### 1.6 P1 — Horários Passados e Referência Temporal Injetável (`OdinPlanningEngine.kt`, `OdinCouncilScreens.kt`)
- **`PlanningClock`:** referência temporal injetável (`referenceDate`, `referenceMinuteOfDay`, `PlanningClock.fromClock(clock)` e `PlanningClock.at(date, time)`).
- Quando o horizonte inclui `referenceDate`, `generateOdinProposal` e `generateReplanProposal` truncam intervalos anteriores a `referenceMinuteOfDay`, impedindo sugestões no passado sem afetar datas futuras ou consultas históricas explícitas.

---

## 2. Invariantes Preservadas
- **Room v11** (`app/schemas/br.com.ascend.aetheris.GameDatabase/11.json`) e **Backup v13** (`v1`–`v13`) inalterados e 100% compatíveis.
- Zero impacto na economia de RPG (XP, Fragmentos de Aether, ledger, conquistas, coleções lendárias, heráldica e retratos).
- Zero permissão `INTERNET`.

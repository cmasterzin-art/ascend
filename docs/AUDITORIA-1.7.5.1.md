# ASCEND 1.7.5.1 — Auditoria Independente e Estabilização Técnica
## Etapa I — Engineering & Reliability Pass

Data: 20/11/2026
Branch de origem: `feat/ascend-1.7.5` (`0b07076`)
Branch de destino: `feat/ascend-1.7.5.1`
Escopo: Auditoria técnica independente dos seis riscos prioritários (P0 e P1) e dos cenários complementares de concorrência, integridade referencial, transições temporais e retrocompatibilidade (Room v11 e Backup v13 preservados sem migração destrutiva).

---

## 1. P0 — Avanço Automático do Pomodoro (`autoAdvance`) e Ciclo de Vida

- **Hipótese:** A interface e a entidade `Focus` armazenam `autoAdvance = true`, mas o término do cronômetro (`remaining == 0`) não aciona automaticamente a transição de fase; o avanço depende exclusivamente de clique manual no botão ou não se reconcilia ao retornar do segundo plano / recriar a `Activity`.
- **Reprodução:** Iniciar `startTimeForgeFocus(mode = "pomodoro", workMinutes = 25, shortBreakMinutes = 5, totalCycles = 2, autoAdvance = true)`, avançar o relógio em 25 minutos (`1500s`) e observar que `Focus.phase` permanece em `"work"` até que `advanceFocusPhase()` seja invocado manualmente. Em `TimeForgeScreen.kt`, o `LaunchedEffect(focus?.startedAt, focus?.phase)` apenas atualizava `tick = System.currentTimeMillis()` a cada segundo, sem chamar `reconcileFocusTimer()` ou `advanceFocusPhase()`.
- **Causa:**
  1. Ausência de um método centralizado e idempotente no domínio/repositório (`reconcileFocusTimer(now, mono)`) capaz de inspecionar se a fase ativa atingiu sua duração (`seconds(now, mono) >= activePhaseMinutes() * 60L`) e realizar a transição ou encerramento respeitando `autoAdvance`.
  2. Quando `autoAdvance == false`, o término da fase não pausava/estabilizava o término nem impedia reentradas concorrentes; quando `autoAdvance == true`, nenhuma rotina no repositório ou no loop de `tick` / `ON_RESUME` reconciliava a transição.
  3. Ao retornar do segundo plano após tempo decorrido suficiente para concluir uma fase, era necessário avançar exatamente a fase concluída na marca temporal real (`phaseEndTime`) sem inventar foco durante períodos não medidos com confiabilidade (ex.: se `autoAdvance` transiciona de trabalho para pausa em segundo plano, ou de pausa para o próximo trabalho, sem atribuir trabalho fictício não executado).
- **Correção:**
  1. Implementado `GameRepository.reconcileFocusTimer(now: Long = System.currentTimeMillis(), mono: Long = monotonicNow()): Boolean` protegido pelo `Mutex` (`lock`) e transação Room (`change`):
     - Verifica se há `Focus` ativo com `startedAt > 0L` e se `f.seconds(now, mono) >= f.activePhaseMinutes() * 60L`.
     - Se `autoAdvance == false`, não avança silenciosamente a fase; mantém a fase concluída aguardando confirmação explícita do usuário (ou registra/transiciona apenas quando solicitado).
     - Se `autoAdvance == true`, executa a transição idempotente de fase (`advanceFocusPhaseInternal(f, now, mono, autoTriggered = true)`):
       - Se estava em `"work"`, registra a sessão única de trabalho (`recordWorkFocusIfNeeded`, garantida como única por `sessionId` em `focus_sessions` e baseline idempotente no `HabitRecord`), e:
         - Se for o último ciclo (`currentCycle >= totalCycles`) ou modo sem pausa (`!spec.usesBreaks`), encerra e limpa o `Focus` (`d.clearFocus()`).
         - Caso contrário, transiciona para `"short_break"` ou `"long_break"` (a cada 4º ciclo quando `longBreakMinutes > 0`) iniciando o cronômetro da pausa.
       - Se estava em pausa (`isBreakPhase`), nunca registra `FocusSession` nem soma minutos de hábito, e avança para `"work"` no ciclo `currentCycle + 1` com novo `sessionId` e baseline atualizado.
       - Se o aplicativo ficou em segundo plano por tempo superior a uma fase, `reconcileFocusTimer` conclui a fase expirada de forma conservadora (sem simular múltiplos ciclos de trabalho não supervisionados em background e sem inventar foco).
  2. `TimeForgeScreen` (modo normal e `ImmersiveNordicFocusView`) e o ciclo de vida (`ON_RESUME` / loop de `tick`) passam a invocar `vm.repo.reconcileFocusTimer()` quando `remaining == 0L && focus.autoAdvance && focus.startedAt > 0L`.
  3. Em `recordWorkFocusIfNeeded`, a data de registro do hábito/sessão usa `f.date` (data de início da fase de trabalho), preservando a integridade mesmo quando uma sessão atravessa a meia-noite, e `Focus.seconds()` prioriza o delta monotônico (`elapsedRealtime`) protegendo contra alterações manuais do relógio do sistema.
- **Teste:** `p0_pomodoroAutoAdvanceLifecycleConcurrencyAndClockAnomaly` em `Stability1751AuditTest.kt` (cobrindo `autoAdvance = true`, `autoAdvance = false`, fim do 1º ciclo, fim do último ciclo, pausa manual, encerramento antecipado, retorno do segundo plano, duas transições concorrentes, relógio alterado e registro único de foco).
- **Resultado:** Confirmado e corrigido.

---

## 2. P1 — Recorrências Operacionais da Agenda 3.0

- **Hipótese:** `OdinPlanningEngine.expandRecurrenceOccurrences()` existe e é testado isoladamente, mas suas ocorrências não são projetadas em `agendaDay()`, `monthMarker()`, `busyIntervalsForDate()`, `freeIntervalsForDate()`, `simulateCapacity()` nem nas visualizações da Agenda.
- **Reprodução:** Criar um `TimeBlock` ou `AgendaEvent` em `2026-06-01` (`09:00`–`10:00`) e salvar uma `RecurrenceRule` ativa (`pattern = "daily"`, `startDate = "2026-06-01"`). Consultar `agendaDay(s, "2026-06-02")`, `monthMarker(s, "2026-06-02")` e `OdinPlanningEngine.busyIntervalsForDate(s, "2026-06-02")`. Todos retornavam vazio para `2026-06-02`, permitindo que a Mesa de Odin agendasse sugestões sobrepostas ao bloco/compromisso recorrente.
- **Causa:**
  1. `busyIntervalsForDate()`, `agendaDay()`, `monthMarker()` e `simulateCapacity()` consultavam apenas `s.timeBlocks.filter { it.date == date }` e `s.agendaEvents.filter { it.date == date }`, ignorando `s.recurrenceRules`.
  2. O limite de iteração `guard < 400` em `expandRecurrenceOccurrences` limitava séries longas (> 13 meses) e `rule.startDate` não protegia contra edições posteriores da série que não deveriam alterar o passado antes do início da regra.
- **Correção:**
  1. Criado mecanismo compartilhado de projeção virtual sob demanda em `OdinPlanningEngine`:
     - `effectiveBlocksForDate(s: GameState, date: String): List<TimeBlock>`
     - `effectiveEventsForDate(s: GameState, date: String): List<AgendaEvent>`
     - Para cada `RecurrenceRule` ativa, localiza o template (`TimeBlock` ou `AgendaEvent` por `templateId` ou `recurrenceId`), verifica se `date` pertence a `expandRecurrenceOccurrences(rule, date, date)` e, caso não exista uma instância materializada na mesma data para a mesma regra/template (evitando duplicação na data de origem ou em datas já materializadas), projeta uma ocorrência virtual determinística (`id = "${template.id}@rec:$date"`, `date = date`, `recurrenceId = rule.id`).
  2. `excludeRecurrenceDate(ruleId, date)` e `saveRecurrenceRule` preservam o histórico: quando uma regra é editada com nova `startDate` ou encerrada, ocorrências anteriores permanecem inalteradas e exclusões individuais (`excludedDates`) suprimem apenas a data excluída.
  3. Integrado `effectiveBlocksForDate` e `effectiveEventsForDate` em:
     - `OdinPlanningEngine.busyIntervalsForDate()` e `freeIntervalsForDate()`
     - `OdinPlanningEngine.simulateCapacity()`
     - `agendaDay()` e `monthMarker()` em `ProjectsAndAgenda.kt`
     - Tratamento seguro em `AgendaScreens.kt` e `Repository.kt` para ações sobre ocorrências virtuais recorrentes (ex.: excluir uma ocorrência virtual em `deleteTimeBlock` / `deleteEvent` adiciona a data a `excludedDates` da regra sem apagar o template histórico; fixar/editar uma ocorrência virtual materializa o bloco naquela data).
  4. Ampliado o guarda de `expandRecurrenceOccurrences` para suportar horizontes plurianuais (ex.: anos bissextos e 3.660 dias) mantendo execução instantânea.
- **Teste:** `p1_recurrenceRulesProjectedAcrossAgendaAndOdinPlanning` em `Stability1751AuditTest.kt` (cobrindo diária, semanal, dias selecionados, mensal, exclusão de ocorrência, fevereiro, ano bissexto `2028-02-29`, limites de mês `31 -> 28/29/30`, data de término, ocorrência bloqueando sugestão de Odin e mudança da série sem alteração retroativa).
- **Resultado:** Confirmado e corrigido.

---

## 3. P1 — Replanejamento e Conflitos (`applyReplanChoice` & Fallback Fixo)

- **Hipótese:** `Repository.applyReplanChoice()` persiste transferências (`TRANSFER`), divisões (`SPLIT`) e adiamentos (`POSTPONE`) sem revalidar disponibilidade autorizada, compromissos fixos, recorrências, blocos existentes, dias de descanso, limite diário ou estado da tarefa; além disso, `generateReplanProposal()` cria um fallback artificial às `09:00` quando não há janela livre na próxima semana.
- **Reprodução:**
  1. Em `OdinPlanningEngine.kt` (linhas 1315–1317), quando `nextWeekSlot == null`, `POSTPONE` preenchia `targetDate = base.plusDays(7)`, `startTime = "09:00"`, `endTime = formatMinutesToTime(9 * 60 + dur)` mesmo sem disponibilidade configurada ou com compromisso às 09:00.
  2. Em `Repository.kt` (linhas 1180–1227), `applyReplanChoice` validava apenas `validIsoDate` e `validTimeInterval`, gravando o bloco mesmo que um compromisso fixo ou bloco fixado tivesse sido criado naquele horário após a geração da sugestão.
- **Causa:**
  1. Falta de validação centralizada compartilhada entre `applyOdinProposal` e `applyReplanChoice`.
  2. Fallback estático `"09:00"` em `generateReplanProposal` para `POSTPONE`.
- **Correção:**
  1. Removido o fallback arbitrário `"09:00"` de `ReplanActionKind.POSTPONE` em `OdinPlanningEngine.generateReplanProposal()`: `POSTPONE` com horário de bloco só é oferecido quando existe janela livre real (`nextWeekSlot != null`) respeitando disponibilidade autorizada, buffers, limite diário e pré-requisitos da tarefa. Quando não há janela livre nos próximos 7–14 dias, o sistema não inventa horário às 09h (mantendo as opções seguras `SEND_TO_PENDING` e `REMOVE_SCHEDULE` e informando a indisponibilidade).
  2. Extraída função centralizada de validação de agendamento no domínio/repositório (`validateScheduledSlot(...)`), reutilizada tanto por `applyOdinProposal` quanto por `applyReplanChoice` (`TRANSFER`, `POSTPONE`, `SPLIT`):
     - Revalida janela de disponibilidade autorizada (`authorizedWindowsForDate`, que já bloqueia `restDates` e `AvailabilityException.blocked`).
     - Revalida ausência de conflito com `busyIntervalsForDate` (incluindo compromissos fixos, blocos existentes/fixados e ocorrências recorrentes, ignorando apenas o `oldBlock.id` que está sendo movido/dividido).
     - Revalida ausência de sobreposição entre as duas partes de um `SPLIT` e o respeito ao intervalo mínimo (`bufferMinutes`) e limite diário (`dailyLimitMinutes`).
     - Revalida o estado atual da tarefa (não concluída, não arquivada, caminho ativo) e suas dependências finish-to-start não liberadas.
  3. Toda a execução de `applyReplanChoice` roda dentro de `change(...)` (`db.withTransaction`), garantindo rollback atômico sem persistência parcial se qualquer verificação falhar.
- **Teste:** `p1_replanValidatesConflictsRejectsStaleOptionsAndNeverFallsBackTo9am` em `Stability1751AuditTest.kt` (cobrindo transferência, divisão e adiamento com conflito posterior, rollback atômico e ausência de fallback às 09:00 sem janela livre).
- **Resultado:** Confirmado e corrigido.

---

## 4. P1 — Dependências Liberadas por Tarefas Parcialmente Agendadas e Seleção Parcial

- **Hipótese:**
  1. Em `generateOdinProposal()`, `allocateCandidate()` registra `scheduledCompletion[cand.taskId] = d to en` para cada fatia alocada, mesmo quando `remaining > 0` (tarefa parcialmente agendada). Com isso, tarefas dependentes (`B` que depende de `A`) enxergam `scheduledCompletion[A] != null` e são agendadas como se `A` tivesse sido integralmente concluída.
  2. Em `applyOdinProposal()`, quando o usuário desmarca sugestões de `A` (aplicação parcial) mantendo `B` marcada, `applyOdinProposal` persiste `B` sem verificar que sua dependência `A` deixou de estar integralmente agendada.
- **Reprodução:**
  1. Configurar apenas `60 min` livres na segunda e `60 min` na terça. Criar tarefa `A` (`120 min`) e tarefa `B` (`30 min`, `B` depende de `A`), com limite ou janela em que `A` só consegue alocar `60 min` (parcial). Na versão 1.7.5, `allocateCandidate` gravava `scheduledCompletion["A"]` após os primeiros `60 min`, permitindo que `B` fosse agendada embora `A` ainda tivesse `60 min` não alocados.
  2. Gerar proposta válida com `A` (`60 min`) e `B` (`60 min`, `B <- A`). Chamar `repo.applyOdinProposal(proposal, selectedIds = setOf(suggestionB.id))`. Na versão 1.7.5, `B` era aplicada mesmo com `A` desmarcada.
- **Causa:**
  1. `scheduledCompletion[cand.taskId]` era atualizado dentro do loop de fatias antes de confirmar `remaining == 0`, e a inicialização de `scheduledCompletion` a partir de `s.timeBlocks` existentes também não verificava se a soma dos blocos existentes cobria toda a estimativa da tarefa (`needed <= 0`).
  2. `applyOdinProposal` não validava a integridade das dependências finish-to-start do subconjunto `selectedIds`.
- **Correção:**
  1. Diferenciação explícita dos 4 estados de agendamento de tarefa em `OdinPlanningEngine`:
     - `COMPLETED` (`task.completed`)
     - `FULLY_SCHEDULED` (soma dos blocos existentes + fatias planejadas `>= estimateMinutes`, ou seja, `remaining == 0`)
     - `PARTIALLY_SCHEDULED` (possui blocos/fatias, mas `remaining > 0`)
     - `UNSCHEDULED` (nenhum bloco/fatia alocado)
  2. `scheduledCompletion[taskId]` só é preenchido quando a tarefa pré-requisito está **integralmente agendada** (`remaining == 0` ao final de `allocateCandidate`, ou `needed <= 0` pelos blocos existentes).
  3. Quando um pré-requisito fica `PARTIALLY_SCHEDULED` ou `UNSCHEDULED`, todas as tarefas que dependem dele permanecem bloqueadas, entram em `unscheduled` com motivo explícito (`"Bloqueada por pré-requisito parcialmente agendado: ..."` ou `"Bloqueada por pré-requisito não agendado: ..."`) e nenhuma data de conclusão fictícia é atribuída.
  4. Em `Repository.applyOdinProposal()`, adicionada validação de dependências sobre o conjunto selecionado (`chosen`): para cada sugestão de tarefa selecionada, todos os seus pré-requisitos ativos (`blockingPrerequisites`) devem estar concluídos ou integralmente cobertos (por blocos já existentes no banco + sugestões selecionadas em `chosen`), e qualquer bloco de pré-requisito deve terminar antes do início do bloco da tarefa dependente (`finish-to-start`). Caso o usuário desmarque parte ou a totalidade de um pré-requisito, `applyOdinProposal` rejeita a aplicação parcial inválida com mensagem clara e rollback transacional.
- **Teste:** `p1_dependenciesRequireFullSchedulingAndRejectInvalidPartialSelection` em `Stability1751AuditTest.kt` (cobrindo `A -> B`, `A -> B -> C`, `A e B -> C`, pré-requisito parcialmente agendado, ausência de capacidade, seleção parcial desmarcando pré-requisito, tarefas concluídas e blocos existentes).
- **Resultado:** Confirmado e corrigido.

---

## 5. P1 — Desfazimento Seguro (`undoAppliedPlan`)

- **Hipótese:** `Repository.undoAppliedPlan()` preserva blocos com `modifiedManually || pinned`, mas ao restaurar `taskSnapshotsJson` (`scheduledDate` e `scheduledTime` da `PathTask`), sobrescreve edições manuais feitas diretamente na tarefa após a aplicação do plano (ou restaura snapshots antigos sobre alterações feitas por um segundo plano aplicado posteriormente).
- **Reprodução:**
  1. Criar tarefa `T1` com `scheduledDate = ""`.
  2. Aplicar plano da Mesa de Odin (que agenda `T1` em `2026-06-01 09:00` e grava snapshot `["", ""]`).
  3. Editar manualmente a tarefa `T1` via `repo.saveTask(t1.copy(scheduledDate = "2026-06-05", scheduledTime = "14:00"))` (sem tocar no `TimeBlock`).
  4. Chamar `repo.undoAppliedPlan(planId)`.
  5. Na versão 1.7.5, `undoAppliedPlan` verificava apenas `tid !in preservedTaskIds` (baseado nos blocos) e restaurava `T1.scheduledDate = ""`, apagando a edição manual `"2026-06-05"`.
- **Causa:** `taskSnapshotsJson` armazenava apenas `[prevDate, prevTime]`, sem registrar `[appliedDate, appliedTime]` produzidos pelo plano. Assim, `undoAppliedPlan` não comparava o estado atual da tarefa com o estado que o plano havia aplicado.
- **Correção:**
  1. Em `applyOdinProposal()`, `taskSnapshotsJson` passa a armazenar `[prevDate, prevTime, appliedDate, appliedTime]` (mantendo retrocompatibilidade de leitura com registros legados de 2 elementos, nos quais o estado aplicado é inferido a partir do primeiro bloco criado pelo plano para aquela tarefa).
  2. Em `undoAppliedPlan()`, a restauração de `scheduledDate` e `scheduledTime` de cada `PathTask` é **condicional**:
     - Só restaura `scheduledDate` / `scheduledTime` para `prevDate` / `prevTime` se:
       - A tarefa não foi concluída (`!t.completed`) nem arquivada (`!t.archived`);
       - Nenhum bloco daquela tarefa neste plano foi fixado ou modificado manualmente (`tid !in preservedTaskIds`);
       - O estado atual da tarefa (`t.scheduledDate`, `t.scheduledTime`) **ainda corresponde exatamente ao estado aplicado pelo plano** (`appliedDate`, `appliedTime`);
       - Se outro plano ativo posterior ainda possui blocos para essa tarefa, a data da tarefa é sincronizada com o bloco remanescente ou preservada se foi editada manualmente.
  3. Repetição de `undoAppliedPlan` no mesmo `planId` é rejeitada explicitamente (`require(!record.undone)`) sem efeitos colaterais.
- **Teste:** `p1_undoAppliedPlanPreservesManualTaskEditsAndHandlesSequentialPlans` em `Stability1751AuditTest.kt` (cobrindo edição manual de data/horário da tarefa após o plano, tarefa concluída, bloco fixado/modificado/excluído, dois planos sucessivos e idempotência/rejeição de segundo undo).
- **Resultado:** Confirmado e corrigido.

---

## 6. P1 — Horários Passados no Dia Corrente e Referência Temporal Injetável

- **Hipótese:** Quando `fromDate` é o dia atual (`todayDate`), `OdinPlanningEngine.generateOdinProposal()` e `generateReplanProposal()` consideram todas as janelas autorizadas do dia desde `00:00`, oferecendo sugestões em horários que já passaram (ex.: sugerir `08:00–09:00` quando o plano é gerado às `18:00`).
- **Reprodução:** Configurar disponibilidade das `08:00` às `20:00` no dia de hoje. Gerar proposta às `18:00`. O motor sugeria blocos às `08:00` da manhã do próprio dia.
- **Causa:** `generateOdinProposal` e `generateReplanProposal` não recebiam uma referência temporal (`referenceDate: String`, `referenceMinuteOfDay: Int` / `Clock`) para truncar intervalos anteriores ao instante corrente na data de referência.
- **Correção:**
  1. Introduzida referência temporal injetável e determinística (`PlanningClock` / parâmetros `referenceDate: String = today()`, `referenceMinuteOfDay: Int = currentMinuteOfDay()` com `java.time.Clock` opcional):
     - Evita consultas dispersas ao relógio do sistema durante a mesma operação.
     - Quando uma data `d == referenceDate` (o plano ou replanejamento inclui o dia corrente), os intervalos livres de `d` são truncados para `[max(iv.start, referenceMinuteOfDay), iv.end)`.
     - Se `fromDate < referenceDate` (simulação/consulta histórica explícita solicitada para datas passadas) ou `d > referenceDate` (datas futuras), os intervalos daquelas datas não sofrem truncamento pelo horário do dia corrente.
     - Em `23:59` (`referenceMinuteOfDay = 1439`), restando `1 min` (< `minSlice`), nenhum bloco inválido é sugerido no dia corrente, passando limpa e deterministicamente para o dia seguinte.
- **Teste:** `p1_pastHoursFilteredOnCurrentDayWithInjectableReferenceClock` em `Stability1751AuditTest.kt` (cobrindo geração às `09:00`, `18:00`, `23:59`, virada do dia, fuso horário via `Clock.fixed`, data futura e consulta histórica explícita).
- **Resultado:** Confirmado e corrigido.

---

## 7. Verificações Complementares (Seção 12)

Foram inspecionados e validados os 12 cenários complementares:
1. **Proposta aplicada duas vezes:** rejeitada atomicamente por conflito de ocupação com os blocos criados na 1ª aplicação.
2. **Concorrência entre duas aplicações:** `GameRepository.lock` (`Mutex`) serializa todas as chamadas `change(...)` + `db.withTransaction`; a 2ª aplicação concorrente lê o estado já atualizado pela 1ª e falha com rollback limpo.
3. **Alteração de disponibilidade após geração:** `applyOdinProposal` e `applyReplanChoice` revalidam `authorizedWindowsForDate` no momento da aplicação e rejeitam slots que deixaram de ser autorizados.
4. **Tarefa excluída após geração:** `applyOdinProposal` e `applyReplanChoice` verificam a existência e o estado ativo da tarefa no banco; se a tarefa de uma sugestão de tarefa foi excluída ou concluída após a geração, a aplicação rejeita o agendamento órfão.
5. **Caminho arquivado após geração:** `applyOdinProposal` e `applyReplanChoice` rejeitam agendamentos vinculados a caminhos arquivados/concluídos/abandonados (`isPathInactiveForPending(path.state)`).
6. **Bloco fixado após geração:** `busyIntervalsForDate` inclui blocos fixados e `applyOdinProposal`/`applyReplanChoice` rejeitam sobreposição.
7. **Recorrência excluída/adicionada após geração:** `busyIntervalsForDate` projeta ocorrências recorrentes em tempo real no momento da aplicação.
8. **Desfazimento de planejamento antigo:** `undoAppliedPlan` verifica o estado aplicado vs estado atual, preservando qualquer evolução posterior.
9. **Replanejamento depois de edição manual:** `generateReplanProposal` e `applyReplanChoice` respeitam o horário atual do bloco e revalidam conflitos.
10. **Sessão de foco atravessando meia-noite:** `recordWorkFocusIfNeeded` registra no `f.date` de início da fase de trabalho e `timeBlockFocus` particiona sessões que cruzam meia-noite (`firstDay..lastDay`).
11. **Registro de foco durante mudança do relógio:** `Focus.seconds()` usa delta monotônico (`elapsedRealtime`) e sinaliza `clockAnomaly = true` quando `|wallSec - monoSec| > 120s` ou `now < startedAt - 5000ms`.
12. **Registros de foco vinculados a projetos:** `pathFocusMinutes` e `taskFocusMinutes` somam exclusivamente sessões com `registered = true` vinculadas ao `pathId`/`taskId`, sem inventar atribuição retroativa.

# Diário de Desenvolvimento — ASCEND 1.7.5.1 (O Conselho Restaurado)

Branch: `feat/ascend-1.7.5.1` (a partir de `feat/ascend-1.7.5` @ `0b07076`, preservando `feat/ascend-1.7.5` e `main`).
Versionamento: `versionName = "1.7.5.1"`, `versionCode = 28`, Room v11 (inalterado), Backup v13 (inalterado), sem permissão `INTERNET`.

## Etapa I — Auditoria Independente e Estabilização Técnica (`1ee9e24`)

Conduzida sem alterações visuais ou de esquema, cobrindo os 6 riscos prioritários documentados em [docs/AUDITORIA-1.7.5.1.md](AUDITORIA-1.7.5.1.md) e [docs/ESTABILIDADE-1.7.5.1.md](ESTABILIDADE-1.7.5.1.md):
- **P0 — Reconciliação de fase da Forja do Tempo:** `Focus.isPhaseCompleted()` + `GameRepository.reconcileFocusTimer()` protegido por `Mutex`, disparado em `ON_RESUME` e em `TimeForgeScreen`, encerrando ciclos vencidos em segundo plano sem duplicar sessões e sem inventar múltiplos ciclos não supervisionados.
- **P1 — Projeção real de recorrências (`@rec:YYYY-MM-DD`):** `effectiveBlocksForDate()` e `effectiveEventsForDate()` integrados em `agendaDay`, `monthMarker`, `busyIntervalsForDate`, `freeIntervalsForDate`, `simulateCapacity`, `generateOdinProposal` e `generateReplanProposal`, com exclusão de ocorrência única via `excludeRecurrenceDate` em `Repository.kt`.
- **P1 — Validação centralizada de agendamento:** `validateScheduledSlot()` no motor + remoção do fallback silencioso de `09:00` no replanejamento (`POSTPONE`).
- **P1 — Dependências no agendamento completo e parcial:** `TaskScheduleState` (4 estados) com gating estrito por pré-requisitos e validação de seleção parcial em `applyOdinProposal`.
- **P1 — Desfazer condicional de planos aplicados:** `undoAppliedPlan` preserva edições manuais de `PathTask` usando snapshots `[prevDate, prevTime, appliedDate, appliedTime]`.
- **P1 — Relógio injetável (`PlanningClock`):** filtra horários já passados no dia atual sem depender do relógio global em testes.
- **Regressão:** `Stability1751AuditTest.kt` (`8` novos testes → `226` testes aprovados ao término da Etapa I).

## Etapa II — Refinamento Visual Nórdico & Distribuição Oficial

Conduzida estritamente sobre `1ee9e24`, preservando 100% das correções da Etapa I:
1. **Inspeção visual real da 1.7.5:** auditadas 13 capturas de `docs/screenshots/1.7.5/`, mapeando botões ruidosos, hierarquia plana, marcadores diminutos, truncamento no Gantt e cartões pouco diferenciados no Kanban e Forja do Tempo.
2. **Geração e limpeza pictórica (`83df5ac`):** gerados `6/8` recursos originais (`2` de reserva preservados), limpos em RGBA com remoção de resíduos internos, otimizados em WebP (`374.380 bytes` / `365,6 KB` no APK + prancha conceitual fora do APK) e catalogados em [docs/art/README.md](art/README.md).
3. **Design System 3.2.1 (`2bde75a`):** atualização de `SagaButton` (`NorthComponents.kt`) e `NordicCouncilDesign.kt` com 6 famílias metálicas, 8 `PlanningMark`, 8 `StrategyFrameAsset`, `NordicStrategyDivider`, `NordicSectionHeader`, `NordicCompactAction`, `TimeForgeRefinedPiece` e runas contextuais *Elder Futhark*.
4. **Refinamento de telas e inspeção iterativa (`eacedb3`):** aplicadas melhorias em `OdinCouncilScreens.kt`, `Paths30Screens.kt`, `TimeForgeScreen.kt`, `AgendaScreens.kt` e `CouncilOfNineScreen.kt`; criada `VisualRefinement1751UiTest.kt` gerando 14 capturas comparativas em `docs/screenshots/1.7.5.1/`; após inspeção das 14 imagens, ajustados o CTA cerimonial em largura total no Centro de Comando, o alinhamento 2×2 na Forja do Tempo (`Confirmacao` em *Registrar foco*) e a eliminação de reticências em barras Gantt de 1 dia.
5. **CI, assinatura oficial e documentação:** workflow `.github/workflows/android.yml` atualizado para `feat/ascend-1.7.5.1`, executando testes, lint, compilação, assinatura com `ASCEND_KEYSTORE_BASE64` (`e1f003ef...`) e publicação do APK oficial em `downloads/ASCEND-1.7.5.1.apk`.

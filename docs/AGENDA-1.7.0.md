# ASCEND 1.7.0 — Agenda Profissional 2.0, Entrada, Pendências e Rumos do Dia

Documentação técnica e funcional da evolução de produtividade da versão `1.7.0` (`versionCode 20`), implementada em [`ProjectsAndAgenda.kt`](../app/src/main/java/br/com/ascend/aetheris/ProjectsAndAgenda.kt), [`AgendaScreens.kt`](../app/src/main/java/br/com/ascend/aetheris/AgendaScreens.kt), [`JourneyHome.kt`](../app/src/main/java/br/com/ascend/aetheris/JourneyHome.kt) e [`Repository.kt`](../app/src/main/java/br/com/ascend/aetheris/Repository.kt).

---

## 1. Agenda Integrada (`PlannerScreen`: Dia / Semana / Mês)

O Planejador Semanal da `1.6.x` evoluiu para uma visão unificada preservando todas as regras de `PlanException` e a consistência com widgets, análises e lembretes:

- **Três modos de visualização:**
  - **Dia:** resumo do dia selecionado com compromissos, blocos de tempo, tarefas agendadas/com prazo, prazos de Caminhos e hábitos programados (com alternância entre grade/lista e ações rápidas).
  - **Semana:** seletor contínuo dos 7 dias da semana com resumo rápido de hábitos, tarefas, blocos e eventos do dia selecionado e navegação por semana.
  - **Mês:** grade de calendário mensal real (`monthMarker`) indicando descanso, hábitos previstos/concluídos, tarefas, blocos de tempo, compromissos e prazos de projeto em cada dia.
- **Blocos de Tempo (`TimeBlock`):**
  - Armazenam título, data, `startTime`, `endTime`, categoria, cor, descrição e vínculo opcional a `pathId`, `taskId` ou `habitId`.
  - **Detecção de sobreposição (`timeBlockOverlaps`):** intervalos concorrentes na mesma data recebem alerta visual explícito no dia.
  - **Planejado × Real (`timeBlockMinutes` vs `timeBlockActualMinutes`):** compara minutos planejados com minutos efetivos registrados no `FocusSession` para a mesma data e alvo vinculado, sem inventar tempo.
- **Compromissos (`AgendaEvent`):**
  - Suportam tipos em `eventKinds` (`Compromisso`, `Aula`, `Consulta`, `Reunião`, `Entrega`), horário inicial/final, conclusão e arquivo.
  - Compromissos são registros de organização pessoal e **não concedem XP, moedas ou atributos**.

---

## 2. Caixa de Entrada (`InboxScreen` / `InboxItem`)

- Captura rápida de pensamentos e tarefas futuras sem burocracia (`título`, `notas`, data de criação).
- **Triagem atômica (`triageInboxToTask`, `triageInboxToEvent`):** dentro de `db.withTransaction`, cria o destino (`PathTask` ou `AgendaEvent`) e arquiva o `InboxItem` original de forma indivisível. Em caso de erro de escrita, a transação sofre rollback integral sem perder o item da caixa de entrada.

---

## 3. Central de Pendências (`PendingCenterScreen` / `pendingBuckets`)

Reúne tarefas em **6 recortes distintos**:
1. **Hoje** (`todayTasks`): tarefas em aberto com `scheduledDate == hoje` ou `deadline == hoje`.
2. **Próximas** (`upcomingTasks`): tarefas em aberto futuras.
3. **Atrasadas** (`overdueTasks`): tarefas não concluídas com data anterior a hoje.
4. **Sem data** (`undatedTasks`): tarefas em aberto sem agendamento nem prazo.
5. **Alta prioridade** (`highPriorityTasks`): tarefas em aberto com prioridade `Alta` (`2`) ou `Essencial` (`3`).
6. **Concluídas** (`completedTasks`): tarefas concluídas recentemente (com histórico visível e reabertura possível).

**Regra de isolamento:** tarefas arquivadas ou pertencentes a Caminhos **Arquivados** (`state == 4`) ou **Cancelados** (`state == 5`) são automaticamente excluídas das pendências operacionais (`isTaskActivePending`).

---

## 4. Integração na Jornada ("Rumos do Dia" e Atalhos)

- **`ShortcutStrip` ampliada:** além de `Planejador`, `Análises` e `Foco`, exibe acesso direto para **`Caminhos`**, **`Entrada`** e **`Pendências`**, reorganizando-se em múltiplas linhas responsivas sem truncamento em `320 dp` / fonte `130%`.
- **`RumosDoDiaPanel` (`JourneyHome.kt`):** cartão expansível discreto na tela `Jornada` com o próximo compromisso, próximo bloco de tempo, até 3 tarefas prioritárias (com conclusão direta em 1 toque e botão de foco) e Caminhos em andamento/prazos próximos.

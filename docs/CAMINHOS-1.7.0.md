# ASCEND 1.7.0 — Os Nove Caminhos (`Caminhos` e Projetos Pessoais)

Documento técnico e funcional do módulo **Caminhos** introduzido na versão `1.7.0` (`versionCode 20`), implementado em [`ProjectsAndAgenda.kt`](../app/src/main/java/br/com/ascend/aetheris/ProjectsAndAgenda.kt), [`CaminhosScreens.kt`](../app/src/main/java/br/com/ascend/aetheris/CaminhosScreens.kt), [`Repository.kt`](../app/src/main/java/br/com/ascend/aetheris/Repository.kt) e [`Database.kt`](../app/src/main/java/br/com/ascend/aetheris/Database.kt).

---

## 1. Modelo de Dados e Persistência (Room v7)

Todos os registros são armazenados localmente em SQLite (`Room v7`, migração explícita `MIGRATION_6_7`, esquema [`7.json`](../app/schemas/br.com.ascend.aetheris.AppDatabase/7.json)) e incluídos no backup JSON `v9` transacional com checksum SHA-256:

| Entidade (`@Entity`) | Tabela Room | Finalidade |
|---|---|---|
| `PersonalPath` | `paths` | Projeto pessoal com nome, descrição, categoria, ícone, cor, data de início, prazo (`deadline`), prioridade (`0..3`), estado (`0..5`), atributo opcional, critérios de conclusão, notas, `completedDate` e `position`. |
| `PathStage` | `path_stages` | Etapa/marco opcional dentro de um Caminho (`pathId` com `ForeignKey.RESTRICT`), meta (`targetNote`) e `position`. |
| `PathTask` | `path_tasks` | Tarefa prática vinculada a um Caminho (`pathId`) e/ou Etapa (`stageId`), ou tarefa solta (`pathId = ""`), com prioridade, prazo, agendamento (`scheduledDate` / `scheduledTime`), estimativa em minutos, `completed`, `completedDate`, `archived` e `position`. |
| `PathSubtask` | `path_subtasks` | Subtarefa/checklist de uma `PathTask` (`taskId` com `ForeignKey.RESTRICT`), estado `done` e `position` reordenável. |
| `PathHabitLink` | `path_habit_links` | Vínculo N:N entre `PersonalPath` e `Habit` existentes (chave determinística `pathId:habitId`). |

### Estados do Caminho (`pathStates`)

- `0` — **Planejado**
- `1` — **Em andamento**
- `2` — **Pausado**
- `3` — **Concluído** (registra `completedDate` e gera memória real em `Salão das Memórias`)
- `4` — **Arquivado** (retira tarefas pendentes da Central de Pendências e dos Rumos do Dia)
- `5` — **Cancelado** (idem acima)

### Prioridades (`priorityLabels`)

- `0` — Baixa · `1` — Média · `2` — Alta · `3` — Essencial

---

## 2. Regras de Progresso, Operações e Preservação de Economia

- **Progresso real (`pathProgress` / `stageProgress`):** derivado exclusivamente de tarefas não arquivadas (`!archived`). Nenhuma porcentagem ou conclusão é inventada.
- **Gestão completa na interface (`PathsScreen`, `PathDetailScreen`):**
  - Busca textual, filtros por estado (`Ativos`, `Todos`, ou estado específico), prioridade e categoria.
  - Criação/edição completa (`PathEditorDialog` / `TaskEditorDialog`) com confirmação explícita antes de excluir caminhos, etapas ou tarefas.
  - Subtarefas com adição, marcação, **reordenação (subir/descer)** com atualização atômica de `position` e remoção.
  - **Duplicação de Caminho (`duplicatePath`):** clona o caminho, opcionalmente copia estrutura de etapas/tarefas/subtarefas (com tarefas reiniciadas como não concluídas e novo `id`) e **nunca** copia histórico de sessões de foco nem memórias.
  - **Mover tarefa entre caminhos/etapas (`movePathTask`):** transfere `pathId` e `stageId` preservando subtarefas e sessões vinculadas à tarefa.
- **Vínculo de Hábitos sem duplicação de XP (`linkHabitToPath`):**
  - O detalhe do Caminho exibe os hábitos vinculados com seu progresso diário real e botão de acesso à rotina.
  - Concluir um hábito vinculado utiliza exatamente o fluxo padrão de recompensa (`habit:<id>@<data>` no ledger). O vínculo de projeto é organizacional e **não concede XP, moedas ou atributos adicionais**.
- **Foco em Caminho ou Tarefa (`Focus`, `FocusSession`):**
  - Pela tela do Caminho, detalhe da tarefa ou Planejador, o usuário pode iniciar uma sessão de foco vinculada a `pathId`/`taskId` (`targetLabel`).
  - Ao encerrar, a sessão real é gravada em `focus_sessions` (`habitId = ""`, `xp = 0`, `coins = 0`) e contabilizada em `pathFocusMinutes`, `taskFocusMinutes`, `timeBlockActualMinutes` e no `Histórico de Foco`, **sem inventar XP ou moedas**.
- **Integração com Salão das Memórias e Crônica (`MemoryHall` / `buildHallMemories`):**
  - Caminhos concluídos (`state == 3`) geram entrada própria na categoria **`Caminhos`** (filtro dedicado em `Mundo → Salão das Memórias`), exibindo título, data real de conclusão e categoria do projeto.

# Caminhos 3.0 — Gerenciamento Avançado de Projetos (ASCEND 1.7.5)

O módulo **Caminhos 3.0** (`Paths30Screens.kt`, `OdinPlanningEngine.kt`, `CaminhosScreens.kt`) amplia os projetos pessoais sem duplicar entidades: Gantt, Kanban, Caminho Crítico e Dependências operam sobre a **mesma entidade `PathTask`**.

## 1. Dependências e Estados de Tarefa

- **Relação término → início (`TaskDependency`):** usa IDs estáveis (`taskId<-dependsOnTaskId`), impede autorreferência (`A -> A`) e rejeita ciclos de qualquer comprimento via detecção `O(V + E)` (algoritmo de Kahn).
- **Seis estados derivados (`TaskWorkflowState`):** *Disponível*, *Em andamento*, *Bloqueada*, *Concluída*, *Atrasada* e *Arquivada*.
- **Liberação manual auditada:** iniciar uma tarefa bloqueada exige justificativa explícita, gravando `overridden = true`, `overrideReason` e `overrideDate`.
- **Integridade referencial:** excluir uma tarefa ou movê-la para outro Caminho limpa automaticamente dependências órfãs; excluir um Caminho remove marcos, dependências e referências em objetivos pessoais.

## 2. Caminho Crítico (CPM)

`OdinPlanningEngine.computeCriticalPath` executa passos *forward* (ES/EF) e *backward* (LS/LF/folga) em ordem topológica:

- Identifica a sequência crítica (folga `0`).
- Calcula **duas datas projetadas distintas**:
  1. **Projeção por esforço:** esforço crítico dividido pelo limite diário nominal.
  2. **Projeção por disponibilidade real:** caminhando pelas janelas livres reais configuradas (até 365 dias), destacando risco quando ultrapassa `path.deadline`.
- Declara explicitamente todas as premissas (incluindo referência provisória de 30 min para tarefas sem estimativa).

## 3. Gráfico de Gantt Interativo em Compose

- Renderizado nativamente em Compose (`PathGanttPanel`) com rolagem horizontal, alternância entre **Escala diária** e **Escala semanal**, linha vertical de **Hoje**, losangos metálicos de **Marcos**, destaque dourado para **tarefas críticas** e toque em qualquer barra para inspecionar folga e janelas projetadas.

## 4. Quadro Kanban Unificado e Acessível

- Quatro colunas (*A fazer*, *Em andamento*, *Bloqueadas*, *Concluídas*) sobre a mesma `PathTask`.
- Movimentação acessível por botões explícitos (`→ A fazer`, `→ Em andamento`, `Liberar & Iniciar`, `→ Concluir`), sem depender exclusivamente de gestos de arrastar.

## 5. Marcos e 6 Modelos Reutilizáveis de Projeto

- **Marcos (`PathMilestone`):** datas-alvo marcáveis como concluídas e exibidas sobre o Gantt.
- **6 modelos reutilizáveis (`projectTemplates`):**
  1. *Preparação para prova*
  2. *Artigo científico*
  3. *Projeto profissional*
  4. *Leitura de livro*
  5. *Desenvolvimento de aplicativo*
  6. *Projeto criativo*
- Cada modelo cria etapas, tarefas, dependências e marcos editáveis sem nunca copiar histórico ou registros concluídos.

# Mesa de Odin — Planejamento Estratégico Adaptativo (ASCEND 1.7.5)

A **Mesa de Odin** (`OdinPlanningEngine.kt`, `OdinCouncilScreens.kt`) é o núcleo de planejamento estratégico pessoal da versão `1.7.5`, operando **100% offline**, de forma **determinística**, sem chamadas de IA externa e sem algoritmos de força bruta exponencial.

## 1. Princípios e Restrições

- **Disponibilidade real autorizada:** o motor nunca presume 24 horas livres. Apenas janelas explícitas em `availability_windows` (ou janelas especiais em `availability_exceptions`) são consideradas alocáveis.
- **Restrições rígidas (nunca violadas):**
  - compromissos fixos (`AgendaEvent`),
  - janelas semanais autorizadas e exceções de data,
  - dias globais de descanso (`RestDay`),
  - dependências *finish-to-start* (`TaskDependency`),
  - limite diário de carga (`PlanningConfig.dailyLimitMinutes`) e margem entre blocos (`bufferMinutes`),
  - blocos fixados (`TimeBlock.pinned = true`),
  - exclusão de tarefas concluídas/arquivadas e caminhos encerrados.
- **Preferências flexíveis:** equilíbrio entre categorias, concentração de estudo, agrupamento de atividades semelhantes, redução de troca de contexto e preferência suave baseada em histórico real de foco (`FocusSession` ≥ 5 min, ativada somente com mínimo de **5 sessões registradas**).

## 2. Seis Estratégias de Odin

As estratégias ajustam exclusivamente pesos de ordenação, tamanho máximo de fatia, margem entre blocos e taxa-alvo de carga diária — **jamais alteram XP, moedas, classes ou economia do RPG**:

1. **Guardião** (`guardian`): sustentabilidade, hábitos primeiro, margem de 15 min, carga alvo 72%.
2. **Erudito** (`scholar`): estudo profundo, leitura e revisões de Mímir em janelas de maior clareza.
3. **Berserker** (`berserker`): ataque direto a tarefas atrasadas e prazos imediatos.
4. **Estrategista** (`strategist`, padrão): equilíbrio entre caminho crítico (CPM), prazos e áreas.
5. **Explorador** (`explorer`): blocos mais curtos (até 50 min) para variedade e desbloqueio.
6. **Artífice** (`artificer`): blocos longos (até 120 min) para construção profunda e entregas criativas.

## 3. Simulador de Capacidade, Propostas, Transação e Reversão Inteligente

- **Simulador de Capacidade (`simulateCapacity`):** analisa 5 horizontes (*Hoje*, *Próximos 7 dias*, *Semana civil*, *Próximos 30 dias*, *Personalizado*), **deduplica** minutos de blocos já vinculados a tarefas para evitar dupla contagem e sinaliza explicitamente tarefas sem estimativa e prazos incompatíveis.
- **Propostas explicáveis (`generateOdinProposal`):** cada sugestão traz motivo legível em português; itens não encaixados e conflitos impossíveis são sempre exibidos em lista própria. Permite seleção parcial, fixação individual e comparação lado a lado de duas estratégias.
- **Aplicação transacional (`applyOdinProposal`):** revalida janelas, eventos fixos e sobreposições dentro de `db.withTransaction`; qualquer conflito executa rollback integral.
- **Desfazer inteligente (`undoAppliedPlan`):** remove blocos ainda intactos e restaura snapshots de agendamento de tarefas, mas **detecta e preserva** blocos editados manualmente (`modifiedManually = true`) ou fixados após a aplicação.
- **Replanejamento adaptativo (`generateReplanProposal`):** sugere 5 alternativas para itens atrasados ou blocos incompletos (*Transferir*, *Dividir*, *Adiar*, *Pendência*, *Remover*), sem autoaplicação e sem penalidades no RPG.
- **Matriz de Eisenhower (`eisenhowerQuadrant`):** classifica exclusivamente com base nos marcadores explícitos `important` e `urgent` definidos pelo usuário.

## 4. Benchmarks Reais Medidos (JVM / Robolectric)

Verificação determinística em `OdinCouncilTest.performanceBenchmarks_50_200_1000_Tasks` (validação de ciclos + CPM + proposta de 14 dias + simulação de capacidade):

| Volume de tarefas | Dependências DAG | Tempo total medido |
|---:|---:|---:|
| 50 tarefas | 40 | ~68 ms (JVM a frio) |
| 200 tarefas | 160 | ~47 ms |
| 1.000 tarefas | 800 | ~94 ms |

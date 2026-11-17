# Produtividade — ASCEND 1.6

Planejador, Análises e Histórico de Foco são acessíveis pela Jornada e pelo Livro de hábitos. Não há sexta aba principal, serviço remoto ou nova moeda.

## Planejador Semanal

A semana civil vai de segunda a domingo. Os dias são cartões selecionáveis com rolagem horizontal, evitando sete colunas pequenas. Anterior/Hoje/Próxima alteram a semana; o dia selecionado apresenta agenda ordenada pelo horário real do hábito, cumprimento, descanso e ajustes pontuais. Hábitos sem horário ficam no fim e recebem “Sem horário”; deixar horário vazio é permitido quando não há lembrete.

Em **Ajustes pontuais**, selecione um hábito e escolha:

- Programação normal: remove a exceção daquela data.
- Incluir nesta data: prevê a atividade mesmo fora de seu dia recorrente.
- Dispensar nesta data: retira a atividade prevista, sem apagar hábito ou registro.

Ajustes só são gravados de hoje em diante. O passado conserva as exceções já realizadas, sem edição retroativa. A programação semanal continua no editor de hábitos. O calendário anterior permanece disponível.

### Prioridade única da programação

`scheduled` é a regra central; `GameState.planned/due` a reutilizam com índices transitórios:

1. Antes da criação ou em descanso global: não previsto.
2. Pausa/arquivo/exclusão aplicáveis à data: não previsto. Hoje/futuro respeitam também o estado atual; o passado usa o histórico de programação.
3. Exceção daquela combinação hábito/data: inclusão ou dispensa.
4. Sem exceção: dias recorrentes do histórico do hábito.

Descanso e inatividade prevalecem sobre inclusão. Exceções ficam guardadas durante o descanso; remover o descanso volta a aplicá-las. Reativação também pode tornar uma exceção futura novamente efetiva. Ajustar um hábito inativo exige reativá-lo primeiro.

Jornada, calendário, widgets, análises, sequências e lembretes consultam a mesma regra com exceções. O modo livre continua oferecendo hábitos ativos na Jornada; o widget mantém programação estrita, agora incluindo os ajustes pontuais. Essa diferença continua explícita nas Configurações.

Planejar não concede XP nem cria registro. Atividades extras continuam permitidas. Um registro existente mantém meta/recompensas e o snapshot histórico; dispensar hoje pode mudar o denominador das métricas, mas não altera esse registro nem seu pagamento.

## Central de Análises

Períodos disponíveis: últimos 7/30/90 dias, semana civil até hoje e mês civil até hoje. A comparação usa o intervalo imediatamente anterior com a mesma quantidade de dias; não confunde mês parcial com mês anterior inteiro.

- Conclusões: registros completos no período, incluindo atividades extras.
- Cumprimento: conclusões previstas / atividades previstas × 100. Sem previsão, mostra “—”. Descansos e dispensas saem do denominador; extras ficam no total de conclusões.
- Dias previstos: quantidade de datas com pelo menos uma atividade prevista.
- XP geral: soma dos valores atuais do ledger no período, incluindo recompensas do RPG. XP individual usa o ledger do hábito e suas recompensas originais.
- Tempo registrado: soma do progresso dos registros cronometrados, em minutos. Não soma sessões de foco novamente.
- Atributos: distribuição das conclusões por atributo do registro.
- Ranking: dez maiores taxas e indicação da menor; todos os hábitos continuam acessíveis individualmente pelo botão de análises nos detalhes.
- Detalhes individuais: previsão, conclusões/taxa, sequências atual/melhor, minutos, XP, comparação e até 30 registros recentes dentro do período.

O gráfico mostra XP diário, eixo de valores, escala e datas. “Consultar valores por data” disponibiliza números reais em texto. Os insights são regras locais: maior taxa, comparação de conclusões, dia da semana com maior taxa e categoria com mais minutos. Não há IA, previsões ou causalidades inventadas. Sem dados, o texto informa isso.

Correções históricas atualizam registros/ledger e os derivados. As análises são calculadas em `Dispatchers.Default`, com estado de carregamento e descarte de resultados desatualizados. Os registros são agrupados por hábito e as exceções são indexadas; caches não entram no backup.

## Histórico de Foco

Toda sessão iniciada na 1.6 recebe UUID estável e horário original de início. O slot de foco ativo conserva ID, baseline, tempo acumulado e instante da última retomada. Pausar acumula apenas tempo ativo; retomar preserva o restante. A tela continua baseada em timestamps, sem serviço permanente ou alarme exato.

Ao encerrar, uma transação registra progresso, ledger, sessão detalhada e limpeza do slot ativo:

- Concluída: tempo previsto atingido e registro solicitado.
- Encerrada antes do tempo: registro solicitado antes da duração prevista.
- Cancelada: tempo observado guardado no histórico, sem alimentar o hábito.

O histórico não paga XP. O registro usa o baseline absoluto anterior, preserva progresso manual maior e mantém a recompensa única por hábito/dia. Encerramentos repetidos/concorrentes não duplicam sessão nem pagamento. Correções posteriores dos minutos do hábito não reescrevem o fato histórico de uma sessão realizada.

As sessões mostram hábito, data, tempo efetivo, duração prevista, estado e detalhes de início/término/ID. Há totais, média, contagem de concluídas, tempo por hábito e comparação por período. Os resumos de tempo são em minutos inteiros; detalhes mostram segundos. Totais de sessões incluem tempo observado em canceladas, enquanto as análises de hábitos usam só registros efetivamente salvos.

A data da sessão e do registro é a do início, preservando a regra anterior mesmo ao atravessar meia-noite. Horários de detalhes são apresentados no fuso atual do aparelho. Alterações manuais de relógio seguem as limitações da abordagem existente de wall clock.

Fechar a tela ou recriar a Activity não encerra o foco. Após morte de processo, o slot persistido é relido; uma sessão em execução continua pelos timestamps até o teto da duração. Isso foi verificado por reabertura do banco em arquivo, reload e recriação da Activity com sessão pausada; encerramento físico de processo permanece pendente.

Sessões iniciadas antes da 1.6 mantêm seu funcionamento, mas não ganham histórico detalhado artificial: o instante original pode não ser conhecido após pausas. Registros antigos continuam nas análises de minutos. Arquivo/exclusão lógica de hábitos preservam sessões concluídas e seus nomes guardados.

## Room v6 e backup v8

Novas tabelas:

- `plan_exceptions`: chave canônica `habitId@date`, hábito, data civil e inclusão/dispensa.
- `focus_sessions`: ID, hábito/nome guardado, data, início/término, segundos efetivos, minutos previstos, estado e indicação de registro.

O slot `focus` ganha `sessionId` e `beganAt`. `MIGRATION_5_6` é explícita, aditiva e mantém os esquemas 1–5. Valores padrão deixam sessões legadas sem ID detalhado. Não há migração destrutiva.

Backup v8 acrescenta os dois conjuntos e os campos de foco, mantendo checksum e restauração transacional. Lê v1–v7 e os novos conjuntos começam vazios quando ausentes. Os snapshots, registros, ledger, Sagas, Expedições, equipamentos, diário e memórias permanecem. Preferências do aparelho continuam fora do backup, como antes.

## Limites de validação

Não há lembrete de fim de foco em segundo plano, distribuição de minutos entre dias ou edição/exclusão individual de sessão concluída nesta entrega. A prioridade é preservar o histórico real. A lista detalhada do hábito mostra até 30 registros recentes; o histórico geral existente continua acessível.

As outras regiões não receberam Expedições. Não há economia nova, combate ou conteúdo online. Testes físicos de launcher, relógio, reboot, teclado, gestos e bateria continuam separados em [VALIDACAO.md](../VALIDACAO.md).

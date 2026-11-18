# Auditoria independente do ASCEND 1.7.0.1

Base `9365b39` (1.7.0), comparada com `448e7cd` (1.6.9.19). A implementação fica em `feat/ascend-1.7.0.1`. O escopo cobre Caminhos, Agenda, Rumos do Dia, Foco e backup, com a Biblioteca da Forja preservada.

| Gravidade | Problema confirmado e causa | Correção | Evidência |
|---|---|---|---|
| P0 | Projeto contabilizava tanto o vínculo histórico quanto o proprietário atual da tarefa | Usar apenas o projeto gravado no início. Sessões sem projeto ficam sem atribuição a projeto | `FocusAttributionTest`, movimentação e edição de tarefa ativa em `PathsAndAgendaTest` |
| P0 | Cada bloco recebia todo o foco do dia para o alvo | Cruzar o intervalo da sessão com os blocos e distribuir cada trecho a um só bloco | Blocos distintos, adjacentes, sobrepostos, sessões parciais e pausadas em `FocusAttributionTest` |
| P0 | Backup podia restaurar uma sessão ativa com o mesmo ID de uma sessão histórica, que seria sobrescrita ao registrar | Rejeitar colisão antes da importação | Backup inválido conserva o estado original |
| P1 | Rumos do Dia mostrava o primeiro registro do dia, independentemente do relógio | Selecionar por horário local e rotular atividades em andamento. Sem horário tem indicação própria | Teste de início, fim, concluído, sem horário e mudança de dia |
| P1 | Projetos pausados e concluídos ainda entravam nas pendências e conclusão ignorava tarefas abertas | Estados 0 e 1 aparecem nas pendências. Confirmar conclusão contra as tarefas abertas atuais e limpar a data ao reabrir | Testes de estados, confirmação, memória e reabertura |
| P1 | Foco aparecia após o cabeçalho pictórico e três grupos da Jornada. Aro fixo de 192 dp e metadados lado a lado somavam altura | Sessão no topo, aro de 146 dp e metadados em pilha. A textura já era desenhada dentro das dimensões medidas em `forgeMaterial` e continua recortada proporcionalmente | Renderizações e controles em 320, 360 e 411 dp |
| P1 | Excluir um alvo deixava foco ativo sem vínculos e o backup rejeitava a própria sessão. Exclusão de tarefa também podia transformar seu bloco em bloco genérico de projeto | Aceitar sessão desvinculada com rótulo histórico. Limpar somente referências removidas e evitar atribuição genérica quando a tarefa do bloco foi apagada | Exclusão de tarefa/projeto, exportação, restauração e registro sem XP adicional |
| P1 | Triagem usava upsert, o que podia sobrescrever uma tarefa ou compromisso com o mesmo ID. Alguns formulários fechavam ou navegavam antes do commit | Rejeitar destinos existentes e itens arquivados. Fechar editores e navegar após gravação bem-sucedida | Teste de colisão e triagem repetida, mais o teste anterior de falha de escrita |
| P1 | Edição de etapa podia movê-la a outro projeto sem mover as tarefas. Novos blocos podiam combinar alvos incompatíveis | Impedir reatribuição da etapa. Validar os vínculos do bloco no momento de salvá-lo, preservando a leitura dos registros anteriores | Teste de edição de etapa e testes de movimentação |
| P2 | Estimativa inválida virava zero ou era limitada silenciosamente. Checkbox repetido invertia o estado novamente | Enviar valor inválido à validação e preservar o formulário. Checkbox envia o estado solicitado | Testes de estimativa inválida e repetição da conclusão |

## Política temporal

Room v7 e backup v9 já registram início, fim, tempo efetivo, projeto e tarefa. Não armazenam localização das pausas, fuso original ou ID do bloco que iniciou a sessão. O registro de foco continua separado da comparação com a Agenda. A comparação usa os intervalos no fuso local e, se houver sobreposição, dá precedência ao bloco de tarefa ou hábito, depois ao início mais cedo e ao ID. Para cada janela atribuída, o limite comprovado desconta todo o tempo potencialmente inativo. A Agenda sinaliza quando o restante não pode ser determinado. Horários ambíguos na transição do fuso não são atribuídos. Um bloco editado usa seu intervalo atual. O botão do bloco segue iniciando foco no alvo, sujeito à mesma regra de interseção. Uma atribuição explícita ao bloco e o registro das pausas exigiriam novos campos, o que não é necessário para impedir dupla contagem.

Mover uma tarefa mantém o snapshot do projeto no foco ativo e histórico. A sessão sem projeto continua no histórico da tarefa. Ao excluir alvos, os identificadores removidos são desvinculados e a sessão mantém tempo e rótulo. Nenhuma sessão é apagada ou atribuída ao projeto atual da tarefa.

## Regressão e limites

Os três primeiros testes reproduziram na base acumulação em dois projetos, repetição em blocos e tarefas de projetos pausados/concluídos. Os três passaram após a correção. Dois cenários preexistentes recebem fixtures compatíveis com as novas regras. A confirmação de tarefas é explícita no teste de conclusão e o teste de quatro minutos posiciona a sessão dentro do bloco, mantendo as asserções anteriores.

A auditoria confirmou transações Room na importação e triagem, checksum e migração aditiva. Os esquemas exportados de v6 e v7 mantêm as mesmas definições. A comparação dos novos vínculos foi feita sem mudar o ledger e sem migrar dados para outro formato. A revisão visual não levou a um redesenho das outras telas. Instalação, TalkBack e comportamento de renderização em aparelhos físicos permanecem sem verificação. Fuso original de sessões antigas e os intervalos de pausa não podem ser reconstruídos com os dados existentes.

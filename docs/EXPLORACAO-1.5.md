# O Despertar dos Reinos — exploração 1.5

## Entrar e descobrir

Mundo → Mapa → selecione **Floresta da Persistência** → **Explorar região**. A região exige nível 3. O Atlas conserva as seis regiões, nomes, ordem e níveis 1/3/5/8/12/20. Regiões sem conteúdo próprio mantêm descrição e acesso aos recursos anteriores, com aviso explícito; não há expedições fictícias.

A Floresta oferece três locais, encontrados em sequência. Locais futuros aparecem velados, sem revelar a história. Ao entrar no local, **Abrir expedição** apresenta premissa, fases, hábitos compatíveis, objetivos, dificuldade narrativa, recompensa e efeito esperado. Escolha hábitos ativos; nenhum hábito é criado automaticamente.

| Expedição / local | Etapas | Recompensa final |
|---|---|---|
| Raízes sob a Névoa / Marco dos Retornos | 5 conclusões | 40 XP, 10 Fragmentos |
| O Santuário Esquecido / Santuário das Raízes | 2 + 2 + 2 conclusões | 60 XP, 15 Fragmentos |
| A Vigília da Floresta / Coração da Névoa | Chamado: 2; Resistência: 3; Juramento: 3 | 100 XP, 25 Fragmentos, Broche da Vigília |

Concluir a Vigília também atende à conquista **Testemunha da Floresta**, que usa os 5 Fragmentos habituais de uma conquista, e disponibiliza o título cosmético **Testemunha das Raízes**. Não há moedas novas, modificadores de XP ou compra real. O broche é exclusivo da exploração, equipável no nicho de acessório, sem bônus de combate.

## Registros reais e marcos

Qualquer hábito ativo pode ser vinculado. Nesta expansão, a métrica é **conclusões**, permitindo atividades de qualquer atributo. Quantidade e tempo contam quando atingem a meta do próprio hábito; não há obrigação universal de estudar, ler ou fazer exercícios. Um hábito só tem uma conclusão por dia, como antes.

Cada etapa grava uma fotografia dos progressos existentes, inclusive os já registrados hoje. Para uma conclusão contar, o registro precisa estar completo e não estar completo na fotografia inicial. Uma etapa nova nunca consome novamente as conclusões da etapa anterior. Cinco hábitos distintos podem cumprir o primeiro objetivo no mesmo dia; um único hábito precisará de cinco dias com novas conclusões.

A seleção é fixada ao iniciar, preservando a fotografia. Renomear, editar recompensa ou arquivar o hábito não reescreve os registros que já contribuíram. Excluir um hábito da lista preserva seu histórico, conforme a regra antiga. Para desfazer uma atividade, corrija seu registro para zero; os marcos e recompensas são recalculados.

**Confirmar marco** encerra uma etapa e abre a próxima com fotografia nova, na mesma transação. Marcos intermediários têm recompensa zero. **Resgatar expedição** concede apenas o prêmio final. Cada fase tem identificador determinístico e Claim único. Cliques concorrentes, reabrir a tela ou corrigir/restaurar progresso não criam um segundo resgate.

A aventura seguinte exige a anterior validada e nível 3. Correções que invalidem uma etapa também invalidam as condições seguintes, sem apagar registros ou tentativas. O ledger compensa a recompensa econômica, assim como nas Sagas; recuperar o progresso revalida os mesmos resgates, sem novo prêmio. Cosméticos, conquistas e memórias já obtidos permanecem, seguindo a política existente. Ausência não remove progresso; apenas correções alteram os registros.

## Guardião e transformação

O Guardião da Névoa é o desafio da terceira Expedição. Suas três fases são provações, não combate. Cada fase exige atividades novas e confirmação. Chefes semanais antigos permanecem opcionais e não foram alterados. Não há mortes, dano fictício, penalização por descanso ou perda por ausência.

A exploração regional soma **sete marcos confirmados**: 1 da trilha, 3 do santuário, 3 da Vigília. Percentual = marcos válidos / 7, em inteiros. Chegar ao nível 3 apenas revela a região; não concede exploração. O estado final, 100%, exige o resgate da última fase.

A Floresta tem três pinturas: inicial, trilha recuperada após Raízes e final após a Vigília. O santuário tem ruínas, recuperação após o primeiro marco e restauração após a terceira etapa. A segunda etapa desenvolve a narrativa mantendo a recuperação parcial. Transições de 400 ms são desativadas com redução de movimento. O Atlas indica o percentual na pedra da Floresta. Fechar/reabrir deriva os mesmos estados do banco; correções podem rever um estado.

O Salão das Memórias registra cada marco confirmado com data do resgate, região, narrativa e valores próprios. O Cronista reconhece as aventuras terminadas. Os nove capítulos, seus títulos e desbloqueios não foram modificados; o novo relato não antecipa Ione ou acontecimentos de capítulos futuros. A Crônica Semanal também reconhece os resgates entre os contratos do período.

## Arquitetura e persistência

- `Exploration.kt`: definições tipadas, IDs estáveis, fases, condições e progresso regional.
- `ExplorationScreens.kt`: região/local/aventura com listas LazyColumn, pinturas e controles nativos.
- `Repository.kt`: início e confirmação transacionais, associados ao mutex/Room/ledger existentes.
- `Sagas.kt`: cálculo compartilhado de progresso por fotografia; Sagas Pessoais permanecem separadas por `isPersonalSaga`.
- Identificadores `exp:roots:0`, `exp:sanctuary:0..2`, `exp:vigil:0..2` usam as tabelas **quests/claims existentes**. O conteúdo não é inserido automaticamente; só existe uma Quest quando a aventura/fase é iniciada.
- `validClaim` verifica a cadeia de fases e os snapshots dos Claims; o cálculo não usa as próprias recompensas como atividade.
- Room permanece em **v5**, sem alteração de tabelas ou migração desnecessária. Todas as migrações anteriores são mantidas. Backup **v7** preserva fases, Claims, ledger, item, conquista e memórias e lê v1–v7. A versão nova do envelope identifica o novo conteúdo, que versões antigas não conhecem.

Ao expandir o catálogo futuramente, preserve IDs e significado de fases publicadas. Uma aventura com outra estrutura deve receber outro ID. Não altere os termos capturados dos resgates antigos.

## Widgets

Na tela inicial do Android, abra o seletor de Widgets → ASCEND. Há **Jornada compacta** e **Próximos passos**. Ambos são redimensionáveis; o maior mostra nível/XP e até três pendências. Em alturas reduzidas, o número de ações diminui para caber no espaço.

O cabeçalho abre a Jornada. **Concluir** usa o repositório para hábitos simples programados na data local, respeitando pausas, arquivo, exclusão e descanso. PendingIntents são imutáveis, com hábito/data próprios; um toque antigo depois da meia-noite é recusado e atualiza o widget. Repetir não duplica registro ou XP. **Registrar** abre o editor de progresso para quantidade/tempo; não conclui automaticamente um hábito complexo.

A atualização é chamada após transações, incluindo correções e backup. O provider também responde a atualização do host, reinício, mudança de data/hora/fuso e agenda uma atualização aproximada para a próxima meia-noite, sem serviço contínuo ou permissão de alarme exato. O Android pode atrasar atualizações por bateria; o host tem atualização periódica de 30 minutos. A ação sempre revalida a data e programação, mesmo com a exibição atrasada.

O widget acompanha claro/escuro do **sistema**, independentemente do tema escolhido no app. Não exibe diário, Crônicas ou dados de conversas. Ele usa a programação estrita dos hábitos, mesmo com o modo livre de frequência da Jornada ativado. Alarmes locais e widgets não exigem internet.

## Limites da 1.5

Só a Floresta recebe expedições. As aventuras não têm abandono, reinício ou edição de associações após iniciar; essas funções continuam disponíveis nas Sagas Pessoais. Não há movimentação livre, combate ou novas métricas de expedição além de conclusões. Retratos alternativos ficaram fora do escopo principal; as três aparências anteriores e personalizações foram preservadas. Novas pinturas foram criadas para a expansão, não para novos personagens.

Widgets foram testados com RemoteViews, PendingIntent, receiver e host simulado no Robolectric. Não houve instalação em launcher/telefone físico, benchmark, teste físico de bateria/horário de verão ou audição dos sons. [Validação](../VALIDACAO.md).

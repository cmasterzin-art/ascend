# Sagas Pessoais — regras da 1.4

Abra Missões → Sagas → Iniciar saga. Escolha um modelo ou crie uma saga personalizada. Os seis modelos são Saga do Conhecimento, Caminhos do Norte, Livro das Eras, Forja da Disciplina, Juramento do Artífice e Vigília do Guardião. Nenhum hábito novo é criado automaticamente.

Selecione os hábitos reais, nome, descrição, meta, métrica (conclusões, minutos ou páginas), prazo opcional, XP e Fragmentos finais. As metas de tempo usam minutos: 30 horas = 1800 minutos. O seletor lista seus hábitos; escolha os compatíveis com a métrica. Só hábitos cronometrados contribuem para minutos; páginas exigem unidade contendo “página”. Conclusões contam registros que atingiram sua meta.

Ao iniciar, o app grava a data e uma fotografia dos progressos existentes. Atividades de datas anteriores e valores já registrados hoje não contam. No mesmo dia, só o acréscimo após essa fotografia contribui. Exemplo: 20 minutos registrados, saga iniciada, total corrigido para 50 → 30 minutos na saga. Cada registro continua concedendo seu XP habitual uma vez; a saga apenas o consulta.

Os marcos 25/50/75/100% são derivados dos registros e revelam narrativa, sem XP intermediário. O resgate final é explícito, transacional e único. Desfazer ou corrigir um registro recalcula progresso e a recompensa da saga; se moedas já foram gastas, vale a compensação existente. Restaurar o progresso revalida o mesmo resgate, sem criar outro. Excluir um hábito da lista preserva seus registros antigos, como nas versões anteriores; para desfazer uma atividade, corrija o registro para zero no histórico.

Hábitos e métrica ficam fixados após iniciar para preservar a fotografia. Nome, descrição, meta, prazo e recompensas podem ser editados até o resgate; após resgatar, os termos ficam preservados. Não há opção de aproveitar histórico anterior nesta versão nem abandono/exclusão de sagas. Uma saga sem prazo não expira. Com prazo, registros posteriores ficam fora da contagem; não há penalização por atraso.

As sagas usam `quests` com `period=2`, resgates em `claims` e o ledger atual. Room 4 acrescenta `sagaStart` e `sagaBaseline` às duas tabelas, sem alterar tabelas antigas ou fórmulas de XP. Backup 5 guarda os termos, fotografias e memórias narrativas, lendo versões 1–5. A leitura de backups antigos preenche os campos ausentes.

Diálogos: a Jornada e as Crônicas mostram o próximo comentário aplicável por prioridade. Saga resgatada, edifícios completos, conquistas épicas/lendárias, contrato épico, retorno já reconhecido pela conquista existente, níveis, saga iniciada e primeira conclusão. “Guardar esta memória” persiste a confirmação em um asset `memory`, incluído no backup. Até confirmar, a mensagem permanece disponível. Não se inventam eventos nem se usa IA externa.

Recompensas comuns aparecem em aviso breve; eventos importantes da mesma transação aparecem em um painel com XP, aumento real do saldo disponível e atributos. Compras/restauração exibem gasto nas informações do evento, sem tratar gasto como ganho. Correções nunca são apresentadas como uma nova recompensa.

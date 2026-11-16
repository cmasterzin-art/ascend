# Grimório dos Hábitos — 1.3.1

“Todo grande feito começa com um pequeno ritual.”

Abra **Hábitos → Explorar hábitos**. O catálogo contém **102 sugestões**, incluindo todos os 75 exemplos das cinco categorias principais e opções adicionais. Nenhum modelo é automaticamente criado como hábito ativo.

| Categoria | Sugestões |
|---|---:|
| Intelecto | 15 |
| Vigor | 15 |
| Disciplina | 16 |
| Equilíbrio | 15 |
| Criatividade | 17 |
| Desenvolvimento profissional | 7 |
| Finanças pessoais | 6 |
| Relações pessoais | 5 |
| Autocuidado cotidiano | 6 |

## Escolher e personalizar

Busque nomes funcionais, nomes narrativos ou palavras-chave; acentos e maiúsculas não impedem a busca. Os filtros cobrem categoria, dificuldade, tipo de registro, atributo, frequência semanal e tempo estimado. Ordene por nome, categoria, duração ou dificuldade.

Toque em **Adicionar** para abrir o editor preenchido. Você pode alterar nome, nome narrativo, descrição, ícone, cor, categoria (inclusive texto próprio), atributo, tipo, meta, unidade, XP, moedas, pontos, dificuldade, dias, horário e lembrete. Hábitos de tempo registram minutos e oferecem o cronômetro existente; altere o tipo para usar outra unidade. Hábitos simples têm uma conclusão por dia. Sugestões não impõem valores mínimos além da validação já existente de metas/recompensas.

Após salvar, o hábito usa o mesmo histórico, registro, edição, pausa, arquivo, calendário e recompensa dos hábitos manuais. Ele aparece na Jornada nos dias que você escolheu; também fica disponível na lista de Hábitos. O XP de registros anteriores não muda quando você edita a recompensa futura.

Ative **Exibir nomes narrativos** no Grimório ou nas Configurações. O nome funcional permanece logo abaixo nos cartões e nos detalhes. Os modelos de Equilíbrio, Relações pessoais e Autocuidado começam sem contagem de sequência; a opção é editável. Não há punição por ausência. Metas físicas são sugestões ajustáveis, não prescrições universais.

## Sugestões para sua Jornada

Escolha interesses entre as nove categorias. Uma regra local mostra os primeiros quatro modelos dessas categorias que você ainda não adicionou nem dispensou, na ordem estável do catálogo. Não usa IA, rede, histórico sensível ou serviço externo. É possível dispensar sugestões e reexibi-las depois. Nenhuma sugestão é adicionada automaticamente.

## Modelos de rotina

| Rotina | Hábitos oferecidos |
|---|---|
| Ritual da Aurora | Arrumar a cama; Planejar o dia; Alongamento; Ler por lazer (15 minutos) |
| Ritual do Conhecimento | Estudar uma disciplina (60 minutos / 60 XP); Revisar anotações; Resolver questões; Produzir resumo |
| Ritual da Disciplina | Organizar o quarto; Revisar tarefas; Resolver uma pendência; Planejar o dia seguinte |
| Ritual do Guerreiro | Caminhar; Realizar exercícios em casa; Fazer mobilidade; Fazer pausa de descanso |
| Ritual do Descanso | Escrever no diário; Organizar ambiente de descanso; Fazer uma atividade relaxante; Preparar a rotina de sono |
| Ritual do Artífice | Trabalhar em projeto pessoal; Desenhar; Registrar ideias; Revisar progresso do projeto |

Em **Modelos de rotina**, examine os quatro hábitos antes de confirmar. Desmarque os que não deseja e personalize qualquer novo item. Ao salvar, todos os selecionados são gravados em uma única transação. Hábitos já existentes são reutilizados sem mudar suas configurações. Repetir a operação acrescenta somente o que estiver faltando à mesma rotina.

A identificação de duplicatas usa ID de origem ou nome funcional normalizado. Hábitos pausados/arquivados também são considerados existentes; reative-os em Hábitos quando necessário. Uma cópia intencional continua disponível no menu **Duplicar**. Não se tenta adivinhar sinônimos ou proibir hábitos manuais semelhantes.

As rotinas não dão XP por serem adicionadas ou agrupadas. Recompensas continuam ligadas a uma conclusão/progresso por hábito e dia.

## Armazenamento

`HabitCatalog.kt` contém modelos Kotlin tipados, IDs estáveis e dados completos. Instâncias recebem UUID próprio e uma cópia dos valores; mudar o catálogo não modifica hábitos salvos. A Room v3 acrescenta três colunas com defaults explícitos, preservando sequências antigas. Backup v4 guarda esses campos e aceita versões 1–4. Interesses, dispensas e preferência de exibição são locais em DataStore e não acompanham o backup. Lembretes continuam locais e sujeitos às permissões/restrições do Android.

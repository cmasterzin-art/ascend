# ASCEND — progresso da versão 1.1

Implementação nativa em Kotlin, Compose, Material 3, Navigation, Room, DataStore, ViewModel, Coroutines e StateFlow. Sem permissão INTERNET, contas, anúncios ou backend.

## Adicionado na 1.1

- Jornada redesenhada: cena vetorial de Aetheris, cartão de personagem, metas do dia, semana de consistência e filtro de pendências.
- Temas escuro, claro e do sistema, modo minimalista e redução de movimento, com preferências locais.
- Busca por nome/categoria e filtros por estado/atributo. Detalhes de hábito com programação, sequência, recorde, últimos 14 dias e histórico editável.
- Formulário mais simples, opções avançadas recolhíveis e atalhos de quantidade/tempo no registro.
- Descansos globais por data no calendário; preservam sequências, XP e histórico. A taxa de cumprimento exclui atividades extras e descansos.
- Comparação de períodos, gráfico de linhas, distribuição por atributo e recorde histórico real.
- Resgate de missão disponível diretamente na Jornada, aviso com XP recebido e navegação de retorno no diário/configurações.
- Quatro novos capítulos originais, até a Aurora de Aetheris no nível 30.
- Migração Room 1 → 2 e leitura de backups v1, com testes de compatibilidade.

## Funcionalidades preservadas

- Criação de personagem, cinco classes, nome, objetivos, títulos e avatar vetorial personalizável.
- Cinco áreas de navegação com paleta escura, ilustrações vetoriais, barras animadas e celebração de nível.
- Hábitos simples, quantitativos, cronometrados e repetitivos. Nome, descrição, categoria, ícone, cor, atributo, dificuldade, dias, hora, meta e recompensas editáveis.
- XP, moedas e pontos livres; recompensa proporcional por valores absolutos, sem duplicação. Snapshot de recompensas por hábito/dia, edição explícita de registros e reversão.
- Pausa, arquivo, exclusão com confirmação preservando histórico, duplicação e ordenação. Sequências respeitam dias previstos e mudanças/pausas registradas.
- Calendário, histórico, rotinas e cronômetro persistente com pausa/retomada e registro de minutos efetivos.
- Missões diárias, semanais, épicas, personalizadas e chefes opcionais configuráveis. Resgates únicos por ocorrência e recálculo de recompensas após correções.
- Conquistas, galeria, títulos por marcos, loja, inventário, compra/equipamento/remoção, companheiros cosméticos com falas locais.
- Mapa vetorial de seis regiões, revelação por níveis, nove capítulos escritos e acessíveis mediante marcos.
- Vila com cinco construções e duas etapas de recuperação após as ruínas, cenas que refletem a restauração e compras transacionais.
- Diário editável/pesquisável, estatísticas reais com períodos, XP diário/semanal/mensal, barras, calendário de consistência e histórico de atributos.
- Lembretes locais opcionais com AlarmManager e reagendamento após reinício; não dependem de internet.
- Backup JSON versionado, checksum SHA-256, validação estrutural e importação transacional, via seletor de arquivos Android.

## Limites desta versão

- Regiões além da vila são revelações de mapa e narrativa; a reconstrução interativa está concentrada na Vila do Despertar.
- Dias globais de descanso podem ser planejados ou corrigidos pelo calendário. Não há Cristais da Perseverança, economia de cristais ou árvore de habilidades. A ausência não retira XP ou conquistas.
- Avatar é um retrato vetorial, com poucas opções de roupa/pele/moldura. Não há animação de corpo inteiro ou combate.
- Gráficos de linha para XP diário, distribuição por atributo e calendário de consistência. Períodos de 7, 30 e 90 dias; comparação de XP com o período anterior.
- Lembretes são aproximados e sujeitos às restrições de bateria do Android. O cronômetro usa horário persistido, não requer serviço contínuo e não toca alarme de foco em segundo plano.
- Calendário, sequências e estatísticas usam o histórico de programação registrado pelo aplicativo. Alterações feitas no mesmo dia substituem a programação desse dia. Não é possível reconstruir uma programação que nunca foi registrada.
- Conquistas representam marcos atingidos e são preservadas após correções; XP, pontos, moedas e recompensas de missões são recalculados. Missões resgatadas preservam snapshots das regras e recompensas por ocorrência; edições afetam as próximas ocorrências.
- Ao desfazer moedas já gastas, o saldo disponível fica em zero e ganhos futuros compensam a diferença. Nenhuma compra pode exceder o saldo.
- Preferências de interface e permissões de notificações pertencem ao aparelho e não são transferidas no backup. O diário do backup é texto legível.
- Banco Room v2 com migração explícita 1 → 2, sem destruição dos dados. Backups v2 incluem descansos e continuam lendo arquivos v1. Preferências visuais permanecem no aparelho.

A validação executada e os resultados de compilação constam em VALIDACAO.md.

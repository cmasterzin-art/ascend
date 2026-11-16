# ASCEND — progresso da versão 1.2

Implementação nativa em Kotlin, Compose, Material 3, Navigation, Room, DataStore, ViewModel, Coroutines e StateFlow. Sem permissão INTERNET, contas, anúncios ou backend.

## Adicionado na 1.2

- Livro de missões em pergaminho, mapa ilustrado e inventário com equipamentos ao redor do retrato, inspirados nas referências fornecidas. Arte vetorial original embutida; não são reproduções fotográficas das imagens.
- Seis habilidades persistentes em três caminhos; pré-requisitos, pontos e relíquias equipáveis. Nove novos itens no total, incluindo três de loja.
- Cinco raridades visuais, filtros por categoria, detalhes com origem/data e espaços de cabeça, traje, acessório e arma. Moldura, fundo e companheiro são equipados pela coleção.
- Três missões de moradores, troféus e diálogos conforme o estágio de cada construção; lanterna visível na cena.
- Títulos por atributos e nível 50, atributos na Jornada e silhueta de chefes.
- Backup v3 aceitando v1/v2, assinatura preservada da 1.1, testes de dados e interface.

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

- O documento de expansão não está implementado integralmente. Não há novos sons ambientes, vibração, gráficos avançados por mês/ano, diário com modelo narrativo por capítulo, registro individual de cada repetição ou histórico separado de sessões de foco.
- O ledger continua transacional, mas não é um log imutável de todas as edições. Metas quantitativas usam valores absolutos por dia e têm teto de 1.000.000.
- A árvore tem três caminhos e seis habilidades; a especialização é representada por títulos. Não há todas as árvores/classes avançadas propostas no documento.
- Corpo e capa compartilham o espaço de traje para preservar os itens antigos. Raridades são cosméticas; não há durabilidade ou poder de combate. Chefes usam as metas configuradas e recompensas de missões existentes.
- As missões dos moradores contam também as conclusões anteriores ao aceite, filtradas pelo atributo; ficam na aba Épicas. A reconstrução continua custando 30 e 60 fragmentos por construção, sem novos requisitos de atributos.


- Regiões além da vila são revelações de mapa e narrativa; a reconstrução interativa está concentrada na Vila do Despertar.
- Dias globais de descanso podem ser planejados ou corrigidos pelo calendário. Não há Cristais da Perseverança ou economia de cristais. A árvore de habilidades atual tem seis escolhas cosméticas; não concede multiplicadores de XP ou automações. A ausência não retira XP ou conquistas.
- Avatar é um retrato vetorial com equipamentos sobrepostos, roupas, pele e moldura. Não há animação de corpo inteiro ou combate.
- Gráficos de linha para XP diário, distribuição por atributo e calendário de consistência. Períodos de 7, 30 e 90 dias; comparação de XP com o período anterior.
- Lembretes são aproximados e sujeitos às restrições de bateria do Android. O cronômetro usa horário persistido, não requer serviço contínuo e não toca alarme de foco em segundo plano.
- Calendário, sequências e estatísticas usam o histórico de programação registrado pelo aplicativo. Alterações feitas no mesmo dia substituem a programação desse dia. Não é possível reconstruir uma programação que nunca foi registrada.
- Conquistas representam marcos atingidos e são preservadas após correções; XP, pontos, moedas e recompensas de missões são recalculados. Missões resgatadas preservam snapshots das regras e recompensas por ocorrência; edições afetam as próximas ocorrências.
- Ao desfazer moedas já gastas, o saldo disponível fica em zero e ganhos futuros compensam a diferença. Nenhuma compra pode exceder o saldo.
- Preferências de interface e permissões de notificações pertencem ao aparelho e não são transferidas no backup. O diário do backup é texto legível.
- Banco Room v2 com migração explícita 1 → 2, sem destruição dos dados. Backups v3 incluem descansos e habilidades e continuam lendo arquivos v1 e v2. Preferências visuais permanecem no aparelho.

A validação executada e os resultados de compilação constam em VALIDACAO.md.

# 1.7.0.1 — A Consolidação dos Caminhos

- Foco pertence ao Caminho gravado na sessão. Blocos dividem o intervalo elegível e sinalizam tempo sem atribuição comprovável.
- Rumos do Dia considera o relógio local. Caminhos pausados ou concluídos saem das pendências ativas, com confirmação quando ainda existem tarefas abertas.
- Sessão ativa aparece no topo da Jornada. Validações de vínculos, backup e triagem preservam os dados quando uma operação falha.
- Room v7, backup v9 e certificado preservados. [Auditoria](docs/AUDITORIA-1.7.0.1.md) · [Validação](VALIDACAO.md).

# 1.7.0 — Os Nove Caminhos

Maior atualização funcional sobre a base `feat/ascend-1.6.9.19`, transformando o ASCEND em um sistema integrado de hábitos, projetos pessoais e agenda avançada offline, mantendo integralmente a Biblioteca da Forja (`1.6.9.19`), o RPG e a economia:
- **Caminhos (`PathsScreen`, `PathDetailScreen`):** projetos pessoais com estados (`Planejado`, `Em andamento`, `Pausado`, `Concluído`, `Arquivado`, `Cancelado`), etapas, tarefas, subtarefas reordenáveis, mover tarefa, duplicação de estrutura, vínculo organizacional a hábitos (sem XP extra), foco em projeto/tarefa (`0 XP` adicional) e memória própria no filtro `Caminhos` do `Salão das Memórias`.
- **Agenda Profissional 2.0 (`PlannerScreen`):** visões `Dia`, `Semana` e `Mês`, `TimeBlock` com detecção de sobreposição e comparação planejado × foco real, compromissos `AgendaEvent` sem XP, `InboxScreen` com triagem atômica transacional, `PendingCenterScreen` com 6 recortes e painel `Rumos do Dia` na `Jornada`.
- **Room v7 (`MIGRATION_6_7`) e Backup v9:** esquema `7.json` aditivo e retrocompatível com `v1`–`v8`, rollback atômico comprovado em teste, **143 testes aprovados** (`135` preservados + `8` novos), `lintDebug` com **0 erros** (`22` avisos), `versionCode 20`, mesma assinatura oficial `e1f003ef...`. [Caminhos](docs/CAMINHOS-1.7.0.md) · [Agenda 2.0](docs/AGENDA-1.7.0.md) · [Visual 1.7.0](docs/VISUAL-1.7.0.md).

# 1.6.9.19 — A Biblioteca da Forja

Criação e integração controlada da biblioteca de recursos pictóricos originais (*Nordic Pictorial Library*) sobre a `1.6.9.15`, sem alterações funcionais ou estruturais: prancha conceitual (`docs/art/concepts/biblioteca-forja-16919.webp`) e **19 novos recursos WebP** gerados via `image-generator` e distribuídos em quatro famílias — **Materiais** (`mat_iron_nordic`, `mat_bronze_ancient`, `mat_silver_oxidized`, `mat_slate_volcanic`, `mat_leather_carved`, `mat_vellum_aged`), **Componentes de interface** (`ui_plate_primary`, `ui_plate_secondary`, `ui_plate_ceremonial`, `ui_field_inscription`, `ui_tablet_selector`, `ui_control_surface`, `ui_frame_dialog` aplicados com recorte 9-slice `drawNineSlice`), **Ornamentação** (`orn_divider_forged`, `orn_frames_rarity` por raridade `0..3`, `orn_medallions_seals`, `orn_track_progress`) e **Widgets** (`wid_plate_compact`, `wid_plate_expanded`). Todos os recursos anteriores foram preservados. `versionCode 19`, APK assinado com certificado oficial `e1f003ef...`. [Catálogo completo, prancha e comparativos](docs/VISUAL-1.6.9.19.md).

# 1.6.9.15 — O Legado da Forja

Consolidação do Design System Nórdico (*Premium Nordic Experience*), Widgets 3.0 e acabamento profissional sobre a `1.6.9.10`, sem alterações funcionais: botões forjados com variante cerimonial em ouro oxidado e filigrana (`SagaButton`), campos de inscrição em pedra rebaixada (`FormField`), seletores chanfrados (`ChipRow`), abas editoriais responsivas em `320 dp / 130%` (`AetherisTabs` em 2 linhas no menu do Herói), cinco famílias de superfície com marcenaria e iluminação próprias (`AetherSurface`), modais unificados (`AetherDialog`), estados de hábito diferenciados (`◆ Concluído`, `◈ Em curso`, `Pendente`), correção de unidade em hábitos cronometrados (`30/60 minutos`), maior nitidez nos nichos vazios do Inventário (`alpha = 0.66f`), aro de astrolábio no `FocusCard` e **Widgets 3.0** com faixas de atividade alinhadas à esquerda e barra vertical metálica de `4 dp`. `versionCode 18`, APK assinado com certificado oficial `e1f003ef...`. [Prancha de componentes, comparações e limites](docs/VISUAL-1.6.9.15.md).

# 1.6.9.10 — O Silêncio das Runas

Refinamento cinematográfico e editorial (*Nordic Cinematic UI*) sobre a biblioteca gráfica da `1.6.9`, sem alterações funcionais: `CompactHero` com integração contínua ao céu do fiorde, barra de XP em filete (`Meter(..., slim = true)`), linha diária `DayLedger` e faixa compacta de atalhos `ShortcutStrip`; menus secundários `AetherisTabs` em inscrições abertas sem grades de botões; ficha do Herói (`HeroStage`, `HeroEvolution`, `AttributeInscriptions`) em palco contínuo; câmara de artefatos no Inventário com iluminação localizada e maior contraste nos nichos vazios; `MissionCard2` e `SagaBook` compactados com marcos conectados; resumos abertos no Planejador e Central de Análises; distinção entre marcos solenes e inscrições cotidianas no Salão das Memórias; e iluminação superior fria nos widgets. `versionCode 17`, APK assinado com certificado oficial `e1f003ef...`. [Comparações e limites](docs/VISUAL-1.6.9.10.md).

# 1.6.9 — O Último Entalhe

Consolidação artística definitiva (*Nordic Cinematic UI*) sem alterações funcionais: família de 25 símbolos vetoriais (`CarvedArt.kt` / `CarvedGlyph`), seis recursos `.webp` originais (`carved_emblems`, `carved_seals`, `plate_iron`, `plate_bronze`, `artifact_frame`, `widget_slate`), navegação inferior e abas contínuas (`AetherisNavigation`, `AetherisTabs`), placas metálicas para botões (`SagaButton`), molduras de arsenal com raridades diferenciadas (`ArtifactFrame`), selos cerimoniais (`CeremonialSeal`), compactação dos menus superiores e superfície de ardósia nos widgets (`widget_surface.xml`). [Escopo e limites](docs/VISUAL-1.6.9.md).

# 1.6.8 — O Véu do Norte

Composição da Jornada, ficha vertical do Herói, cronômetro ampliado e organização dos filtros. [Escopo e limites](docs/VISUAL-1.6.8.md).

# ASCEND 1.6.5 — As Sombras do Norte

Paleta mineral, moldura pictórica, basalto, mostradores e widgets refinados. Apenas apresentação. [Capturas e limitações](docs/VISUAL-1.6.5.md).

# ASCEND — progresso da versão 1.6.2

Implementação nativa em Kotlin, Compose, Material 3, Navigation, Room, DataStore, ViewModel, Coroutines e StateFlow. Sem permissão INTERNET, contas, anúncios ou backend.

## Entregue na 1.6.2

- Biblioteca visual com materiais originais otimizados, sem padrões geométricos repetidos. Navegação contínua, botões/abas, Jornada, códice de missões, Herói e arsenal refinados.
- Aplicação global pelos componentes compartilhados; temas, minimalista, redução de movimento, Room v6 e backup v8 preservados. Apenas apresentação; nenhuma funcionalidade nova.
- 135 testes existentes aprovados sem alterações; build concluído e Lint com zero erros / 20 avisos. APK 1.6.2 / code13 com certificado preservado.
- [Comparações e limites reais](docs/VISUAL-1.6.2.md) · [Validação](VALIDACAO.md).

## Entregue na 1.6.1

- Biblioteca gráfica original e Design System 2.1 aplicados às superfícies, botões e navegação; composição heroica com fiorde, medalhão e retrato; arsenal com nichos trabalhados; Memórias com eixo cerimonial.
- Planejador com setas acessíveis. Sem mudanças em regras, persistência, economia ou permissões. Room v6 e backup v8 preservados.
- 135 testes aprovados; build e Lint concluídos (zero erros / 20 avisos). APK 1.6.1 / code12 com certificado preservado.
- [Comparações, origem dos recursos e limites](docs/VISUAL-1.6.1.md) · [Validação executada](VALIDACAO.md).

## Entregue na 1.6

- Design System 2.0 com superfícies operacional, heroica, cerimonial, pergaminho e artefato; componentes compartilhados e tokens. Jornada nova com retrato compacto, resumo diário, pendências primeiro, lista virtualizada e acessos diretos.
- Planejador Semanal com navegação por dia/semana, horários reais, descanso e inclusão/dispensa/reset por data. Fonte única da programação para Jornada, widgets, análises, sequências e lembretes.
- Central de Análises: 7/30/90 dias, semana/mês civis até hoje, comparação de mesma duração, gráficos com eixo/tabela, taxa, XP, minutos, atributos, ranking e insights determinísticos. Detalhes individuais pelos hábitos.
- Histórico de Foco persistido com ID/início/término/tempo/estado, cancelamento explícito, consulta, totais e comparação. Sem inventar sessões antigas ou pagar XP extra.
- Perfil em ficha lateral, inventário em superfície de coleção mantendo nichos, contratos operacionais/cerimoniais, páginas de Crônicas e Memórias, controles e widgets refinados. Pinturas mantidas.
- 135 testes aprovados (116 anteriores + 19 novos), Lint sem erros/20 avisos.
- Room v6 / backup v8, migração aditiva 5 → 6 e leitura v1–v7. Android/applicationId/certificado preservados; versionCode 11.
- [Regras e limites](docs/PRODUTIVIDADE-1.6.md) · [Antes/depois reais](docs/VISUAL-1.6.md) · [Sistema visual](docs/DESIGN-SYSTEM-2.0.md).

## Estabilizado na 1.5.2

- Registro rejeita quantidade negativa e o formulário não converte texto inválido em zero; permite corrigir sem perder o valor digitado.
- Snapshot de um registro novo vem do hábito persistido; snapshots existentes, fórmula e valores de XP/fragmentos permanecem iguais.
- Foco consulta o hábito atual e exige cronometrado ativo; encerramento considera descansos no histórico.
- Validação de datas do personagem/foco no backup, mantendo Room v5/backup v7; rollback verificado com falha de gravação injetada.
- Widgets mostram feedback para ação antiga, revalidam atalhos complexos, ocultam progresso sem programação e preservam o título na largura mínima. Modo estrito é explicado nas Configurações.
- Rascunhos de hábito/registro salvos para recriação da Activity, confirmação de descarte e período brasileiro sem datas repetidas.
- 116 testes aprovados; APK 1.5.2/versionCode 10, certificado preservado. [Auditoria, limites e checklist físico](docs/ESTABILIDADE-1.5.2.md).

## Refinado na 1.5.1

- Widgets com moldura discreta, símbolo próprio, progresso em destaque e botões diferentes para Concluir/Registrar/Abrir Jornada. Conteúdo se adapta à altura, largura e fonte; estados sem personagem e sem rituais têm mensagens curtas.
- Floresta e locais com seções mais legíveis; Expedições priorizam fase e objetivo, agrupam os espólios e preservam todas as etapas e regras. Guardião mantém a pintura e recebe hierarquia cerimonial.
- Memórias em lista eficiente com linha do tempo proporcional ao cartão, datas brasileiras, filtros e destaque discreto para Sagas/conclusões de Expedição.
- Crônica Semanal com números em duas colunas, narrativa em fonte de leitura e acontecimentos agrupados. Perfil com identificação antes do retrato e controles ajustados.
- Dias da semana em duas linhas com alvos confortáveis, inclusive em 320 dp/fonte 130%. Intenções consumidas do widget são retiradas da Activity para evitar reabertura indevida.
- Room v5, backup v7, applicationId, certificado e economia preservados. versionName 1.5.1 / versionCode 9. [Comparações e limites](docs/VISUAL-1.5.1.md).

## Adicionado na 1.5

- Floresta explorável, três locais, três Expedições e sete marcos, incluindo as três provações do Guardião da Névoa.
- Baselines por fase, seleção de hábitos, resgates únicos, correções integradas e percentual regional derivado.
- Pinturas originais em nove células, transformações em três estágios, broche, conquista, título, relatos e Memórias.
- Widgets compacto/expandido com conclusão simples segura, registro complexo e atualização local aproximada.
- Room v5 preservada; backup v7 compatível com v1–v6. [Regras e limites](docs/EXPLORACAO-1.5.md) · [Revisão visual](docs/VISUAL-1.5.md).

## Adicionado/refinado na 1.4.5 (histórico)

- Navegação em ferro, ícones Canvas originais e menus responsivos sem abas cortadas.
- Crônica Semanal derivada dos registros e Salão das Memórias pelo Perfil/Crônicas.
- Ciclo de abandono/arquivo/reinício das Sagas, prazo visual e confirmação de descarte.
- Conversas reconhecidas com cópia persistente, símbolos de habitantes e novas condições locais.
- Retrato ampliado, artefatos maiores, sons opcionais locais e volume.
- Room 5/backup 6 com compatibilidade anterior; não muda a economia. [Revisão](docs/VISUAL-1.4.5.md).

## Adicionado/refinado na 1.4 (histórico)

- Sagas Pessoais, quatro marcos, início com fotografia de progresso, resgate único e correções integradas. [Regras e limites](SAGAS.md).
- Botões em bronze, pressão breve, recompensas consolidadas, emblemas, Jornada com próxima atividade e região, leitura por parágrafos e capítulos revelados.
- Diálogos contextuais por prioridade; a ação Guardar esta memória impede reexibição e acompanha o backup. Não é uma simulação por IA.
- Reconstrução com crossfade de 450 ms (0 em redução de movimento), pré-requisitos e estados da árvore mais claros.
- Room 4/backup 5 com compatibilidade anterior. [Capturas e revisão](docs/VISUAL-1.4.md).

## Adicionado/refinado na 1.3.1 (histórico)

- 102 sugestões em nove categorias e seis rotinas, totalmente offline. Busca, filtros, ordenação, interesses e nomes narrativos opcionais. [Fluxo completo e contagens](docs/GRIMORIO.md).
- Formulário completo antes de adicionar; seleção parcial de rotinas, edição individual e prevenção transacional de duplicatas. Nenhuma mudança no cálculo de XP ou moeda.
- Pinturas de equipamentos em nichos, 17 identidades visuais, marcadores de pedra, medidores e botões refinados.
- Três cenas próprias para cada construção: ruína, reconstrução e restauração.
- Migração aditiva Room 2 → 3 e backup v4, com compatibilidade anterior testada. [Revisão visual](docs/VISUAL-1.3.1.md).

## Redesenhado na 1.3 (histórico)

- Jornada com painel heroico e cenário de fiorde; personagem adulto ilustrado, com três tons de pele e equipamentos sobrepostos.
- Inventário/loja em couro e ferro, sete espaços, ícones de artefatos, símbolos de raridade e abas fixas.
- Atlas com geografia ilustrada e marcadores funcionais, mantendo os seis desbloqueios originais.
- Livro de missões, chefe mítico, vila e páginas de crônicas com nova direção de arte.
- Materiais, tipografia Cinzel local, paleta nórdica, selos, divisórias e medidores entalhados. Transição entre abas e feedback de equipamento respeitam redução de movimento.
- Temas claro/escuro/sistema e modo minimalista preservados. Validação de navegação em 320 dp e texto ampliado para 130%.
- Nenhuma nova regra econômica ou mudança no armazenamento. [Revisão e comparação visual](docs/VISUAL-1.3.md).

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

- Criação de personagem, cinco classes, nome, objetivos, títulos e retrato ilustrado personalizável.
- Cinco áreas de navegação com paleta escura, ilustrações vetoriais, barras animadas e celebração de nível.
- Hábitos simples, quantitativos, cronometrados e repetitivos. Nome, descrição, categoria, ícone, cor, atributo, dificuldade, dias, hora, meta e recompensas editáveis.
- XP, moedas e pontos livres; recompensa proporcional por valores absolutos, sem duplicação. Snapshot de recompensas por hábito/dia, edição explícita de registros e reversão.
- Pausa, arquivo, exclusão com confirmação preservando histórico, duplicação e ordenação. Sequências respeitam dias previstos e mudanças/pausas registradas.
- Calendário, histórico, rotinas e cronômetro persistente com pausa/retomada e registro de minutos efetivos.
- Missões diárias, semanais, épicas, personalizadas e chefes opcionais configuráveis. Resgates únicos por ocorrência e recálculo de recompensas após correções.
- Conquistas, galeria, títulos por marcos, loja, inventário, compra/equipamento/remoção, companheiros cosméticos com falas locais.
- Mapa ilustrado de seis regiões, revelação por níveis, nove capítulos escritos e acessíveis mediante marcos.
- Vila com cinco construções e duas etapas de recuperação após as ruínas, cenas que refletem a restauração e compras transacionais.
- Diário editável/pesquisável, estatísticas reais com períodos, XP diário/semanal/mensal, barras, calendário de consistência e histórico de atributos.
- Lembretes locais opcionais com AlarmManager e reagendamento após reinício; não dependem de internet.
- Backup JSON versionado, checksum SHA-256, validação estrutural e importação transacional, via seletor de arquivos Android.

## Limites desta versão

- O documento de expansão não está implementado integralmente. Não há novos sons ambientes, vibração, gráficos avançados por mês/ano, diário com modelo narrativo por capítulo, registro individual de cada repetição ou histórico separado de sessões de foco.
- O ledger continua transacional, mas não é um log imutável de todas as edições. Metas quantitativas usam valores absolutos por dia e têm teto de 1.000.000.
- A árvore tem três caminhos e seis habilidades; a especialização é representada por títulos. Não há todas as árvores/classes avançadas propostas no documento.
- Corpo e capa compartilham o espaço de traje para preservar os itens antigos. Raridades são cosméticas; não há durabilidade ou poder de combate. Chefes usam as metas configuradas e recompensas de missões existentes.
- Cenários e retratos são pinturas estáticas locais; o mapa não possui zoom/pan e usa os marcadores da progressão existente. A vila tem três pinturas por construção e luzes no panorama geral, sem renderização 3D.
- As missões dos moradores contam também as conclusões anteriores ao aceite, filtradas pelo atributo; ficam na aba Épicas. A reconstrução continua custando 30 e 60 fragmentos por construção, sem novos requisitos de atributos.


- Além da vila, a Floresta da Persistência recebe exploração interativa na 1.5. As outras quatro regiões são revelações de mapa/narrativa, sem expedições próprias.
- Dias globais de descanso podem ser planejados ou corrigidos pelo calendário. Não há Cristais da Perseverança ou economia de cristais. A árvore de habilidades atual tem seis escolhas cosméticas; não concede multiplicadores de XP ou automações. A ausência não retira XP ou conquistas.
- Avatar é um retrato ilustrado com três tons de pele; equipamentos são apresentados em nichos separados. As classes compartilham a mesma base de retrato; a classe selecionada e seus dados são preservados. Não há animação de corpo inteiro ou combate.
- Gráficos de linha para XP diário, distribuição por atributo e calendário de consistência. Períodos de 7, 30 e 90 dias; comparação de XP com o período anterior.
- Lembretes são aproximados e sujeitos às restrições de bateria do Android. O cronômetro usa horário persistido, não requer serviço contínuo e não toca alarme de foco em segundo plano.
- Calendário, sequências e estatísticas usam o histórico de programação registrado pelo aplicativo. Alterações feitas no mesmo dia substituem a programação desse dia. Não é possível reconstruir uma programação que nunca foi registrada.
- Conquistas representam marcos atingidos e são preservadas após correções; XP, pontos, moedas e recompensas de missões são recalculados. Missões resgatadas preservam snapshots das regras e recompensas por ocorrência; edições afetam as próximas ocorrências.
- Ao desfazer moedas já gastas, o saldo disponível fica em zero e ganhos futuros compensam a diferença. Nenhuma compra pode exceder o saldo.
- Preferências de interface e permissões de notificações pertencem ao aparelho e não são transferidas no backup. O diário do backup é texto legível.
- Banco Room v5 com migrações explícitas 1 → 2 → 3 → 4 → 5, sem destruição dos dados. Backups v7 incluem conteúdo de expedições, estados das sagas e texto das memórias; continuam lendo v1–v6. Preferências visuais, interesses e dispensas permanecem no aparelho.

A validação executada e os resultados de compilação constam em VALIDACAO.md.

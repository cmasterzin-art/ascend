# 1.7.1 — A Sabedoria dos Nove Reinos

Expansão de conhecimento pessoal e evolução de arquétipos (*Knowledge & Character Ascension Update*) sobre a base `feat/ascend-1.7.0.1`, preservando integralmente a Biblioteca da Forja (`1.6.9.19`), as correções da `1.7.0.1`, a economia de XP/moedas e os 155 testes anteriores. [Biblioteca de Mímir](docs/BIBLIOTECA-MIMIR-1.7.1.md) · [Arquétipos](docs/ARQUETIPOS-1.7.1.md) · [Visual 1.7.1](docs/VISUAL-1.7.1.md).

## A Biblioteca de Mímir & Runas da Memória

- **Cadernos e páginas em Markdown (`KnowledgeNotebook`, `KnowledgePage`, `MimirScreens.kt`):** gestão local de cadernos temáticos com 6 capas pictóricas, alternância `Visualizar` / `Editar`, pré-visualização Markdown local (`#`, `##`, `###`, negrito, itálico, código inline/bloco, listas, citações e links), tags normalizadas, favoritos, busca global/local, ordenação, duplicação, movimentação, arquivamento e exclusão em cascata segura.
- **Ligações, backlinks e referências (`KnowledgePageLink`, `KnowledgeReference`):** ligações direcionadas entre páginas com seção `Referenciada por`, preservação ao renomear títulos, referências bibliográficas estruturadas e integração opcional com `PersonalPath` e `PathTask` (sem apagar conhecimento ao excluir projeto/tarefa).
- **Runas da Memória (`Flashcard`, `FlashcardReview`):** revisão espaçada SM-2 determinística (`Errei`, `Difícil`, `Bom`, `Fácil`), sanitização de `easeFactor`, sessão interrompível com persistência por cartão, proteção contra revisão repetida no mesmo dia, `0 XP` / `0 moedas` extras e atalho discreto em `Rumos do Dia`.

## Ascensão dos Arquétipos & O Acervo de Mímir

- **10 especializações (2 por classe, nível 3) e 10 habilidades de ascensão:** escolha, troca e remoção seguras sem apagar habilidades obtidas; `SkillTree` com filtro de classe mantendo intactas as 6 constelações universais originais e o sistema `skillPoints(s)`; 5 Marcos de Sabedoria idempotentes (`0` moedas).
- **3 recursos pictóricos originais (`generate_image`, `351,1 KB`):** `mimir_library.webp`, `archetype_seals_atlas.webp` (`5 × 2` RGBA) e `mimir_codex_atlas.webp` (`3 × 2` RGB).
- **Room v8 (`MIGRATION_7_8`) e Backup v10:** migração explícita aditiva (`8.json`), retrocompatibilidade `v1`–`v9`, rollback atômico e **167 testes aprovados** (`155` preservados + `12` novos), Lint com **0 erros**, `versionCode 22`.

# 1.7.0.1 — A Consolidação dos Caminhos

- Foco pertence ao Caminho gravado na sessão. Blocos dividem o intervalo elegível e sinalizam tempo sem atribuição comprovável.
- Rumos do Dia considera o relógio local. Caminhos pausados ou concluídos saem das pendências ativas, com confirmação quando ainda existem tarefas abertas.
- Sessão ativa aparece no topo da Jornada. Validações de vínculos, backup e triagem preservam os dados quando uma operação falha.
- Room v7, backup v9 e certificado preservados. [Auditoria](docs/AUDITORIA-1.7.0.1.md) · [Validação](VALIDACAO.md).

# 1.7.0 — Os Nove Caminhos

Evolução funcional do ASCEND sobre a base `feat/ascend-1.6.9.19` (*Personal Projects & Advanced Productivity Update*), integrando gestão de projetos pessoais, agenda profissional e centralização de pendências sem alterar regras econômicas, progresso histórico de hábitos ou a Biblioteca da Forja (`1.6.9.19`). [Caminhos 1.7.0](docs/CAMINHOS-1.7.0.md) · [Agenda 2.0](docs/AGENDA-1.7.0.md) · [Visual 1.7.0](docs/VISUAL-1.7.0.md).

## Projetos Pessoais (`Caminhos`)

- **`PersonalPath`, `PathStage`, `PathTask`, `PathSubtask` e `PathHabitLink`:** gestão completa de projetos com estados (`Planejado`, `Em andamento`, `Pausado`, `Concluído`, `Arquivado`, `Cancelado`), prioridades, prazos, etapas com progresso derivado real, subtarefas reordenáveis, mover tarefa entre caminhos/etapas e duplicação de estrutura sem copiar foco ou memórias.
- **Vínculo de hábitos e foco em projeto/tarefa:** associação organizacional com hábitos (sem XP duplicado) e sessões de foco persistidas em `focus_sessions` vinculadas a `pathId`/`taskId` com `0 XP` / `0 moedas` adicionais.
- **Salão das Memórias (`Caminhos`):** conclusão de projeto gera registro real próprio e filtro dedicado em `Mundo → Salão das Memórias`.

## Agenda Profissional 2.0, Entrada, Pendências e Jornada

- **Planejador em Dia / Semana / Mês (`AgendaScreens.kt`):** visualização diária detalhada, seleção semanal e calendário mensal (`monthMarker`), mantendo integralmente as exceções `PlanException` da `1.6`.
- **Blocos de Tempo e Compromissos (`TimeBlock`, `AgendaEvent`):** blocos com detecção automática de sobreposição e comparação planejado × foco real (`timeBlockActualMinutes`), além de compromissos pessoais sem XP.
- **Caixa de Entrada e Central de Pendências (`InboxScreen`, `PendingCenterScreen`):** triagem transacional atômica para tarefa ou compromisso, e 6 recortes (`Hoje`, `Próximas`, `Atrasadas`, `Sem data`, `Alta prioridade`, `Concluídas`) excluindo projetos arquivados/cancelados.
- **Jornada (`RumosDoDiaPanel` e `ShortcutStrip`):** atalhos diretos para `Caminhos`, `Entrada` e `Pendências`, além de painel expansível na tela inicial.

## Persistência, Compatibilidade e Validação

- **Room v7 (`MIGRATION_6_7`) e Backup v9:** migração explícita preservando esquemas `1.json` a `6.json`, leitura compatível de backups `v1` a `v8` e importação transacional com rollback atômico.
- `versionCode 20`, `versionName 1.7.0`, `applicationId br.com.ascend.aetheris`, sem permissão `INTERNET`.
- **143 testes aprovados** (`135` preservados + `8` novos em `PathsAndAgendaTest` e `PathsAndAgendaUiTest`) e Lint com **0 erros** (`22` avisos preexistentes).

# 1.6.9.19 — A Biblioteca da Forja

Criação e integração da nova biblioteca de recursos pictóricos originais (*Nordic Pictorial Library*) sobre a base `feat/ascend-1.6.9.15`, sem implementar funcionalidades, reformular layouts ou realizar alterações arquiteturais, preservando integralmente Room v6, backup v8, economia, narrativas, os 135 testes existentes e todos os recursos gráficos anteriores. [Catálogo completo, prancha e comparativos](docs/VISUAL-1.6.9.19.md) · [Inventário técnico](docs/art/README.md).

## Biblioteca Pictórica Nórdica (20 novos recursos WebP) e Integração 9-Slice

- **Geração pictórica original (`image-generator` / Nano Banana):** prancha conceitual (`docs/art/concepts/biblioteca-forja-16919.webp`) e **19 recursos WebP** em `app/src/main/res/drawable-nodpi/` distribuídos em quatro famílias:
  - **Família I — Materiais (`512 × 512` RGB):** `mat_iron_nordic.webp` (ferro nórdico enegrecido), `mat_bronze_ancient.webp` (bronze artesanal envelhecido), `mat_silver_oxidized.webp` (prata oxidada), `mat_slate_volcanic.webp` (ardósia vulcânica), `mat_leather_carved.webp` (couro cerimonial entalhado) e `mat_vellum_aged.webp` (velino envelhecido), integrados via `ForgeMaterial` e `Modifier.forgeMaterial` em `AetherSurface` (`ForgeArt.kt`).
  - **Família II — Componentes de interface (RGBA com 9-slice):** `ui_plate_primary.webp`, `ui_plate_secondary.webp` e `ui_plate_ceremonial.webp` em `SagaButton` (`CarvedArt.kt`); `ui_field_inscription.webp` em `FormField` (`Forms.kt`); `ui_tablet_selector.webp` em `ChipRow` e `FilterChip` (`Design.kt`, `Forms.kt`); `ui_control_surface.webp` em `Toggle` (`Forms.kt`); e `ui_frame_dialog.webp` em `AetherDialog` (`DesignSystem2.kt`), renderizados por `DrawScope.drawNineSlice` / `Modifier.pictorialFrame` sem deformação de cantos ou rebites.
  - **Família III — Ornamentação (atlas RGBA):** `orn_divider_forged.webp` (`1 × 2`) em `NorthDivider` e `SagaDivider`; `orn_frames_rarity.webp` (`2 × 2` por raridade `0..3`) em `ArtifactFrame` e `RelicArt`; `orn_medallions_seals.webp` (`3 × 2`) em `ForgeMedallion`, `DayDial` e `RuneSeal`; e `orn_track_progress.webp` (`1 × 2`, sulco em pedra e metal fundido) em `Meter` (`Design.kt`).
  - **Família IV — Widgets (RGBA):** `wid_plate_compact.webp` (`widget_surface.xml`) e `wid_plate_expanded.webp` (`widget_surface_expanded.xml`), alternados dinamicamente conforme a altura do widget em `HabitWidget.kt`, além de `ui_plate_primary.webp` e `ui_plate_secondary.webp` nos botões de ação (`widget_complete.xml`, `widget_secondary.xml`).

## Compatibilidade, assinatura e validação

- `versionCode 19`, `versionName 1.6.9.19`, `applicationId br.com.ascend.aetheris`, sem permissão `INTERNET`.
- Assinatura oficial preservada (`e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`) via `ASCEND_KEYSTORE_BASE64`, com APK publicado em `downloads/ASCEND-1.6.9.19.apk`.
- `testDebugUnitTest assembleDebug lintDebug` aprovado com **135 testes** e **0 erros** no Lint. Prancha da biblioteca gerada em `docs/screenshots/v1.6.9.19/prancha-biblioteca-16919.png`.

# 1.6.9.15 — O Legado da Forja

Consolidação do Design System Nórdico (*Premium Nordic Experience*), Widgets 3.0 e acabamento individual dos componentes sobre a base `feat/ascend-1.6.9.10`, preservando integralmente regras de negócio, banco Room v6, backup v8, economia, narrativas e os 135 testes existentes. [Prancha de componentes, comparações reais e limites](docs/VISUAL-1.6.9.15.md).

## Design System Nórdico, superfícies e controles

- **Botões forjados e estados (`NorthComponents.kt`, `CarvedArt.kt`):** `SagaButton` e `plateMaterial` refinados com chanfros de cinzel nos cantos, iluminação especular superior, filete central com diamante e nova variante `ceremonial = true` (ouro oxidado, dupla filigrana, diamantes laterais em `Canvas` e tipografia `SagaFont`) aplicada a resgates de contratos, Sagas, Expedições, restauração da Vila, registro de Foco e subida de nível.
- **Inscrições, seletores, abas e trilhos (`Design.kt`, `Forms.kt`, `AetherisNavigation.kt`):** `FormField` em tábua rebaixada de pedra/ferro com base em bronze; `ChipRow` e `FilterChip` semanais em tabuletas chanfradas; `Toggle` em bronze e ferro; `AetherisTabs` com diamante central no sublinhado ativo e compactação responsiva automática em `320 dp` (reduzindo o menu do Herói para 2 linhas em `320 dp / 130%`); e `Meter` com alto contraste em sépia sobre pergaminho.
- **Cinco famílias de superfície e modais (`ForgeArt.kt`, `DesignSystem2.kt`):** diferenciação estrutural de cantos, marcenaria e iluminação localizada em `AetherSurface` (`Operational`, `Heroic`, `Ceremonial`, `Parchment`, `Artifact`) e novo modal compartilhado `AetherDialog`.
- **Acabamento nas telas e Widgets 3.0 (`JourneyHome.kt`, `JourneyScreens.kt`, `EquipmentScreen.kt`, `RpgDesign.kt`, `SagaScreen.kt`, `ProductivityScreens.kt`, `GrimoireScreen.kt`, `widget_*.xml`, `HabitWidget.kt`):** diferenciação visual imediata nos estados de `HabitCard` (`◆ Concluído`, `◈ Em curso`, `Pendente`), correção de unidade em hábitos cronometrados (`30/60 minutos`), indicador de rolagem nos filtros de Hábitos, revisão de singular/plural, maior nitidez nas silhuetas dos nichos vazios do Inventário (`alpha = 0.66f`), aro de astrolábio com 12 marcações radiais no `FocusCard` e **Widgets 3.0** com faixas de atividade alinhadas à esquerda e barra vertical metálica de `4 dp`.

## Compatibilidade, assinatura e validação

- `versionCode 18`, `versionName 1.6.9.15`, `applicationId br.com.ascend.aetheris`, sem permissão `INTERNET`.
- Assinatura oficial preservada (`e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`) via `ASCEND_KEYSTORE_BASE64`, com APK publicado em `downloads/ASCEND-1.6.9.15.apk`.
- `testDebugUnitTest assembleDebug lintDebug` aprovado com **135 testes** e **0 erros** no Lint (`22` avisos). Prancha de componentes gerada em `docs/screenshots/v1.6.9.15/prancha-componentes-16915.png`.

# 1.6.9.10 — O Silêncio das Runas

Refinamento cinematográfico e editorial (*Nordic Cinematic UI*) sobre a base `feat/ascend-1.6.9`, preservando integralmente regras de negócio, banco Room v6, backup v8, economia, narrativas e os 135 testes existentes. [Comparações reais e limites](docs/VISUAL-1.6.9.10.md).

## Composição, iluminação e hierarquia visual

- **Jornada (`DesignSystem2.kt`, `JourneyHome.kt`):** cabeçalho integrado diretamente ao céu do fiorde (`CompactHero`), transição longa de névoa e vinhetas laterais sem borda retangular, iluminação fria localizada atrás do retrato, barra de XP em filete (`Meter(..., slim = true)`), linha diária `DayLedger` aberta e faixa de atalhos compacta (`ShortcutStrip` com ocultação automática de glifos em `320 dp / 130%`).
- **Navegação, abas e filtros (`AetherisNavigation.kt`, `Design.kt`):** `AetherisTabs` redesenhado como linha aberta de inscrições (`FlowRow`) com glifo ativo e sublinhado de bronze iluminado, substituindo grades de botões nas 7 opções do Herói, 6 opções de Missões e 4 de Hábitos; `ChipRow` compactado (`32 dp`) com suporte a faixa única sem truncamento lateral.
- **Herói e Inventário (`HeroSheet.kt`, `CharacterScreen.kt`, `EquipmentScreen.kt`, `RpgDesign.kt`):** ficha em palco contínuo (`HeroStage` com sangria lateral de `-20 dp`), evolução em filete (`HeroEvolution`), atributos inscritos em livro contínuo (`AttributeInscriptions`) sem cartões repetidos e câmara de artefatos com luz fria localizada, chanfro nos nichos, maior contraste nas silhuetas vazias (`alpha = 0.50f`) e aura por raridade.
- **Missões, Sagas, Hábitos, Planejador e Análises (`DesignSystem2.kt`, `SagaScreen.kt`, `JourneyScreens.kt`, `ProductivityScreens.kt`):** `SagaCover` em lombada de couro e bronze, contratos cotidianos compactados com ações lado a lado em `MissionCard2`, marcos `25% · 50% · 75% · 100%` conectados em `SagaBook`, ações secundárias em `FlowRow` sem quebra de palavras em `320 dp / 130%` e resumos abertos com `Hairline()` no Planejador e na Central de Análises.
- **Memórias, Atlas, Expedições, Cronômetro e Widgets (`ChronicleScreens.kt`, `WorldScreen.kt`, `ExplorationScreens.kt`, `widget_*.xml`):** distinção entre marcos solenes (`Ceremonial` com `NorthDivider`) e inscrições cotidianas no Salão das Memórias; marcadores translúcidos e leitura aberta no Atlas; selos conectados nas Expedições; halo radial violeta no `FocusCard`; e iluminação superior fria na ardósia e botões dos widgets.

## Compatibilidade, assinatura e validação

- `versionCode 17`, `versionName 1.6.9.10`, `applicationId br.com.ascend.aetheris`, sem permissão `INTERNET`.
- Assinatura oficial preservada (`e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`) via `ASCEND_KEYSTORE_BASE64`, com APKs publicados em `downloads/ASCEND-1.6.9.10.apk` e `downloads/ASCEND-1.6.9.apk`.
- `testDebugUnitTest assembleDebug lintDebug` aprovado com **135 testes** e **0 erros** no Lint (`22` avisos). Detalhes em `ASSINATURA.md` e `VALIDACAO.md`.

# 1.6.9 — O Último Entalhe

Consolidação artística definitiva (*Nordic Cinematic UI*) preservando integralmente regras, banco Room v6, backup v8, economia, narrativas e os 135 testes existentes. [Escopo e limites](docs/VISUAL-1.6.9.md).

## Direção artística, iconografia e recursos originais

- Biblioteca `CarvedArt.kt` com 25 símbolos vetoriais originais (`Glyph` / `CarvedGlyph`), emblemas das cinco classes (`ClassEmblem` em `NorthComponents.kt`) e dos cinco atributos (`AttributeEmblem`), além de selos cerimoniais (`CeremonialSeal` / `SealKind`).
- Seis folhas e texturas `.webp` originais geradas com `image-generator` em `app/src/main/res/drawable-nodpi/` (`carved_emblems.webp`, `carved_seals.webp`, `plate_iron.webp`, `plate_bronze.webp`, `artifact_frame.webp`, `widget_slate.webp`) e conceito `docs/art/concepts/entalhe-169.webp`, inventariados em `docs/art/README.md`.

## Componentes, telas e widgets

- Navegação inferior e abas superiores (`AetherisNavigation.kt`) em superfície contínua discreta com traço `NavigationSigil` próprio.
- Botões `SagaButton` (`NorthComponents.kt`) com acabamento de placa de bronze (primário) e ferro (`plateMaterial`), e refinamento de `Meter`, `ChipRow`, `FormField`, `Action` e `Empty` em `Design.kt`.
- Jornada (`DesignSystem2.kt` / `JourneyHome.kt`) com `CompactHero` integrado ao fiorde, `dialSize` responsivo e atalhos compactos (`Planejador`, `Análises`, `Foco`).
- Herói e Inventário (`CharacterScreen.kt`, `EquipmentScreen.kt`) com faixa superior compacta, cinco `AttributeEmblem` no perfil, `ArtifactFrame` ao redor de `RelicArt` e nichos vazios preservados (`dim`).
- Missões, Hábitos, Sagas e Conquistas (`MissionCard2`, `HabitCard`, `SagaCover`, `SealKind.Reward`, `SealKind.Saga`) com diferenciação clara e leitura preservada em `320 dp` / fonte `130%`.
- Widget Android (`app/src/main/res/drawable/widget_surface.xml`) com `widget_slate.webp` (`alpha="0.18"`) e moldura de dupla borda fina sem faixas esticadas.

## Compatibilidade e validação

- `versionCode 16`, `versionName 1.6.9`, `applicationId br.com.ascend.aetheris`, sem permissão `INTERNET`.
- GitHub Actions execução `44214011907` (`feat/ascend-1.6.9`): `testDebugUnitTest assembleDebug lintDebug` aprovado com **135 testes**, **0 erros** no Lint (`22` avisos) e APK oficial publicado em `downloads/ASCEND-1.6.9.apk` (`e1f003ef...`).

# 1.6.8 — O Véu do Norte

Composição da Jornada, ficha vertical do Herói, cronômetro ampliado e organização dos filtros. [Escopo e limites](docs/VISUAL-1.6.8.md).

# ASCEND 1.6.5 — As Sombras do Norte

Paleta mineral, moldura pictórica, basalto, mostradores e widgets refinados. Apenas apresentação. [Capturas e limitações](docs/VISUAL-1.6.5.md).

# Mudanças — 1.6.2 · Refinamento Visual Nórdico

- Quatro recursos originais em WebP: ferro, couro, velino e medalhão; removidos padrões repetidos de riscos, rebites e entrelaçados dos componentes.
- Navegação contínua com pendente de seleção, botões e abas em placas discretas; mesmos controles e ações.
- Jornada com composição compacta de retrato/fiorde/XP, mostrador diário e atalhos; Missões como códice; Herói e nichos refinados. Materiais propagados pelas superfícies compartilhadas.
- Exclusivamente visual. Room v6, backup v8, permissões, dados, funções e economia preservados. Versão 1.6.2 / code13.
- [Capturas comparativas e limitações](docs/VISUAL-1.6.2.md).

# Mudanças — 1.6.1 · As Forjas de Aetheris

- Nordic Cinematic UI / DS2.1: biblioteca Canvas original, ferro riscado, couro costurado, papel granulado, chapas heroicas, nichos rebaixados, entrelaçados e medalhão.
- Jornada com fiorde integrado, retrato emoldurado e nível; Herói, arsenal e Memórias com composições refinadas. Navegação e botões compartilhados chanfrados.
- Planejador com setas e descrições acessíveis, preservando a agenda e evitando quebra de palavras em telas estreitas.
- Apenas apresentação. Room v6, backup v8, regras, dados, retratos e permissões preservados. Versão 1.6.1 / code 12.
- [Capturas comparativas e limites reais](docs/VISUAL-1.6.1.md).

# Mudanças — 1.6.0 · O Renascimento de Aetheris

- Design System 2.0 aplicado a Jornada, hábitos, contratos, Sagas, Perfil, inventário, leitura, controles e widgets; cinco contextos de superfície, cabeçalhos e métricas compartilhados.
- Jornada com retrato compacto, painel do dia, pendências prioritárias, LazyColumn e acessos a Planejador/Análises/Foco.
- Planejamento semanal persistido: incluir/dispensar/retornar à regra normal por data, sem modificar registros ou pagar XP. Programação integrada a widgets, descansos, estatísticas, sequências e lembretes.
- Central de Análises com períodos móveis/civis, comparação, gráfico com eixo/valores, detalhes por hábito, ranking e insights locais; cálculo em dispatcher de trabalho e índices transitórios.
- Sessões detalhadas de foco com identificação estável, timestamps, tempo efetivo, estado, registro ou cancelamento, histórico/totais/detalhes. Encerramento transacional e pagamento único existentes preservados.
- Foco configurado em formulário completo com proteção de pedido/rascunho; sessões antigas não são convertidas artificialmente em histórico detalhado.
- Room v6 com migração 5 → 6; backup v8 lê v1–v7 e inclui os novos dados. Mesmo pacote/certificado, versionCode 11.
- Capturas reais comparadas à base 1.5.2 com a mesma fixture em worktree isolado. Resultados finais em VALIDACAO.md.

# Mudanças — 1.5.2 · A Forja da Estabilidade

- Correção de progresso inválido sendo convertido em zero, snapshot novo usando hábito desatualizado e início de foco com referência incompatível/excluída.
- Registro de foco considera descansos; backup valida datas do personagem e da sessão antes da restauração, sem alterar Room v5/backup v7.
- Widget explica recusa de ação antiga, revalida atalhos de registro, oculta barra sem atividades e protege título em largura mínima/fonte 130%.
- Estado salvo de editores de hábito/registro e confirmação de descarte. Recriação da Activity após widget validada no Robolectric.
- Período de um dia mostra uma data; Diário mantém apresentação brasileira. Pinturas, sons, economia, Sagas e Expedições preservados.
- 116 testes aprovados (97 anteriores + 19 adicionais), Android Lint sem erros/21 avisos documentados; versionCode 10, mesma assinatura. [Auditoria completa](docs/ESTABILIDADE-1.5.2.md).

# Mudanças — 1.5.1 · O Polimento de Aetheris

- Widgets: moldura em ferro/bronze, símbolo próprio, percentual, data e botões com hierarquia; densidade conforme espaço/fonte; estados vazios intencionais.
- Floresta, locais e Expedições: respiros, seções de leitura, estados visíveis, objetivo antes da premissa e recompensa final agrupada. Fases e lógica do Guardião preservadas.
- Salão das Memórias: lista eficiente, eixo contínuo por inscrição, datas brasileiras, filtros e ênfase para marcos importantes.
- Crônica Semanal: estatísticas em pares, narrativa legível, acontecimentos agrupados e período brasileiro. Perfil mais compacto, detalhes de artefatos e aquisição mais claros.
- Botões com bronze mais sóbrio, textura menos ruidosa, campos em fonte funcional e dias da semana com pelo menos 48 dp no cenário 320 dp/130%.
- Correção de extras já consumidos nos atalhos de widget. Nenhuma alteração de cálculo de XP, banco, backup ou conteúdo de progressão.
- Room v5 / backup v7; versionCode 9, applicationId e certificado preservados. 97 testes aprovados, incluindo os 90 anteriores; APK assinado e capturas reais.

# Mudanças — 1.5.0 · O Despertar dos Reinos

- Floresta da Persistência explorável no nível 3, mantendo as seis regiões, ordem e requisitos anteriores. Fluxo Atlas → Região → Local → Expedição.
- Marco dos Retornos, Santuário das Raízes e Coração da Névoa: locais encadeados, ocultação de conteúdo futuro e estado real persistente.
- Três Expedições originais: Raízes sob a Névoa (5 conclusões), Santuário Esquecido (2+2+2) e Vigília da Floresta (2+3+3). Guardião da Névoa com Chamado, Resistência e Juramento.
- Fotografia por fase e resgate transacional único. Cálculo compartilhado com Sagas; correções reavaliam a cadeia. Sem duplicar XP dos hábitos.
- Progresso regional explícito por sete marcos; pinturas da floresta e santuário em três estágios, Guardião e novo Broche da Vigília, em atlas WebP original. Conquista e título cosmético finais.
- Memórias com datas de resgate e diálogo do Cronista para cada aventura; relatos anteriores e nove capítulos preservados.
- Widgets Android compacto/expandido, redimensionáveis e claro/escuro do sistema. Pendências, nível/XP, conclusão de hábito simples pelo repositório e abertura de registro complexo. Atualização por transação, host, data/fuso/reinício e alarme aproximado, sem serviço contínuo.
- Room permanece v5, sem migração desnecessária; backup v7 aceita v1–v7. versionCode 8, mesmo certificado/applicationId.

# Mudanças — 1.4.5 · As Vozes de Aetheris

- Navegação inferior própria em ferro com cinco ícones originais Canvas, seleção esculpida, resposta de 160 ms, insets do sistema e rótulos sem truncamento.
- AetherisTabs nos menus de Herói, Missões, Mundo, Hábitos e Grimório: opções completas em duas/três colunas, sem categorias de navegação parcialmente cortadas. Abas são salvas; o Herói preserva rolagem por seção.
- Crônica da Semana nas Crônicas: estatísticas derivadas, histórico civil de segunda a domingo, narrativa condicional local e atualização após correções.
- Salão das Memórias pelo Perfil e Crônicas, filtros e inscrições com datas reais/disponíveis; conversas reconhecidas guardam agora uma cópia do texto.
- Sagas: abandono confirmado com progresso congelado, arquivo e nova tentativa com UUID/linha de base próprios; histórico, seletor Android de prazo e aviso de alterações não salvas no editor.
- Retrato ampliado, moldura base de classe, artefatos maiores nos detalhes e estado equipado explícito.
- Novas falas locais de Cronista/Elyra/Darian/Seren, painéis com símbolos e histórico das conversas reconhecidas.
- Cinco efeitos originais locais, opção de ativação e volume, respeitando modo silencioso e volume de mídia; sem música contínua ou vibração.
- Room v5, migração aditiva 4 → 5 e backup v6 aceitando v1–v6; versionCode 7, assinatura preservada.

# Mudanças — 1.4.0 · O Juramento do Norte

- Sagas Pessoais na área de Missões, seis modelos e criação personalizada; seleção de hábitos, métrica de conclusões/minutos/páginas, meta, prazo opcional, XP/moedas editáveis, marcos narrativos de 25/50/75/100% e resgate único.
- Fotografia dos registros no início, incluindo o dia atual; progresso anterior não conta. Correções recalculam progresso e recompensa resgatada com o ledger existente. Hábitos/métrica ficam fixos após iniciar; outros termos podem ser editados até o resgate.
- Novo SagaButton primário/secundário/destrutivo, estados pressionado/desativado/carregando/concluído, bronze com relevo e resposta curta. Ações compartilhadas, formulários, loja e resgates usam o componente.
- Jornada com região, emblema da classe, percentual do dia, pendências e próxima atividade. Medidores refinados e ações de hábitos em ferro recortado.
- Recompensas consolidadas por transação: valores reais de XP, saldo disponível e atributos; nível, conquistas raras, artefatos e restauração no mesmo painel quando ocorrerem juntos.
- Diálogos contextuais locais com prioridade e confirmação persistida; Cronista, restauração de moradores, saga, nível, conquista e retorno reconhecidos sem IA externa.
- Revisão pontual de oito capítulos, preservando os nove títulos, acontecimentos e desbloqueios; códice com parágrafos, diálogos e progresso de capítulos.
- Cinco emblemas originais, descrições de classe, pré-requisitos/estados mais explícitos na árvore, transição breve de reconstrução respeitando redução de movimento.
- Room v4 aditiva e backup v5 aceitando v1–v5. Mesmo certificado desde a 1.1, applicationId preservado, versionCode 6.

# Mudanças — 1.3.1 · Grimório e coerência artística

- Grimório dos Hábitos com 102 modelos, nove categorias, busca por palavras/nomes narrativos, seis filtros e quatro ordenações; formulário completo antes de adicionar.
- Seis modelos de rotina com prévia, seleção parcial, edição de cada novo hábito, reutilização de existentes e gravação transacional sem bônus de XP.
- Sugestões por interesses, calculadas localmente, dispensáveis. Nomes narrativos opcionais preservam o nome funcional. Contagem de sequência opcional por hábito, desativada inicialmente nos modelos de descanso/autocuidado.
- Equipamentos e companheiros pintados em nichos fora do retrato; 17 itens com imagens próprias, mantendo ações de equipar/retirar, raridades e procedência.
- Marcadores do atlas em pedra esculpida, símbolos regionais, fratura para bloqueio e contorno de seleção.
- Quinze cenas de reconstrução: cinco construções × três estágios. Ruínas, andaimes e edifícios iluminados diferenciam os estados visualmente.
- Botões em couro/metal, medidores entalhados e cartões de hábitos com dificuldade e nome narrativo; cabeçalho do Grimório acompanha a rolagem em telas pequenas.
- Room 3 com migração explícita 2 → 3 de três colunas; mantém 1 → 2. Backup 4 aceita 1–4; applicationId, certificado e cálculo de recompensas preservados.
- versionName 1.3.1, versionCode 5. Evidências em [VALIDACAO.md](VALIDACAO.md), [guia do Grimório](docs/GRIMORIO.md) e [revisão visual](docs/VISUAL-1.3.1.md).

---

# Mudanças — 1.3.0 · Redesign nórdico sombrio

## Telas

- Jornada recomposta em torno do painel heroico, com fiorde, retrato, nível, XP entalhado e atributos; progresso diário e hábitos continuam com ações diretas.
- Perfil com retrato ilustrado adulto, três tons de pele e ornamentos/equipamentos sobrepostos. Trajes, elmo, arma, amuleto, moldura, companheiro e fundo têm leitura visual própria.
- Inventário como arsenal de couro e ferro: retrato central, sete espaços, ícones detalhados, selos de raridade, detalhes de procedência e coleção filtrável. Abas do Herói permanecem fixas durante a rolagem.
- Atlas ilustrado com geografia, fiordes, montanhas, florestas, ruínas e névoa; marcadores selecionáveis preservam desbloqueios reais.
- Livro de missões com capa de couro, páginas gastas e costuradas, selos, objetivos e espólios separados. Chefes recebem uma ilustração original de presença mítica.
- Vila com cenário pintado e sinais luminosos de restauração integrados à paisagem. Crônicas passam a ser lidas em páginas de códice.

## Componentes e atmosfera

- Paleta de carvão, ardósia, bronze, marfim e gelo; tons de texto ajustados para contraste nos temas claro e escuro.
- Fonte Cinzel incorporada ao APK para títulos; corpo em fonte de sistema legível. Licença OFL incluída.
- Novos componentes HeroSaga, SagaCover, RuneSeal, SagaDivider e materialSurface; refinamento de Avatar, Landscape, Manuscript, RelicArt, AetherisMap, Meter, botões, chips, slots, notificações e celebração de nível.
- Grão discreto e determinístico, costuras, rebites, cantos cortados e iluminação fria/âmbar. As imagens são WebP locais, sem dependências remotas.
- Transição suave entre abas do Herói, brilho breve ao mudar equipamento e barras animadas. Redução de movimento desativa esses efeitos; modo minimalista simplifica a Jornada e remove grão dos painéis.
- Rótulos acessíveis para equipamentos e interruptores; alvos de toque de pelo menos 48 dp nas ações redesenhadas. Ajustes para 320 dp e texto em 130%.

## Compatibilidade

- VersionCode 4, versionName 1.3.0. Mesmo applicationId e certificado dos APKs 1.1/1.2 fornecidos.
- Sem mudanças no repositório de dados, regras de recompensa, esquema Room v2 ou formato de backup v3.
- 44 testes aprovados. Revisão visual documentada em [docs/VISUAL-1.3.md](docs/VISUAL-1.3.md).

---

# Mudanças — 1.2.0 · O Despertar de Aetheris

## Referências visuais e interação

- Livro de missões com papel texturizado, bordas ornamentais, objetivos, recompensas e resgate funcional. As abas e a edição dos contratos foram preservadas.
- Mapa vetorial em pergaminho, com rio, florestas, montanhas, trilhas, rosa dos ventos e seis regiões selecionáveis. Os bloqueios mostram o nível necessário.
- Inventário com retrato central, espaços de cabeça, corpo/capa, acessório e arma. Coleção em duas colunas, categorias, cinco raridades, origem e data de obtenção.
- Equipar e remover altera o retrato; fundos, molduras, companheiros e decoração continuam disponíveis. A lanterna ilumina a cena da vila.
- Atributos na Jornada e silhueta original para os chefes opcionais.

## Progressão e narrativa

- Seis habilidades em três caminhos: Sabedoria, Perseverança e Criação. Um ponto a cada três níveis; cada habilidade custa um ponto, exige nível e respeita o pré-requisito.
- Habilidades concedem seis cosméticos exclusivos, persistidos em transação; a loja não permite comprá-los diretamente. Não alteram recompensas de hábitos.
- Elmo do Viajante, Lâmina de Prata e Amuleto de Aether disponíveis no mercador.
- Elyra, Darian e Seren oferecem três missões com troféus; os diálogos dos moradores acompanham as três etapas da construção.
- Cinco títulos por atributos e um título de prestígio no nível 50.
- Habilidades conquistadas permanecem após correções de XP; novos pontos só aparecem após compensar o nível, evitando duplicação.

## Compatibilidade

- Mesmo applicationId e certificado do APK 1.1; versionCode 3, versionName 1.2.0.
- Room continua em v2, sem alteração de tabelas ou migração destrutiva. Itens e habilidades usam a coleção de assets existente.
- Backup v3, com leitura das versões 1, 2 e 3. Backups novos devem ser restaurados na 1.2.
- Equipamento utiliza os dados atuais do banco, preservando tipo e exclusividade de espaço.
- Sem permissão INTERNET, serviços pagos, imagens remotas ou fontes baixadas durante o uso.

Este é um incremento funcional do documento de expansão. A lista de recursos entregues e pendentes está em [PROGRESSO.md](PROGRESSO.md).

---

# Mudanças — 1.1.0

## Jornada e apresentação

- Cena original de Aetheris em destaque, com iluminação e restauração ligadas às construções existentes.
- Personagem, progresso de nível, resumo do dia, semana de consistência e pendências acessíveis na Jornada.
- Paletas escura e clara, opção de seguir o sistema e contraste ajustado para textos e atributos.
- Modo minimalista e redução de animações em barras, celebração e transições.
- Avisos mostram o XP efetivamente recebido. Missões prontas podem ser resgatadas na Jornada.

## Hábitos

- Busca por nome/categoria, filtro de estado e filtro de atributo.
- Detalhes com sequência atual, melhor sequência histórica, programação, calendário recente e edição de registros.
- Criação com campos essenciais primeiro e personalizações avançadas recolhidas.
- Atalhos para acrescentar quantidades ou minutos no registro, sempre preservando o teto e os snapshots.
- A visão de datas passadas respeita a programação histórica mesmo após arquivar um hábito.

## Descanso e estatísticas

- Descanso global por data, reversível pelo calendário. Nenhuma recompensa ou conclusão é apagada.
- Lembretes locais também ignoram descansos e retomam após intervalos prolongados, inclusive depois de reiniciar o aparelho.
- Sequências ignoram descansos planejados; registros extras continuam rendendo XP.
- A taxa considera apenas metas programadas e respectivas conclusões. Descansos e atividades não previstas ficam fora da razão.
- Gráfico de linha de XP, comparação com o período anterior, recorde histórico e distribuição por atributo.
- A Jornada atualiza a data local mesmo quando permanece aberta na virada do dia.

## Mundo e dados

- Quatro capítulos originais novos: As Promessas do Bosque, A Cartógrafa do Silêncio, A Fortaleza da Vontade e A Aurora de Aetheris.
- Banco Room v2 com migração 1 → 2 sem apagar as tabelas existentes.
- Restauração de backup disponível na tela de boas-vindas, sem criar um personagem provisório.
- Backups v2 incluem descansos. Backups v1 continuam sendo aceitos e validados.
- applicationId preservado; versionCode 2 e versionName 1.1.0.

Os limites ainda existentes estão em `PROGRESSO.md`. A assinatura deste APK é diferente da 1.0; siga `INSTALACAO.md` antes de trocar de versão.

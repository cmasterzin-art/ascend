# ASCEND 1.7.5.9 — UI Reforge (Módulo a Módulo)

> **Ramo:** `feat/ascend-1.7.5.9` · **Base preservada:** `feat/ascend-1.7.5.1` (`56f55e4`, 227 testes, Room v11, Backup v13)  
> **Escopo:** Reformulação visual global em Jetpack Compose com **NORTHBORN Design System 4.0**. Zero alteração em regra de negócio, economia de XP/Fragmentos, algoritmos de planejamento ou esquema de banco/backup.

---

## 1. Fundação Compartilhada (Núcleo Visual)

- **[`NorthbornDesignSystem.kt`](../app/src/main/java/br/com/ascend/aetheris/NorthbornDesignSystem.kt):** novo núcleo de tokens semânticos (`NorthbornPalette`, `NorthbornDarkPalette`, `NorthbornLightPalette`, `LocalNorthborn`), escala modular (`NorthbornSpace`), chanfros simétricos (`NorthbornShape`), 8 materiais de `northborn_materials_atlas.webp` (`NorthbornMaterial`), 5 papéis de superfície (`NorthbornSurface`), divisores geométricos vetoriais (`NorthbornDivider`), barra superior (`NorthbornTopBar`), cabeçalho editorial (`NorthbornEditorialHeader`), 15 ícones escandinavos de traço uniforme (`NorthbornIcon`) e Botões 4.0 em 7 níveis (`NorthbornButton`).
- **[`Design.kt`](../app/src/main/java/br/com/ascend/aetheris/Design.kt):** `AscendTheme` provê `LocalNorthborn`; reconstruídos `Title`, `Panel`, `Page`, `Meter`, `Empty`, `ChipRow` (sem bitmap sobreposto), `FormField` e `Toggle` (sem caixa cinza genérica).
- **[`DesignSystem2.kt`](../app/src/main/java/br/com/ascend/aetheris/DesignSystem2.kt):** `AetherSurface` delega para `NorthbornSurface`, removendo cabeçalhos automáticos repetitivos (*"SAGA PESSOAL"*, *"MARCA DE AETHERIS"*, *"COLEÇÃO DE ARTEFATOS"*); `AetherDialog` reconstruído em placa fosca com `NorthbornDivider(DiamondKnot)`; `CompactHero`, `DayLedger` e `MissionCard2` refinados.
- **[`NorthComponents.kt`](../app/src/main/java/br/com/ascend/aetheris/NorthComponents.kt):** `SagaButton` delega integralmente para `NorthbornButton` (eliminando adesivos rasterizados sobre botões Material); `NarrativePanel` promovido a superfície documental.
- **[`AetherisNavigation.kt`](../app/src/main/java/br/com/ascend/aetheris/AetherisNavigation.kt):** barra inferior reconstruída com `NorthbornIcon`, indicador superior fino em bronze e fundo limpo nos temas escuro e claro; `AetherisTabs` e `ShortcutStrip` alinhados ao sistema.
- **[`NordicCouncilDesign.kt`](../app/src/main/java/br/com/ascend/aetheris/NordicCouncilDesign.kt):** `CouncilMaterialSurface` integrado ao `northborn_materials_atlas.webp`; `NordicStrategyDivider` convertido em divisor vetorial limpo; `NordicCompactAction`, `OdinHallHeroBanner` e `CouncilKpiTile` adaptados aos dois temas.

---

## 2. Experiência Central

- **Jornada ([`JourneyHome.kt`](../app/src/main/java/br/com/ascend/aetheris/JourneyHome.kt)):** as 3 paredes empilhadas de `ShortcutStrip` foram substituídas por 1 faixa arquitetônica de 3 módulos primários (*Mesa de Odin*, *Forja Tempo*, *Planejador*) e 1 faixa horizontal leve de pílulas secundárias (*Caminhos*, *Conselho 9*, *Entrada*, *Pendências*, *Análises*, *Foco*); `WeekStrip` desenclausurado; botão `+` substituído por botão arquitetônico com `contentDescription = "Criar hábito"`; `RumosDoDiaPanel` convertido em superfície `Informative`.
- **Hábitos e Histórico ([`JourneyScreens.kt`](../app/src/main/java/br/com/ascend/aetheris/JourneyScreens.kt)):** removidos adesivos `viking_button_atlas` de `HabitCard`; desenclausurados `Routines` e `History` em seções editoriais abertas sem `Panel` envolvendo cartões.
- **Perfil do Herói ([`CharacterScreen.kt`](../app/src/main/java/br/com/ascend/aetheris/CharacterScreen.kt)):** eliminado aninhamento caixa-em-caixa em `ArchetypeAscensionPanel`; Identidade e Títulos desenclausurados; ícone de Configurações atualizado para `NorthbornIcon(Settings)`; diferenciação visual clara entre conquistas desbloqueadas (`Ceremonial`) e bloqueadas (`Operational`).
- **Configurações e Moldura Secundária ([`MainActivity.kt`](../app/src/main/java/br/com/ascend/aetheris/MainActivity.kt)):** rotas secundárias agora usam `NorthbornTopBar`; `SettingsScreen` reconstruída em seções editoriais limpas + painel documental de backup + colofão `ASCEND · versão 1.7.5.9`; diálogo `LevelUp` refinado.

---

## 3. Produtividade Estratégica

- **Mesa de Odin ([`OdinCouncilScreens.kt`](../app/src/main/java/br/com/ascend/aetheris/OdinCouncilScreens.kt)):** Centro de Comando, Plano de Hoje, Prazos e lista de Blocos Propostos foram desenclausurados em faixas abertas com `NorthbornDivider`, eliminando empilhamento homogêneo de caixas; seletor de estratégias e quadrantes Eisenhower/Replanejamento tornados seguros para tema claro e escuro.
- **Agenda 3.0 ([`AgendaScreens.kt`](../app/src/main/java/br/com/ascend/aetheris/AgendaScreens.kt)):** 2 `ShortcutStrip` empilhadas reduzidas a 1 faixa única de pílulas; grade semanal e calendário mensal migrados para tokens `LocalNorthborn` com contraste claro/escuro corrigido; projeções recorrentes virtuais `@rec:` da 1.7.5.1 integralmente preservadas.
- **Caminhos 3.0 ([`CaminhosScreens.kt`](../app/src/main/java/br/com/ascend/aetheris/CaminhosScreens.kt), [`Paths30Screens.kt`](../app/src/main/java/br/com/ascend/aetheris/Paths30Screens.kt)):** cabeçalho de abas desenclausurado; trilhos do Gantt adaptados ao tema claro/escuro (`nb.canvas`/`nb.surfaceElevated`/`nb.hairline`); Kanban 4 colunas reconstruído em raias arquitetônicas abertas sem caixa-em-caixa.
- **Forja do Tempo ([`TimeForgeScreen.kt`](../app/src/main/java/br/com/ascend/aetheris/TimeForgeScreen.kt)):** cronômetro mantido como peça central tátil (preservando `reconcileFocusTimer()` 100%), enquanto Ambiente Sonoro e Notificações Locais foram desenclausurados em seções editoriais limpas; `ImmersiveNordicFocusView` refinado em quietude mineral.
- **Conselho dos Nove ([`CouncilOfNineScreen.kt`](../app/src/main/java/br/com/ascend/aetheris/CouncilOfNineScreen.kt)):** cabeçalhos introdutórios desenclausurados; variação sutil de acento entre casas (bronze/fiorde/pátina); Objetivos Pessoais em superfície `Document` (velino arquivístico).
- **Análises e Histórico de Foco ([`ProductivityScreens.kt`](../app/src/main/java/br/com/ascend/aetheris/ProductivityScreens.kt)):** alinhamento ao canvas NORTHBORN e resumo `Informative`.

---

## 4. Conhecimento, RPG e Fluxos Secundários

- **Biblioteca de Mímir e Flashcards SM-2 ([`MimirScreens.kt`](../app/src/main/java/br/com/ascend/aetheris/MimirScreens.kt)):** identidade editorial documental dedicada (`NorthbornSurfaceRole.Document` com `ArchivalVellum`) nos cadernos e no palco de revisão SM-2, com divisores `DiamondKnot`.
- **Atlas de Aetheris ([`WorldScreen.kt`](../app/src/main/java/br/com/ascend/aetheris/WorldScreen.kt)):** legenda de 6 estados adaptada ao tema claro/escuro; Diário de Exploração desenclausurado em seção aberta.
- **Arsenal e Forja de Darian ([`EquipmentScreen.kt`](../app/src/main/java/br/com/ascend/aetheris/EquipmentScreen.kt), [`ForgeScreen.kt`](../app/src/main/java/br/com/ascend/aetheris/ForgeScreen.kt)):** câmaras centrais migradas para tokens `LocalNorthborn`, eliminando fundos escuros fixos no tema claro.
- **Codex Rúnico ([`HeraldryAndCodex.kt`](../app/src/main/java/br/com/ascend/aetheris/HeraldryAndCodex.kt)):** seletor de superfícies de estudo desenclausurado e seguro para tema claro.
- **Onboarding e Formulários ([`Forms.kt`](../app/src/main/java/br/com/ascend/aetheris/Forms.kt)):** chegada em 3 passos reconstruída em atmosfera escandinava editorial com `NorthbornDivider`, citação do Cronista em superfície `Document` e botões cerimoniais; chips de dias da semana no `HabitEditor` adaptados ao tema claro/escuro.

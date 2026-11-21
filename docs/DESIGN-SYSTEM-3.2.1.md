# Design System 3.2.1 — O Conselho Restaurado (`ASCEND 1.7.5.1`)

Evolução pictórica e ergonômica sobre o [Design System 3.2](DESIGN-SYSTEM-3.2.md) sem alterar nenhuma regra de negócio, persistência ou economia do RPG.

## 1. Seis Famílias Metálicas de Botões (`restored_council_buttons_atlas.webp`)

A `1.7.5.1` substituiu o fundo ruidoso alongado nos botões da `1.7.5` por um atlas dedicado (`960×450`, `2×3`, `51.922 bytes`) fatiado por célula via `drawAtlasCellNineSlice` em `NorthComponents.kt` (`SagaButton`), com centro uniforme para contraste AA/AAA e `capXRatio` calibrado (`0.13f`–`0.15f`):

| Família (`VikingButtonStyle`) | Célula (`2×3`) | Papel funcional | Estados |
|---|---|---|---|
| `CerimonialViking` | `(0, 1)` | CTA decisivo (ex.: *Gerar plano*, *Concluir e registrar*, *+ Novo objetivo*) | Largura total para preservar cabeças ornamentadas Urnes |
| `BronzeForjado` | `(1, 0)` | Ação principal cotidiana (ex.: *Forja do Tempo*, *Pausar*, *+ Bloco de tempo*) | Alto relevo em bronze martelado |
| `FerroRunico` | `(0, 0)` | Ação secundária e navegação tática (ex.: *Disponibilidade*, *Ir p/ pausa*) | Ferro escuro escovado com rebites |
| `Confirmacao` | `(1, 1)` | Aplicação, salvamento e conclusão (ex.: *Registrar foco*, *Retomar*, *Aplicar plano*) | Pátina verde-musgo sobre ferro |
| `Destrutivo` | `(0, 2)` | Exclusão, descarte e reversão controlada (ex.: *Desfazer último plano*) | Ferro escurecido com oxidação rubra contida |
| `CompactoRunico` | `(1, 2)` | Ações terciárias e atalhos compactos (ex.: *Modo Imersivo*, *Caminhos 3.0*, *Agenda 3.0*) | Placa compacta com contraste nítido |

Além das 6 placas de `SagaButton`, `NordicCompactAction` substitui `TextButton` soltos nos cartões de Kanban, Agenda 3.0 e Conselho dos Nove por peças táteis chanfradas (`heightIn(min = 38.dp)`), mantendo área de toque acessível.

## 2. Runas Históricas Contextuais (*Elder Futhark*)

Sem poluir rótulos funcionais ou criar ficção pseudohistórica, as runas vetoriais do *Elder Futhark* (`VikingDesignSystem.kt`) marcam o contexto visual de cabeçalhos (`NordicSectionHeader`), divisores (`NordicStrategyDivider`) e KPIs (`CouncilKpiTile`):
- **ᚫ Ansuz:** Salão de Odin, propostas e alertas de revisão em Mímir.
- **ᛏ Tiwaz:** Centro de comando estratégico e hierarquia de prioridades.
- **ᛃ Jera:** Ciclos, recorrência e capacidade semanal.
- **ᛞ Dagaz:** Plano do dia e transições de janela.
- **ᚱ Raido:** Replanejamento adaptativo e trilha de execução (`A fazer`).
- **ᚲ Kenaz:** Foco ativo na Forja do Tempo e tarefas em andamento.
- **ᚹ Wunjo:** Pausa regenerativa na Forja do Tempo.
- **ᛁ Isa:** Estado pausado, bloqueado ou bloco fixado.
- **ᛟ Othala:** Conselho dos Nove (casas da vida).
- **ᛊ Sowilo:** Estratégias de Odin e objetivos pessoais.
- **ᛚ Laguz:** Atmosfera sonora procedural offline.

## 3. Novos Atlas Pictóricos Originais (`365,6 KB` no APK)

| Arquivo | Dimensões | Grade | Tamanho | Papel |
|---|---|---|---|---|
| `docs/art/concepts/nordic-council-refined-1751.webp` | `1280×720` | Prancha | `139.048 B` | Referência conceitual (fora do APK) |
| `restored_council_buttons_atlas.webp` | `960×450` | `2×3` | `51.922 B` | 6 famílias de botões forjados |
| `time_forge_refinement_atlas.webp` | `960×640` | `3×2` | `114.968 B` | Aros de trabalho/descanso, medalhões de ciclo, selos de pausa/conclusão |
| `nordic_planning_marks_atlas.webp` | `768×384` | `4×2` | `60.656 B` | 8 marcadores de planejamento (crítica, dependência, marco, prazo, conflito, fixada, recorrente, concluído) |
| `nordic_strategy_frames_atlas.webp` | `1024×512` | `4×2` | `45.336 B` | Molduras, cantoneiras metálicas (`BronzeCorner`/`IronCorner`) e divisores Urnes/Borre/Velino |
| `odin_council_seals_refined_atlas.webp` | `768×512` | `3×2` | `101.498 B` | 6 medalhões refinados das estratégias de Odin |

**Acréscimo total no APK:** `374.380 bytes` (`365,6 KB` / `0,36 MB`), muito abaixo do teto de `2 MB`. Orçamento usado: `6/8` gerações (`2` de reserva preservadas).

## 4. Acessibilidade, Temas e Modos

- **Tema claro e escuro:** véus controlados em `CouncilMaterialSurface` e `SagaButton` preservam legibilidade AA/AAA.
- **Modo minimalista:** degrada automaticamente todos os atlas para runas vetoriais *Elder Futhark* e divisores planos (`SagaDivider`).
- **Redução de movimento:** preservada integralmente em `TimeForgeScreen`, `ImmersiveNordicFocusView` e `OdinCouncilScreen`.
- **Telas estreitas (`320 dp`) e fonte `130%`:** empilhamento responsivo via `BoxWithConstraints` e `FlowRow` evita sobreposições e garante áreas mínimas de toque.

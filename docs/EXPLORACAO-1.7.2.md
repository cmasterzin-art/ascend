# ASCEND 1.7.2 — Motor de Exploração Regional & Novas Regiões

A versão **1.7.2 — Além das Fronteiras** generaliza o motor de exploração introduzido na Floresta da Persistência (`1.5`) em uma arquitetura extensível baseada em dados ([`Exploration.kt`](../app/src/main/java/br/com/ascend/aetheris/Exploration.kt)), preservando integralmente os IDs históricos da Floresta (`roots`, `sanctuary`, `vigil`), o ledger transacional, os baselines idempotentes por fase, os snapshots de recompensa e a compatibilidade com Room v8 e backup v10.

## 1. Arquitetura do Motor Regional

- **`ExplorableRegionDef`, `ExpeditionDef`, `ExpeditionPhase` e `RegionNpcDef`:** definição declarativa por região contendo `regionIndex`, `unlockLevel`, `atlasRes` (`3 × 3`), selo fictício (`AetherisSealKind`), lista ordenada de expedições, título, conquista e NPC guardião com falas contextuais e variações por especialização de arquétipo (`specializationFlavor`).
- **Baselines e idempotência (`Sagas.kt`, `Repository.kt`):** cada fase de expedição captura um snapshot (`sagaBaseline`) ao iniciar ou avançar, mede apenas o progresso real posterior ao início e paga recompensas por chave determinística (`claim`), bloqueando duplicação mesmo sob chamadas concorrentes.
- **Zero alterações destrutivas de esquema:** Room v8 e backup v10 foram mantidos intactos; expedições, relíquias, conquistas e memórias usam as tabelas e chaves determinísticas já validadas (`quests`, `claims`, `assets`).

## 2. Métricas de Expedição e Integração Produtiva

Além das métricas originais de hábitos (`0 = conclusões`, `1 = minutos`, `2 = dias distintos`), a `1.7.2` adiciona métricas conectadas aos sistemas da `1.7.0` e `1.7.1`, sempre com **fallback explícito para hábitos** (`EXPEDITION_METRIC_HABITS = 0`) para quem prefere focar apenas na rotina básica:

| Código | Constante | Métrica e proteção anti-spam |
|---|---|---|
| `0` | `EXPEDITION_METRIC_HABITS` | Conclusões de hábitos selecionados (baseline real) |
| `4` | `EXPEDITION_METRIC_FOCUS_SESSIONS` | Sessões de Foco registradas (`focus_sessions` concluídas com `actualSeconds > 0`; sessões canceladas ou com `0s` são ignoradas; dias de descanso nunca penalizam) |
| `5` | `EXPEDITION_METRIC_FLASHCARD_DAYS` | Dias distintos com revisões na Biblioteca de Mímir (`flashcard_reviews`; proteção anti-spam: múltiplos flashcards no mesmo dia contam como `1` dia) |
| `6` | `EXPEDITION_METRIC_PATH_TASKS` | Tarefas de Caminhos concluídas após o início da fase (`path_tasks`; fallback disponível para hábitos) |

## 3. Biblioteca das Eras (Nível 5)

> **Distinção fundamental:** a **Biblioteca de Mímir** (`1.7.1`, em *Mundo → Crônicas*) é a ferramenta funcional de cadernos Markdown e flashcards SM-2; a **Biblioteca das Eras** (`1.7.2`, no *Atlas de Aetheris*) é uma **região narrativa explorável** que se integra opcionalmente às revisões de Mímir e às tarefas de Caminhos sem substituir o caderno funcional.

- **Guardiã:** **Eydis — Arquivista das Eras** (diálogos contextuais por progresso regional + 10 variações por especialização).
- **3 Expedições (8 fases):**
  1. *O Primeiro Manuscrito* (`lib_portico`, *O Pórtico dos Escribas*, 2 fases — `Tarefas de Caminhos` ou hábitos)
  2. *As Páginas do Silêncio* (`lib_acervo`, *O Acervo Submerso*, 3 fases — `Dias com revisões em Mímir` ou hábitos; relíquia *Anel do Escriba*)
  3. *O Juramento da Sabedoria* (`lib_camara`, *A Câmara de Mímir*, 3 fases — `Dias com revisões em Mímir` ou hábitos; guardião *O Vigia das Eras*; relíquia *Medalhão da Memória Eterna*)
- **Transformação visual em 3 estágios (`realm_library_atlas.webp`):** *Abandonada* (`0–32%`), *Em restauração* (`33–99%`) e *Restaurada* (`100%`).
- **Recompensas:** relíquias *Anel do Escriba* (`scribes-ring`) e *Medalhão da Memória Eterna* (`eternal-memory-medallion`), título **Guardião das Eras** e conquistas regionais.

## 4. Montanhas da Disciplina (Nível 8)

- **Guardião:** **Haldor — Vigia dos Cumes** (diálogos contextuais por progresso regional + 10 variações por especialização).
- **3 Expedições (8 fases):**
  1. *O Caminho dos Antigos* (`mnt_trilha`, *A Trilha de Ferro*, 2 fases — `Tarefas de Caminhos` ou hábitos)
  2. *A Chama que Não Cessa* (`mnt_torre`, *A Torre da Vigília*, 3 fases — `Sessões de Foco registradas` ou hábitos; relíquia *Broche da Vigília Boreal*)
  3. *O Silêncio dos Cumes* (`mnt_observatorio`, *O Observatório do Norte*, 3 fases — `Sessões de Foco registradas` ou hábitos; guardião *O Guardião de Granito*; relíquia *Bússola do Firmamento*)
- **Transformação visual em 3 estágios (`realm_mountains_atlas.webp`):** *Abandonada* (`0–32%`), *Em restauração* (`33–99%`) e *Restaurada* (`100%`).
- **Recompensas:** relíquias *Broche da Vigília Boreal* (`boreal-vigil-brooch`) e *Bússola do Firmamento* (`firmament-compass`), título **Testemunha dos Cumes** e conquistas regionais.

## 5. Relíquias Colecionáveis (4 novas peças)

As 4 relíquias regionais (`scribes-ring`, `eternal-memory-medallion`, `boreal-vigil-brooch`, `firmament-compass`) são exclusivas da exploração (**não compráveis na Loja**, `price = 0`), equipáveis no nicho de acessório na Câmara dos Equipamentos e ilustradas em `realm_relics_atlas.webp`.

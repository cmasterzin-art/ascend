# Design System 3.2 — Nordic Council (ASCEND 1.7.5)

O **Design System 3.2** (`NordicCouncilDesign.kt`) evolui o Design System 3.1 (1.7.3.1) adicionando **diversidade controlada de materiais nórdicos** para diferenciar semanticamente cada contexto estratégico sem aumentar a poluição visual.

## 1. Diversidade Controlada de Materiais (`CouncilMaterial`)

Amostrados de `nordic_council_materials_atlas.webp` (`3 × 2`, `960 × 640`, sem linhas de junção) com véu de contraste uniforme para garantir legibilidade AA/AAA em tema escuro e tema claro:

| Material | Célula | Uso Semântico |
|---|---|---|
| **Madeira Nórdica** (`NordicWood`) | `(0, 0)` | Centro de comando da Mesa de Odin, seleção de horários e controle de ambiente |
| **Velino Estratégico** (`AgedVellum`) | `(1, 0)` | Objetivos pessoais, matriz de Eisenhower, alertas e premissas |
| **Couro de Projeto** (`ProjectLeather`) | `(2, 0)` | Caminhos 3.0, blocos de proposta e cartões de replanejamento |
| **Bronze Martelado** (`HammeredBronze`) | `(0, 1)` | Estratégias de Odin e painel de Caminho Crítico (CPM) |
| **Prata Oxidada** (`OxidizedSilver`) | `(1, 1)` | Cronômetro da Forja do Tempo (Foco 3.0) |
| **Ferro Escuro** (`DarkIron`) | `(2, 1)` | Gantt, Kanban, Conselho dos Nove, histórico de planos e configurações |

## 2. Orçamento Pictórico e Peso no APK

- **Orçamento utilizado:** `10 / 12` gerações (`2` de reserva preservadas sem necessidade de uso).
- **1 prancha conceitual** fora do APK: `docs/art/concepts/nordic-council-175.webp` (`164,370` bytes).
- **9 recursos WebP de produção** em `app/src/main/res/drawable-nodpi/`: total de **664.174 bytes (0,63 MB)** no APK (teto: `4 MB`).
- Todas as 5 abas principais da barra inferior (`Jornada`, `Hábitos`, `Missões`, `Mundo`, `Herói`) permanecem intactas.
- Respeito integral a tema escuro/claro, redução de movimento, modo minimalista e telas estreitas `320dp` com fonte `130%`.

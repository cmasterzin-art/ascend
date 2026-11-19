# ASCEND 1.7.3 — Módulo II: A Forja de Darian

**A Forja de Darian** ([`ForgeCrafting.kt`](../app/src/main/java/br/com/ascend/aetheris/ForgeCrafting.kt), [`ForgeScreen.kt`](../app/src/main/java/br/com/ascend/aetheris/ForgeScreen.kt)) transforma a construção **A Forja** (`building:1`) da Vila do Despertar em uma oficina funcional de fabricação cosmética, preservando integralmente as **2 etapas históricas de restauração** (`0 → 1` por `30` Fragmentos e `1 → 2` por `60` Fragmentos).

## 1. Regras Econômicas e Integridade

- **Zero nova moeda:** todas as receitas utilizam exclusivamente os **Fragmentos de Aether** do ledger transacional existente (`forge:<recipeId>`).
- **100% cosmético (`0 XP`, `0` bônus de combate ou multiplicadores):** forjar uma peça nunca gera XP, não altera atributos e não cria vantagens econômicas.
- **Exclusividade garantida:** nenhuma das 8 peças da Forja aparece ou pode ser comprada na Loja (`Repository.buy()` bloqueia `forgeItemIds`), e nenhuma das 5 relíquias de expedição pode ser fabricada na Forja.
- **Idempotência concorrente (`Repository.craftForgeItem`):** executada dentro de transação Room com verificação de etapa da Forja, requisitos reais, posse prévia e saldo; chamadas concorrentes para a mesma receita debitam apenas uma vez.
- **Especialização `mestre_da_forja`:** desbloqueia diálogo exclusivo de Darian (`darianWorkshopDialogue`) e o selo dourado de Mestre da Forja na oficina, mantendo o **mesmo custo exato em Fragmentos** (zero vantagem econômica).

## 2. Catálogo das 8 Receitas Originais (`forgeRecipes`)

| Peça (`id`) | Espaço | Raridade | Custo | Etapa de `building:1` | Pré-requisito real |
|---|---|---|---:|---:|---|
| **Elmo do Juramento** (`oath-helm`) | Cabeça | Raro | `◈ 45` | `1+` | Nível `3+` |
| **Cota do Norte** (`north-mail`) | Corpo / capa | Raro | `◈ 55` | `1+` | Nível `4+` |
| **Vestes do Escriba** (`scribe-robes`) | Corpo / capa | Raro | `◈ 60` | `1+` | Conquista *Primeiro Tomo* (`mimir_first_tome`) |
| **Fíbula de Yggdrasil** (`yggdrasil-brooch`) | Acessório | Épico | `◈ 75` | `1+` | Expedição *A Vigília da Floresta* (`forest`) |
| **Coroa Rúnica de Mímir** (`mimir-runic-crown`) | Cabeça | Épico | `◈ 85` | `2` | Expedição *O Primeiro Manuscrito* (`library_portico`) |
| **Manto das Runas** (`rune-mantle`) | Corpo / capa | Épico | `◈ 90` | `2` | Nível `6+` |
| **Armadura da Vigília** (`vigil-armor`) | Corpo / capa | Épico | `◈ 100` | `2` | Expedição *O Caminho dos Antigos* (`mountains_trail`) |
| **Moldura dos Nove Reinos** (`nine-realms-frame`) | Moldura | Lendário | `◈ 120` | `2` | Nível `8+` |

## 3. Bancada de Prévia e Fluxo de Fabricação

- O cabeçalho da oficina exibe a cena correspondente ao estágio real da construção (`darian_forge_atlas.webp`, células `0`, `1` ou `2`) e o retrato de Darian (célula `3`).
- A **Prévia na Bigorna** aplica `previewOverrides` diretamente no `LivingHeroPortrait` sobre o fundo *Oficina dos Artífices* antes da confirmação explícita em diálogo (`AetherDialog`).
- Acesso direto via **Vila do Despertar → A Forja (`Abrir A Forja de Darian`)**, **Arsenal dos Nove Reinos** e **Herói**.

# ASCEND 1.7.1 — Ascensão dos Arquétipos

## 1. Visão geral

A **Ascensão dos Arquétipos** aprofunda a identidade de RPG do personagem em `Herói → Perfil` (`ArchetypeAscensionPanel`, `HeroStage`) e `Herói → Habilidades` (`SkillTree`), mantendo **zero nova moeda**, **zero multiplicadores artificiais de XP/moedas** e preservação integral das 5 classes base e das 6 habilidades universais originais.

## 2. Especializações (10 especializações — 2 por classe)

Persistidas na coluna `Player.specialization` (Room v8, `defaultValue = ''`). Exigem **nível 3** na classe correspondente (`canSelectSpecialization`). Podem ser escolhidas, trocadas ou removidas livremente sem alterar a classe principal nem apagar habilidades já despertadas. Ao trocar a classe principal do personagem, uma especialização incompatível é limpa automaticamente preservando todas as habilidades obtidas.

| Classe | Especialização | ID | Título concedido | Selo (`archetype_seals_atlas.webp`) |
|---|---|---|---|---|
| Guerreiro (`0`) | **Berserker** | `berserker` | Fúria do Norte | Índice `0` (Machado Rúnico) |
| Guerreiro (`0`) | **Sentinela** | `sentinela` | Guarda da Muralha | Índice `1` (Escudo de Ferro) |
| Mago (`1`) | **Erudito** | `erudito` | Leitor de Mímir | Índice `2` (Tomo Aberto) |
| Mago (`1`) | **Arquimago Rúnico** | `arquimago_runico` | Tecelão do Véu | Índice `3` (Círculo Rúnico) |
| Guardião (`2`) | **Protetor** | `protetor` | Escudo da Aurora | Índice `4` (Escudo Ornado) |
| Guardião (`2`) | **Druida** | `druida` | Voz das Raízes | Índice `5` (Árvore Ancestral) |
| Explorador (`3`) | **Cartógrafo** | `cartografo` | Mestre dos Mapas | Índice `6` (Bússola Nórdica) |
| Explorador (`3`) | **Patrulheiro** | `patrulheiro` | Olhos da Névoa | Índice `7` (Arco de Vigília) |
| Artífice (`4`) | **Mestre da Forja** | `mestre_da_forja` | Martelo Antigo | Índice `8` (Bigorna e Martelo) |
| Artífice (`4`) | **Tecelão de Runas** | `tecelao_de_runas` | Entalhador de Éter | Índice `9` (Nó de Urnes) |

Especializações e habilidades concedem emblemas no palco do herói, títulos selecionáveis no perfil e atalhos temáticos de conveniência (`Biblioteca de Mímir`, `Runas da Memória`, `Meus Caminhos`), sem alterar a economia.

## 3. Árvore de Habilidades expandida (16 habilidades no total)

Reutiliza o sistema existente `skillPoints(s)` (**1 ponto a cada 3 níveis**, `1` ponto por habilidade, sem moeda adicional).

- **10 novas habilidades de Ascensão por classe (`RpgContent.kt`):**
  - Guerreiro: `war_oath` (*Juramento de Ferro*, nível 3) → `war_resolve` (*Vontade de Ferro*, nível 6).
  - Mago: `mage_mimir` (*Olhar de Mímir*, nível 3) → `mage_runes` (*Teia das Runas*, nível 6).
  - Guardião: `guard_vigil` (*Vigília da Muralha*, nível 3) → `guard_roots` (*Raízes do Equilíbrio*, nível 6).
  - Explorador: `expl_compass` (*Bússola dos Reinos*, nível 3) → `expl_horizon` (*Passos da Névoa*, nível 6).
  - Artífice: `art_forge` (*Toque da Bigorna*, nível 3) → `art_runes` (*Entalhe de Éter*, nível 6).
- **6 habilidades universais preservadas intactas (`heroClass = -1`):** `scholar`, `runes`, `ember`, `will`, `craft` e `dawn` continuam listadas na seção `Constelações Universais · Relíquias` com seus respectivos itens cosméticos de inventário.
- **Filtro em `SkillTree`:** alternância entre `Minha classe (...)` e `Todas as classes`, exibindo requisitos legíveis (`skillRequirementText`) quando bloqueadas.

## 4. Marcos de Sabedoria (5 conquistas idempotentes com 0 moedas)

Adicionados em `Domain.kt` (`AchievementDef(..., coins = 0)`) e avaliados em `reconcile()` sem gerar entradas no ledger financeiro nem XP extra:

1. `mimir_first_tome` — **Primeiro Tomo**: criar o primeiro caderno na Biblioteca de Mímir.
2. `mimir_first_rune` — **A Primeira Runa**: concluir a primeira revisão nas Runas da Memória.
3. `mimir_ten_cards` — **Memória de Mímir**: revisar 10 flashcards distintos nas Runas da Memória.
4. `mimir_five_days` — **Guardião do Conhecimento**: revisar flashcards em 5 dias distintos.
5. `mimir_sage` — **Sábio dos Reinos**: reunir 10 páginas ativas e 3 ligações entre notas na Biblioteca de Mímir.

# Heráldica Rúnica de Aetheris — ASCEND 1.7.3.1

A **Heráldica Rúnica** introduz identidade simbólica escandinava personalizável (brasão, estandarte e inscrição rúnica curta), **100% cosmética e offline**, sem alterar XP, atributos, Fragmentos de Aether ou regras de economia.

## 1. Estrutura do Brasão e Estandarte

Implementação em [`RunicHeritageData.kt`](../app/src/main/java/br/com/ascend/aetheris/RunicHeritageData.kt) e [`HeraldryAndCodex.kt`](../app/src/main/java/br/com/ascend/aetheris/HeraldryAndCodex.kt):

- **8 Símbolos Centrais** (`ancestral_heraldry_atlas.webp`, 4×2):
  1. `raven` — Corvo do Norte (disponível desde o início)
  2. `wolf` — Lobo da Vigília (nível 3+ ou 1 expedição concluída)
  3. `oak` — Carvalho Ancestral (disponível desde o início)
  4. `axe` — Machado do Juramento (A Forja etapa 1+ ou 1 peça forjada)
  5. `shield` — Escudo da Persistência (disponível desde o início)
  6. `tome` — Tomo de Mímir (conquista *Primeiro Tomo* ou 1 caderno em Mímir)
  7. `mountain` — Montanha Boreal (disponível desde o início)
  8. `yggdrasil` — Árvore dos Nove Reinos (nível 6+, 1 construção restaurada ou 1 coleção completa)
- **4 Formatos de Base** (`viking_heraldic_bases_atlas.webp`, células 0–3):
  - `round_shield` (Escudo Circular), `oval_shield` (Escudo Oval), `runic_plaque` (Placa Rúnica), `ceremonial_medallion` (Medalhão Cerimonial)
- **4 Acabamentos Metálicos:**
  - `iron` (Ferro Enegrecido — inicial), `bronze` (Bronze Envelhecido — inicial), `silver` (Prata Oxidada — nível 4+ ou 1 expedição concluída), `gold` (Ouro Ancestral — nível 8+, 1 peça Consagrada ou 1 coleção completa)
- **5 Cores Secundárias:** `fjord_blue` (Azul Fiorde), `crimson_ember` (Brasa Antiga), `forest_moss` (Musgo do Norte), `obsidian` (Carvão Vulcânico), `ivory_ash` (Cinza de Velino).
- **5 Ornamentações Metálicas** (`forge_consacration_atlas.webp`, células 4–7): `none` (Sem ornamento), `iron_filigree` (Entalhe de Ferro), `bronze_borre` (Entrelaçado Borre), `silver_ringerike` (Filigrana Ringerike), `ceremonial_gold` (Coroa Cerimonial).
- **3 Estandartes Pessoais** (`viking_heraldic_bases_atlas.webp`, células 4–6):
  - `wool` (Estandarte de Tecido — inicial), `leather` (Estandarte de Couro — inicial), `ceremonial` (Estandarte Cerimonial — nível 5+ ou conquista Épica/Lendária).
- **Inscrição Rúnica:** até **6 runas** do Elder Futhark escolhidas manualmente ou por transliteração aproximada.
  - A interface declara explicitamente: *"Transliteração aproximada — não é tradução para nórdico antigo."*

## 2. Persistência e Compatibilidade

- **Room v10 (`MIGRATION_9_10`):** tabela aditiva `heraldry_profile` (`id = 1`, `symbolId`, `shapeId`, `metalId`, `secondaryColorId`, `inscriptionRunes`, `ornamentId`, `bannerId`, `showOnPortrait`, `updated`).
- **Backup v12:** serializa `heraldry: HeraldryProfile?` e valida todos os IDs, quantidade de runas (≤ 6) e integridade antes de substituir o banco local (rollback atômico em falha). Backups v1–v11 importam com `heraldry = null` e utilizam fallback seguro via `resolvedHeraldry(s)`.

## 3. Pontos de Exibição

- **Jornada (`CompactHero`):** brasão compacto (`22–26dp`) ao lado do nome quando configurado, preservando altura enxuta do cabeçalho.
- **Perfil do Herói (`HeroStage`) e Retrato Ampliado (`PortraitScreen`):** brasão + estandarte pessoal completo.
- **Retrato Vivo 1.1 (`LivingHeroPortrait`):** medalhão heráldico opcional no canto superior esquerdo quando `showOnPortrait = true` e `size >= 110.dp`.
- **Arsenal 2.1, Salão das Memórias, Crônicas da Semana e Coleções Lendárias concluídas.**

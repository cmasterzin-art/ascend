# ASCEND 1.7.3 — Módulo I: Retrato Vivo

O **Retrato Vivo** ([`LivingPortrait.kt`](../app/src/main/java/br/com/ascend/aetheris/LivingPortrait.kt)) faz o personagem refletir visualmente equipamentos, molduras, cenários de fundo e a especialização de arquétipo ativa — preservando integralmente os três retratos pintados originais (`wanderer_light.webp`, `wanderer_medium.webp` e `wanderer_dark.webp`, `1024 × 1536`, sem recorte destrutivo nem ocultação do rosto ou dos olhos).

## 1. Arquitetura em Camadas Pictóricas 2D

`LivingHeroPortrait` compõe o retrato em ordem determinística sobre uma proporção fixa `2:3`, consumido de forma unificada por `Avatar` (`NordicArt.kt`), `ForgePortrait` (`ForgeArt.kt`), `CompactHero` (`DesignSystem2.kt`), `HeroStage` (`HeroSheet.kt`), `Equipment` / `HeroCustomizationStudio` (`EquipmentScreen.kt`), `ForgeScreen` (`ForgeScreen.kt`) e `PortraitScreen` (`ChronicleScreens.kt`):

1. **Camada 1 — Fundo do Retrato (`hero_background_atlas.webp`, `2 × 2`):** Salão Viking, Biblioteca Rúnica (`scholar-bg`), Torre dos Cumes (`night`) ou Oficina dos Artífices (prévia na bigorna). Quando um fundo personalizado está ativo, uma máscara radial suave dissolve apenas o céu periférico do retrato original, preservando 100% do rosto, cabelo e ombros.
2. **Camada 2 — Retrato Base Original (`wanderer_light / medium / dark`):** nunca substituído nem alterado em arquivo.
3. **Camada 3 — Traje / Armadura (`living_armor_atlas.webp`, `2 × 2` RGBA):** `north-mail` (Cota do Norte), `scribe-robes` (Vestes do Escriba), `rune-mantle` (Manto das Runas), `vigil-armor` (Armadura da Vigília), além de variações tonais calibradas para `cloak`, `armor` e `dawn-outfit`.
4. **Camada 4 — Acessório / Fíbula no Manto (`living_artifacts_atlas.webp`, `4 × 2` RGBA):** `yggdrasil-brooch`, `mist-clasp`, `eternal-memory-medallion`, `firmament-compass`, `scribes-ring`, `boreal-vigil-brooch`, `ember-amulet` e `aether-amulet` ancorados na região do fecho do manto (`x ≈ 0.70`, `y ≈ 0.70`).
5. **Camada 5 — Elmo / Coroa Aberto (`living_helms_atlas.webp`, `2 × 2` RGBA):** `oath-helm` (Elmo do Juramento aberto), `mimir-runic-crown` (Coroa Rúnica de Mímir), `traveler-helm` (Elmo do Viajante) e `will-helm` (Coroa da Vontade), calibrados acima da linha dos olhos nos três tons de pele.
6. **Camada 6 — Marcadores Contextuais:** armas cosméticas (`silver-blade`, `craft-blade`), companheiros (`lyra`, `corvus`, `ignis`) e selo/runa da especialização ativa (`SpecializationVisualTheme` distinto para todas as **10 especializações**, usando exclusivamente as 12 runas históricas verificadas de `HistoricalFutharkRune`).
7. **Camada 7 — Moldura Pictórica (`hero_frames_atlas.webp`, `2 × 2` RGBA):** Ferro base, Bronze Borre (`frame`), Prata Rúnica (`rune-frame`) e Ouro Lendário dos Nove Reinos com Yggdrasil (`nine-realms-frame`).

## 2. Transparência de Representação e Modos Acessíveis

- `portraitRepresentationFor(itemId)` informa explicitamente na UI do Arsenal, Estúdio e Zoom (`PortraitScreen`) como cada peça aparece no Retrato Vivo (*Camada direta no retrato*, *Marcador contextual*, *Moldura ou fundo* ou *Decoração da vila*).
- No **modo minimalista**, `LivingHeroPortrait` simplifica sobreposições e fundos mantendo o retrato e moldura limpos; com **redução de movimento**, elimina transições animadas.
- O parâmetro `previewOverrides` permite provar itens bloqueados ou ainda não forjados no **Estúdio** e na **Forja** sem jamais alterar `GameState` ou conceder posse.

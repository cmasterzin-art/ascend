# Coleções Lendárias — ASCEND 1.7.3.1

As **Coleções Lendárias** organizam as **12 obras exclusivas da Forja de Darian** em **3 linhagens temáticas de 4 peças**, premiando dedicação com títulos e troféus cosméticos sem qualquer multiplicador de XP ou Fragmentos.

> **Distinção explícita:** As 3 **Coleções Lendárias** (*Juramento de Ferro*, *Sabedoria de Mímir* e *Vigília Boreal*) coexistem de forma independente das 3 **Configurações de Equipamento** salváveis (*Jornada*, *Conselho* e *Expedição*).

## 1. As 3 Coleções Lendárias (3 × 4 obras)

| Coleção | Runa | 4 Obras da Forja | Título Concedido |
|---|---|---|---|
| **Juramento de Ferro** (`iron_oath`) | `Tiwaz` | `oath-helm` (Elmo do Juramento), `north-mail` (Cota do Norte), `vigil-axe` (Machado da Vigília), `yggdrasil-brooch` (Fíbula de Yggdrasil) | **Juramento de Ferro** |
| **Sabedoria de Mímir** (`mimir_wisdom`) | `Ansuz` | `mimir-runic-crown` (Coroa Rúnica de Mímir), `scribe-robes` (Vestes do Escriba), `runic-staff` (Cajado Rúnico), `rune-mantle` (Manto das Runas) | **Herdeiro de Mímir** |
| **Vigília Boreal** (`boreal_vigil`) | `Algiz` | `vigil-armor` (Armadura da Vigília), `northern-blade` (Espada do Norte), `constellation-brooch` (Broche das Constelações), `nine-realms-frame` (Moldura dos Nove Reinos) | **Guardião Boreal** |

## 2. Regras de Conclusão e Integridade

- **Baseada em posse legítima:** todas as 4 peças podem ser obtidas exclusivamente na Forja de Darian. Obras já forjadas na 1.7.3 contam automaticamente após atualização.
- **Compatibilidade de espaços compartilhados:** em *Sabedoria de Mímir*, `scribe-robes` e `rune-mantle` compartilham o espaço `outfit` — por isso a coleção é concluída pela posse das 4 peças, sem exigir equipá-las simultaneamente.
- **Recompensa idempotente e sem vantagem econômica:**
  - `GameRepository.reconcile()` registra automaticamente o troféu `Asset(id = "collection:<id>", kind = "trophy", stage = 1)`.
  - `claimCollectionReward(collectionId)` é idempotente e registra `0 XP` e `0 Fragmentos`.
  - O título fica imediatamente disponível em `titles(s)` e no seletor de títulos do Perfil do Herói, além de desbloquear memória narrativa de Darian no Salão das Memórias.
- **Arte dedicada:** `legendary_collections_atlas.webp` (3×2: 3 pinturas temáticas + 3 medalhões de coleção).

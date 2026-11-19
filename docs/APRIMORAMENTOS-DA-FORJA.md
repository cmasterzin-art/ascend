# Aprimoramentos da Forja de Darian (3 Estágios) — ASCEND 1.7.3.1

A **Forja de Darian** passa de 8 para **12 receitas exclusivas** (adicionando `vigil-axe`, `runic-staff`, `northern-blade` e `constellation-brooch`) e introduz **aprimoramento artesanal em 3 estágios** para todas as 12 obras da Forja.

## 1. Estágios de Acabamento

Armazenado no campo existente `Asset.stage` (`1..3` para `forgeItemIds`), garantindo retrocompatibilidade total com peças já forjadas na 1.7.3 (que iniciam automaticamente em `stage = 1`):

1. **Estágio 1 — Forjado (`FORGE_STAGE_FORGED = 1`):** acabamento original da fabricação (`forge:<recipeId>`).
2. **Estágio 2 — Gravado (`FORGE_STAGE_ENGRAVED = 2`):** gravação rúnica prateada/fria; requer **Forja Etapa 1+** e posse no estágio 1.
3. **Estágio 3 — Consagrado (`FORGE_STAGE_CONSECRATED = 3`):** acabamento cerimonial em bronze/ouro envelhecido; requer **Forja Etapa 2** e posse no estágio 2.

## 2. Regras Econômicas e Determinismo

- **Sem pulo de estágio e sem aleatoriedade:** apenas `1 → 2` e `2 → 3` são aceitos; regressão ou valores fora de `1..3` falham validação.
- **Chave determinística no ledger:** `forge-upgrade:<itemId>:<stage>` (idempotente em chamadas concorrentes na mesma transação Room).
- **Zero XP e zero atributos de combate:** somente consumo legítimo de Fragmentos de Aether já conquistados.
- **Exclusividade preservada:** nenhuma das 12 peças da Forja entra à venda na Loja do Mercador (`Repository.buy` bloqueia `forgeItemIds`).

## 3. Alcance Visual Honesto

Cada peça exibe exatamente onde o aprimoramento se manifesta (`upgradeVisualScopeFor(itemId)`):
- **Retrato Vivo 1.1 (`LivingHeroPortrait`):** trajes e molduras recebem overlays reais de `living_hero_heritage_atlas.webp` para os estágios *Gravado* e *Consagrado*; armas, elmos e broches exibem a peça dedicada, selo `UpgradeStageBadge` e moldura aprimorada.
- **Forja, Arsenal 2.1, Estúdio e Retrato Ampliado:** selos pictóricos de `forge_consacration_atlas.webp` (ferro forjado, gravado prateado e consagrado dourado) + bordas graduadas.
